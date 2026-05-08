package org.bukkit.plugin.java;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.InvalidPluginException;
import io.ampznetwork.lunararc.common.server.LunarArcRemapper;
import io.ampznetwork.lunararc.common.server.LunarArcPluginLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PluginClassLoader extends URLClassLoader {
    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();
    private final PluginDescriptionFile description;
    private final File dataFolder;
    private final File file;
    private JavaPlugin plugin;
    private final LunarArcRemapper remapper = new LunarArcRemapper();

    private static final List<PluginClassLoader> loaders = new CopyOnWriteArrayList<>();

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public PluginClassLoader(LunarArcPluginLoader loader, ClassLoader parent, PluginDescriptionFile description,
            File dataFolder, File file) throws MalformedURLException {
        super(new URL[] { file.toURI().toURL() }, parent);
        this.description = description;
        this.dataFolder = dataFolder;
        this.file = file;
        loaders.add(this);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return findClass(name, true);
    }

    public Class<?> findClass(String name, boolean checkGlobal) throws ClassNotFoundException {
        Class<?> result = classes.get(name);
        if (result != null) {
            return result;
        }

        // 1. Platform classes (API, NMS) -> Parent
        if (name.startsWith("org.bukkit.") ||
                name.startsWith("com.destroystokyo.paper.") ||
                name.startsWith("io.papermc.paper.") ||
                name.startsWith("net.kyori.")) {

            // CraftBukkit remapping
            if (name.startsWith("org.bukkit.craftbukkit.")) {
                String mappedName = remapper.map(name.replace('.', '/')).replace('/', '.');
                if (!mappedName.equals(name)) {
                    try {
                        return getParent().loadClass(mappedName);
                    } catch (ClassNotFoundException ignored) {
                    }
                }
            }

            try {
                return getParent().loadClass(name);
            } catch (ClassNotFoundException ignored) {
            }
        }

        // 2. Local Plugin JAR
        String path = name.replace('.', '/').concat(".class");
        URL url = findResource(path);
        if (url != null) {
            try (InputStream is = url.openStream()) {
                byte[] bytecode = is.readAllBytes();
                byte[] transformed = remapper.transform(bytecode);

                int lastDot = name.lastIndexOf('.');
                if (lastDot != -1) {
                    String pkgName = name.substring(0, lastDot);
                    if (getDefinedPackage(pkgName) == null) {
                        definePackage(pkgName, null, null, null, null, null, null, null);
                    }
                }

                CodeSource codeSource = new CodeSource(this.file.toURI().toURL(), (Certificate[]) null);
                ProtectionDomain protectionDomain = new ProtectionDomain(codeSource, null, this, null);

                result = defineClass(name, transformed, 0, transformed.length, protectionDomain);
                classes.put(name, result);
                return result;
            } catch (IOException e) {
                throw new ClassNotFoundException(name, e);
            }
        }

        // 3. Other Plugins
        if (checkGlobal) {
            for (PluginClassLoader otherLoader : loaders) {
                if (otherLoader == this)
                    continue;
                try {
                    return otherLoader.findClass(name, false);
                } catch (ClassNotFoundException ignored) {
                }
            }
        }

        throw new ClassNotFoundException(name);
    }

    @Override
    public URL getResource(String name) {
        URL url = findResource(name);
        if (url == null) {
            url = getParent().getResource(name);
        }
        if (url == null) {
            for (PluginClassLoader otherLoader : loaders) {
                if (otherLoader == this)
                    continue;
                url = otherLoader.findResource(name);
                if (url != null)
                    break;
            }
        }
        return url;
    }

    public synchronized void initialize(JavaPlugin plugin) {
        if (this.plugin != null) {
            throw new IllegalStateException("Plugin already initialized!");
        }
        this.plugin = plugin;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            loaders.remove(this);
        }
    }
}
