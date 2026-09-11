package org.bukkit.plugin.java;

import io.lunararcdevs.lunararc.common.mod.LunarArcRemapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.security.MessageDigest;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.HexFormat;

final class TransformingPluginLibraryClassLoader extends URLClassLoader {
    private final LunarArcRemapper remapper;
    private final Path cacheRoot;

    static {
        ClassLoader.registerAsParallelCapable();
    }

    TransformingPluginLibraryClassLoader(URL[] urls, ClassLoader parent, LunarArcRemapper remapper, Path cacheRoot) {
        super(urls, parent);
        this.remapper = remapper;
        this.cacheRoot = cacheRoot;
    }

    Class<?> findOwnClass(String name) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> loaded = findLoadedClass(name);
            return loaded != null ? loaded : findClass(name);
        }
    }

    URL findOwnResource(String name) {
        return findResource(name);
    }

    java.util.Enumeration<URL> findOwnResources(String name) throws IOException {
        return findResources(name);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String resourcePath = name.replace('.', '/') + ".class";
        URL resource = findResource(resourcePath);
        if (resource == null) throw new ClassNotFoundException(name);

        try (InputStream stream = resource.openStream()) {
            byte[] original = stream.readAllBytes();
            byte[] transformed = transformed(resourcePath, resource, original, name);

            int split = name.lastIndexOf('.');
            if (split > 0) {
                String pkg = name.substring(0, split);
                if (getDefinedPackage(pkg) == null) {
                    try {
                        definePackage(pkg, null, null, null, null, null, null, null);
                    } catch (IllegalArgumentException ignored) {
                        // Parallel load may have defined it first.
                    }
                }
            }

            CodeSource source = new CodeSource(resource, (Certificate[]) null);
            ProtectionDomain protectionDomain = new ProtectionDomain(source, null, this, null);
            return defineClass(name, transformed, 0, transformed.length, protectionDomain);
        } catch (IOException | RuntimeException error) {
            throw new ClassNotFoundException("Could not transform plugin library class " + name, error);
        }
    }

    private byte[] transformed(String resourcePath, URL resource, byte[] original, String className) throws IOException {
        String key = digest(resourcePath, resource.toExternalForm(), original);
        Path cached = cacheRoot.resolve(key + ".class").normalize();
        if (!cached.startsWith(cacheRoot)) throw new IOException("Unsafe plugin library cache path");

        if (Files.isRegularFile(cached)) {
            byte[] bytes = Files.readAllBytes(cached);
            if (bytes.length >= 8) return bytes;
        }

        LunarArcRemapper activeRemapper = remapper;
        if (!remapper.isNmsRemappingEnabled() && PluginClassLoader.hasLegacyCraftBukkitReference(original)) {
            activeRemapper = new LunarArcRemapper(true);
        }
        byte[] transformed = activeRemapper.transform(original, className.replace('.', '/'));
        Files.createDirectories(cached.getParent());
        Path temp = cached.resolveSibling(cached.getFileName() + ".tmp");
        Files.write(temp, transformed);
        try {
            Files.move(temp, cached, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
            Files.move(temp, cached, StandardCopyOption.REPLACE_EXISTING);
        }
        return transformed;
    }

    private static String digest(String resourcePath, String resource, byte[] bytecode) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(resourcePath.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            digest.update(resource.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            digest.update(bytecode);
            return HexFormat.of().formatHex(digest.digest());
        } catch (java.security.NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }
}
