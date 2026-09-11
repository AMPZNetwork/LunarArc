package org.bukkit.plugin.java;

import org.bukkit.plugin.PluginDescriptionFile;
import io.lunararcdevs.lunararc.common.mod.server.LunarArcServer;
import io.lunararcdevs.lunararc.common.mod.LunarArcRemapper;
import io.lunararcdevs.lunararc.common.mod.PluginMappingNamespace;
import io.lunararcdevs.lunararc.common.server.LunarArcPluginLoader;
import io.lunararcdevs.lunararc.common.server.LunarArcPluginFixManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.security.MessageDigest;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.Map;
import java.util.HexFormat;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;

public final class PluginClassLoader extends URLClassLoader
        implements io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader {

    private final Map<String, Class<?>> classes = new ConcurrentHashMap<>();
    private final PluginDescriptionFile description;
    private final File dataFolder;
    private final File file;
    private final LunarArcPluginLoader pluginLoader;
    private final ClassLoader libraryLoader;
    private final ClassLoader paperLibraryLoader;
    private final Set<String> joinedPaperDependencies;
    private JavaPlugin plugin;
    private final PluginMappingNamespace mappingNamespace;
    private final boolean remapNms;
    private final boolean paperPlugin;
    private final LunarArcRemapper remapper;
    private final Path transformedCacheRoot;
    private final java.util.concurrent.atomic.AtomicBoolean closed = new java.util.concurrent.atomic.AtomicBoolean();

    public io.papermc.paper.plugin.provider.entrypoint.DependencyContext dependencyContext;
    private volatile io.papermc.paper.plugin.provider.classloader.PluginClassLoaderGroup group;

    private static volatile ClassLoader compatibilityLibraryLoader;

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public PluginClassLoader(LunarArcPluginLoader loader, ClassLoader parent, PluginDescriptionFile description,
            File dataFolder, File file) throws MalformedURLException {
        super(new URL[]{file.toURI().toURL()}, parent);
        this.description = description;
        // Before any of this plugin's classes load: some plugins need a system property in place
        // ahead of their own class initialization, and the transformed-class cache means a
        // per-class hook cannot be relied on to run.
        io.lunararcdevs.lunararc.common.server.LunarArcPluginFixManager
                .applyPluginProperties(description == null ? null : description.getName());
        io.lunararcdevs.lunararc.common.server.LunarArcModuleOpener.openMinecraftModuleTo(
                this, description == null ? null : description.getName());
        this.dataFolder = dataFolder;
        this.file = file;
        this.pluginLoader = loader;
        this.mappingNamespace = PluginMappingNamespace.detect(file);
        this.remapNms = this.mappingNamespace.requiresNmsRemap();
        this.paperPlugin = isPaperPlugin(file);
        this.remapper = new LunarArcRemapper(this.remapNms);
        this.transformedCacheRoot = createTransformedCacheRoot(file, this.mappingNamespace);
        this.libraryLoader = wrapPluginLibraryLoader(
                io.lunararcdevs.lunararc.common.server.LunarArcLegacyLibraryResolver.create(description, parent),
                "bukkit-libraries");
        this.paperLibraryLoader = wrapPluginLibraryLoader(
                io.lunararcdevs.lunararc.common.server.LunarArcPaperPluginSupport
                        .createLibraryLoader(file, description, dataFolder, parent),
                "paper-libraries");
        this.joinedPaperDependencies = this.paperPlugin
                ? io.lunararcdevs.lunararc.common.server.LunarArcPaperPluginSupport.joinedDependencies(file)
                : java.util.Set.of();
        this.pluginLoader.getClassSpace().register(this, description, this.paperPlugin);
        // Real Paper registers the classic (Spigot) plugin classloader into a group right here in
        // the constructor, before the plugin's main class is ever loaded. Without it every classic
        // plugin stayed outside PaperClassLoaderStorage entirely until enable time, when
        // PaperPluginInstanceManager's registerUnsafePlugin() fallback caught it and warned
        // ("Enabled plugin with unregistered ConfiguredPluginClassLoader ..."); until that point
        // its classes were invisible to the global group, so cross-plugin lookups during onLoad()
        // could not see them. The group's library predicate only dereferences dependencyContext
        // at class-resolution time, which is always after the provider has assigned it.
        this.group = io.papermc.paper.plugin.provider.classloader.PaperClassLoaderStorage.instance()
                .registerSpigotGroup(this);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return findClass(name, true);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> loaded = findLoadedClass(name);
            if (loaded == null) {
                if (isPlatformClass(name)) {
                    try {
                        loaded = loadPlatformClassWithBackstop(name);
                    } catch (ClassNotFoundException notOnServer) {
                        // "Platform" is a prefix guess, not a guarantee the server has the class.
                        // javax.* in particular is mostly JDK but not entirely: plugins shade
                        // javax.inject and javax.annotation routinely. Dead-ending here meant a
                        // plugin's own bundled copy was never consulted - floodgate failed to load
                        // with NoClassDefFoundError: javax/inject/Provider even though its jar
                        // contains that class. Classes the server must own are still refused.
                        if (isServerOwnedClass(name)) throw notOnServer;
                        loaded = findClass(name, true);
                    }
                } else {
                    try {
                        loaded = findClass(name, true);
                    } catch (ClassNotFoundException ignored) {
                        loaded = getParent().loadClass(name);
                    }
                }
            }
            if (resolve) resolveClass(loaded);
            return loaded;
        }
    }

    public Class<?> findClass(String name, boolean checkGlobal) throws ClassNotFoundException {

        Class<?> result = classes.get(name);
        if (result != null) return result;

        if (isPlatformClass(name)) {
            try { return loadPlatformClassWithBackstop(name); } catch (ClassNotFoundException ignored) {}
        }

        String path = name.replace('.', '/').concat(".class");
        URL url = findResource(path);
        if (url != null) {
            try (InputStream is = url.openStream()) {
                byte[] original = is.readAllBytes();
                byte[] bytecode = loadOrCreateTransformedClass(path, name, original);

                String pkg = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : null;
                if (pkg != null && getDefinedPackage(pkg) == null) {
                    definePackage(pkg, null, null, null, null, null, null, null);
                }

                CodeSource cs = new CodeSource(file.toURI().toURL(), (Certificate[]) null);
                ProtectionDomain pd = new ProtectionDomain(cs, null, this, null);
                result = defineClass(name, bytecode, 0, bytecode.length, pd);
                classes.put(name, result);
                return result;
            } catch (IOException e) {
                throw new ClassNotFoundException(name, e);
            }
        }

        if (checkGlobal) {
            try {
                return lunararc$traceSource(name, "another plugin (dependency class space)",
                        this.pluginLoader.getClassSpace().findDependencyClass(
                                this, name, visibleDependencies(), !this.paperPlugin));
            } catch (ClassNotFoundException ignored) {
            }
        }

        if (paperLibraryLoader != null) {
            try { return lunararc$traceSource(name, "paper library loader", paperLibraryLoader.loadClass(name)); }
            catch (ClassNotFoundException ignored) {}
        }

        if (libraryLoader != null) {
            try { return lunararc$traceSource(name, "plugin library loader", libraryLoader.loadClass(name)); }
            catch (ClassNotFoundException ignored) {}
        }

        ClassLoader compatibilityLoader = compatibilityLibraryLoader();
        if (compatibilityLoader != null) {
            try { return lunararc$traceSource(name, "compatibility library loader", compatibilityLoader.loadClass(name)); }
            catch (ClassNotFoundException ignored) {}
        }

        ClassLoader modCL = LunarArcServer.modClassLoader();
        if (modCL != null && modCL != getParent()) {
            try { return lunararc$traceSource(name, "mod class loader", modCL.loadClass(name)); }
            catch (ClassNotFoundException ignored) {}
        }

        try { return lunararc$traceSource(name, "parent", getParent().loadClass(name)); }
        catch (ClassNotFoundException ignored) {}

        throw new ClassNotFoundException(name);
    }

    private static Class<?> lunararc$traceSource(String name, String source, Class<?> resolved) {
        if (io.lunararcdevs.lunararc.common.LunarArcDebug.CLASSLOAD && resolved != null) {
            ClassLoader definer = resolved.getClassLoader();
            io.lunararcdevs.lunararc.common.LunarArcDebug.classload("{}: answered by {}, defined by {}",
                    name, source, definer == null ? "the bootstrap loader" : definer.getClass().getName()
                            + "@" + Integer.toHexString(System.identityHashCode(definer)));
        }
        return resolved;
    }

    private byte[] loadOrCreateTransformedClass(String resourcePath, String className, byte[] original) throws IOException {

        Path cached = transformedCacheRoot.resolve("classes")
                .resolve(cacheKey(resourcePath) + ".class").normalize();
        if (!cached.startsWith(transformedCacheRoot)) {
            throw new IOException("Unsafe transformed plugin cache path: " + resourcePath);
        }
        if (Files.isRegularFile(cached)) {
            try {
                byte[] bytes = Files.readAllBytes(cached);
                if (bytes.length >= 8) return bytes;
            } catch (IOException ignored) {
            }
        }

        LunarArcRemapper activeRemapper = this.remapper;
        if (!this.remapNms && hasLegacyCraftBukkitReference(original)) {

            activeRemapper = new LunarArcRemapper(true);
        }
        // Paper's own plugin rewriter runs first, on the bytecode exactly as the plugin author
        // compiled it. CraftBukkit reaches it through Bukkit.getUnsafe().processClass(); LunarArc
        // calls the same code directly because the remapper this loader picks depends on the
        // plugin's mapping namespace, which UnsafeValues has no way to know. Choosing
        // activeRemapper from `original` above is deliberate: Commodore strips the legacy
        // versioned CraftBukkit prefix, so the detection has to happen before it runs.
        // Only the classic path gets it, exactly as real Paper does. A paper-plugin.yml plugin is
        // written against current Mojang-mapped Paper API and carries no plugin.yml api-version,
        // so Commodore would read it as pre-flattening and apply the whole legacy reroute set to
        // code that is already correct. Paper routes those plugins through
        // ClassloaderBytecodeModifier instead, which is a no-op here for the same reason.
        byte[] staged = this.paperPlugin
                ? original
                : org.bukkit.craftbukkit.util.CraftMagicNumbers
                        .applyPaperPluginRewrites(this.description, resourcePath, original);
        byte[] transformed = activeRemapper.transform(staged, className.replace('.', '/'));
        // Real technique from MohistMC/Youer's PluginFixManager — patches known-problematic
        // third-party plugin classes (currently: WorldEdit/FAWE version-detection checks) that
        // would otherwise fail on a hybrid server. Applied after remapping, on the final
        // bytecode that actually gets cached and loaded.
        transformed = LunarArcPluginFixManager.injectPluginFix(className, transformed);
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

    static boolean hasLegacyCraftBukkitReference(byte[] bytecode) {
        byte[] marker = "org/bukkit/craftbukkit/v".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        outer:
        for (int i = 0; i <= bytecode.length - marker.length; i++) {
            for (int j = 0; j < marker.length; j++) {
                if (bytecode[i + j] != marker[j]) continue outer;
            }
            return true;
        }
        return false;
    }

    private static String cacheKey(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    private static Path createTransformedCacheRoot(File pluginFile, PluginMappingNamespace mappingNamespace) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = Files.newInputStream(pluginFile.toPath())) {
                byte[] buffer = new byte[128 * 1024];
                int read;
                while ((read = input.read(buffer)) >= 0) {
                    if (read > 0) digest.update(buffer, 0, read);
                }
            }
            digest.update(mappingNamespace.name().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            digest.update(io.lunararcdevs.lunararc.common.server.LunarArcVersionInfo.minecraftVersion()
                    .getBytes(java.nio.charset.StandardCharsets.UTF_8));

            digest.update("compat-transform-v21-chestshop-frames".getBytes(java.nio.charset.StandardCharsets.UTF_8));

            // The token above has to be bumped by hand whenever the transform changes, and it was
            // missed once already: a remapper fix shipped, every plugin kept loading the bad
            // bytecode cached under the old key, and the fix looked like it had not worked.
            // Folding the LunarArc version in makes that failure impossible - any build that
            // changes the transformer also changes the key. Restarts on an unchanged build still
            // hit the cache, which is what it is for.
            digest.update(io.lunararcdevs.lunararc.common.server.LunarArcVersionInfo.lunarArcVersion()
                    .getBytes(java.nio.charset.StandardCharsets.UTF_8));

            ClassLoader owner = PluginClassLoader.class.getClassLoader();
            String mappingBase = "mappings/" + io.lunararcdevs.lunararc.common.server.LunarArcVersionInfo.minecraftVersion() + "/";
            for (String resource : new String[]{"paper-reobf.tiny", "plugin-remap.tsv"}) {
                try (InputStream mapping = owner.getResourceAsStream(mappingBase + resource)) {
                    if (mapping != null) {
                        byte[] buffer = new byte[32 * 1024];
                        int count;
                        while ((count = mapping.read(buffer)) >= 0) {
                            if (count > 0) digest.update(buffer, 0, count);
                        }
                    }
                }
            }
            String key = HexFormat.of().formatHex(digest.digest());
            Path root = io.lunararcdevs.lunararc.common.LunarArcPaths.transformedPlugins()
                    .resolve(key.substring(0, 24));
            Files.createDirectories(root);
            return root;
        } catch (Exception error) {
            Path fallback = io.lunararcdevs.lunararc.common.LunarArcPaths.transformedPlugins()
                    .resolve(normalize(pluginFile.getName()));
            try { Files.createDirectories(fallback); } catch (IOException ignored) {}
            return fallback;
        }
    }

    private Class<?> loadPlatformClass(String name) throws ClassNotFoundException {
        try {
            Class<?> found = getParent().loadClass(name);
            if (io.lunararcdevs.lunararc.common.LunarArcDebug.CLASSLOAD) {
                io.lunararcdevs.lunararc.common.LunarArcDebug.classload("{}: parent resolved as requested", name);
            }
            return found;
        } catch (ClassNotFoundException notUnderRequestedName) {
            if (name.startsWith("org.bukkit.craftbukkit.") || (this.remapNms && name.startsWith("net.minecraft."))) {
                String mapped = remapper.map(name.replace('.', '/')).replace('/', '.');
                if (!mapped.equals(name)) {
                    try {
                        Class<?> found = getParent().loadClass(mapped);
                        if (io.lunararcdevs.lunararc.common.LunarArcDebug.CLASSLOAD) {
                            io.lunararcdevs.lunararc.common.LunarArcDebug.classload(
                                    "{}: absent under that name, parent resolved mapped name {}", name, mapped);
                        }
                        return found;
                    } catch (ClassNotFoundException ignored) {
                    }
                }
            }
            if (io.lunararcdevs.lunararc.common.LunarArcDebug.CLASSLOAD) {
                io.lunararcdevs.lunararc.common.LunarArcDebug.classload("{}: not on the parent under any name", name);
            }
            throw notUnderRequestedName;
        }
    }

    /**
     * {@link #loadPlatformClass} plus the mod class loader as a last resort.
     */
    private Class<?> loadPlatformClassWithBackstop(String name) throws ClassNotFoundException {
        if (name.startsWith("com.google.gson.")) {
            return com.google.gson.JsonElement.class.getClassLoader().loadClass(name);
        }
        try {
            return loadPlatformClass(name);
        } catch (ClassNotFoundException notOnParent) {
            // The parent is whichever loader built this plugin's provider, normally the mod's own
            // loader, which sees Minecraft. That is an assumption about how the active loader
            // arranged its class space rather than something LunarArc controls, and findClass
            // already keeps modClassLoader() as a backstop for the non-platform path. Platform
            // classes deserve the same backstop: an NMS class reachable from the mod but not from
            // this plugin's parent should resolve rather than surface as a link error in the
            // middle of a plugin's method.
            ClassLoader modClassLoader = LunarArcServer.modClassLoader();
            if (modClassLoader != null && modClassLoader != getParent()) {
                try {
                    return modClassLoader.loadClass(name);
                } catch (ClassNotFoundException ignored) {
                }
            }
            throw notOnParent;
        }
    }

    /**
     * Classes the server must own outright, where a plugin's bundled copy would be wrong rather
     * than merely redundant: the JVM's own packages, the Bukkit/Paper API the server implements,
     * Minecraft itself, and Adventure, which passes objects across the plugin boundary and breaks
     * if two copies exist. MohistMC/Youer draws the same line, refusing org.bukkit and
     * net.minecraft in findClass so they can only ever come from the server.
     */
    private static boolean isServerOwnedClass(String name) {
        return name.startsWith("java.")
                || name.startsWith("jdk.")
                || name.startsWith("sun.")
                || name.startsWith("org.bukkit.")
                || name.startsWith("net.minecraft.")
                || name.startsWith("io.papermc.paper.")
                || name.startsWith("com.destroystokyo.paper.")
                || name.startsWith("net.kyori.")
                // isPlatformClass's own comment explains why gson is routed through the platform
                // backstop first - a same-name-different-loader split between it and a plugin's own
                // bundled gson is exactly what took Essentials down with "Type 'JsonArray' is not
                // assignable to 'JsonElement'" in onDisable(). "Preferring" the platform copy was
                // not enough on its own: loadClass's catch at line ~110 falls back to this
                // classloader's own six-source chain - which can find a plugin's own bundled gson -
                // whenever the platform backstop's two sources (parent, then the mod loader) both
                // miss, and Essentials shipping its own gson under this exact package is exactly the
                // case where that fallback finds something. That single fallback resolving
                // differently than every other reference already cached as the platform's copy is
                // the split itself, not a fix for it. Being server-owned here closes that: a miss on
                // both platform sources throws instead of silently trying a plugin-local copy, so
                // every reference across the whole server resolves from the same source or fails
                // together - the same guarantee this class already gives net.minecraft./org.bukkit.,
                // and just as safe here since NeoForge's own dependency tree carries gson the same
                // way it carries net.kyori., so the platform backstop realistically never misses.
                || name.startsWith("com.google.gson.");
    }

    private ClassLoader wrapPluginLibraryLoader(ClassLoader raw, String cacheName) {
        if (raw == null) return raw;
        if (!(raw instanceof URLClassLoader urls)) {
            java.util.logging.Logger.getLogger(description.getName()).fine(
                    "Wrapping non-URL Paper library loader " + raw.getClass().getName()
                            + " with LunarArc's concrete plugin mapping compatibility transformer");
            return new TransformingDelegatePluginLibraryClassLoader(
                    raw, this.remapper, this.transformedCacheRoot.resolve(cacheName));
        }

        URL[] libraryUrls = urls.getURLs();
        ClassLoader libraryParent = urls.getParent();
        try { urls.close(); } catch (IOException ignored) {}
        return new TransformingPluginLibraryClassLoader(
                libraryUrls,
                libraryParent,
                this.remapper,
                this.transformedCacheRoot.resolve(cacheName));
    }

    private static ClassLoader compatibilityLibraryLoader() {
        ClassLoader existing = compatibilityLibraryLoader;
        if (existing != null) return existing;

        synchronized (PluginClassLoader.class) {
            if (compatibilityLibraryLoader != null) return compatibilityLibraryLoader;

            String resourceName = "META-INF/lunararc-libs/commons-lang-2.6.jar";
            ClassLoader owner = PluginClassLoader.class.getClassLoader();
            try (InputStream input = owner.getResourceAsStream(resourceName)) {
                if (input == null) return null;

                Path directory = io.lunararcdevs.lunararc.common.LunarArcPaths.pluginLibraries().resolve("compat");
                Files.createDirectories(directory);
                Path library = directory.resolve("commons-lang-2.6.jar");
                if (!Files.isRegularFile(library) || Files.size(library) == 0L) {
                    Path temp = library.resolveSibling(library.getFileName() + ".tmp");
                    Files.copy(input, temp, StandardCopyOption.REPLACE_EXISTING);
                    try {
                        Files.move(temp, library, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                    } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
                        Files.move(temp, library, StandardCopyOption.REPLACE_EXISTING);
                    }
                }

                compatibilityLibraryLoader = new URLClassLoader(
                        new URL[]{library.toUri().toURL()},
                        owner
                );
                return compatibilityLibraryLoader;
            } catch (IOException error) {
                java.util.logging.Logger.getLogger("LunarArc").warning(
                        "Could not expose bundled plugin compatibility libraries: " + error.getMessage());
                return null;
            }
        }
    }

    public Class<?> findClassFromOwnSpace(String name) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> loaded = findLoadedClass(name);
            if (loaded != null && loaded.getClassLoader() == this) return loaded;

            String path = name.replace('.', '/').concat(".class");
            URL own = super.findResource(path);
            if (own != null) {
                try (InputStream is = own.openStream()) {
                    byte[] original = is.readAllBytes();
                    byte[] bytecode = loadOrCreateTransformedClass(path, name, original);
                    String pkg = name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : null;
                    if (pkg != null && getDefinedPackage(pkg) == null) {
                        definePackage(pkg, null, null, null, null, null, null, null);
                    }
                    CodeSource cs = new CodeSource(file.toURI().toURL(), (Certificate[]) null);
                    ProtectionDomain pd = new ProtectionDomain(cs, null, this, null);
                    Class<?> result = defineClass(name, bytecode, 0, bytecode.length, pd);
                    classes.put(name, result);
                    return result;
                } catch (IOException error) {
                    throw new ClassNotFoundException(name, error);
                }
            }
            if (paperLibraryLoader instanceof TransformingPluginLibraryClassLoader libraries) {
                try { return libraries.findOwnClass(name); } catch (ClassNotFoundException ignored) {}
            }
            if (libraryLoader instanceof TransformingPluginLibraryClassLoader libraries) {
                try { return libraries.findOwnClass(name); } catch (ClassNotFoundException ignored) {}
            }
            throw new ClassNotFoundException(name);
        }
    }

    public URL findResourceFromOwnSpace(String name) {
        URL resource = super.findResource(name);
        if (resource == null && paperLibraryLoader instanceof TransformingPluginLibraryClassLoader libraries) {
            resource = libraries.findOwnResource(name);
        }
        if (resource == null && libraryLoader instanceof TransformingPluginLibraryClassLoader libraries) {
            resource = libraries.findOwnResource(name);
        }
        return resource;
    }

    public java.util.List<URL> findResourcesFromOwnSpace(String name) throws IOException {
        java.util.LinkedHashSet<URL> resources = new java.util.LinkedHashSet<>();
        addResources(resources, super.findResources(name));
        if (paperLibraryLoader instanceof TransformingPluginLibraryClassLoader libraries) {
            addResources(resources, libraries.findOwnResources(name));
        }
        if (libraryLoader instanceof TransformingPluginLibraryClassLoader libraries) {
            addResources(resources, libraries.findOwnResources(name));
        }
        return java.util.List.copyOf(resources);
    }

    public PluginMappingNamespace getMappingNamespace() {
        return this.mappingNamespace;
    }

    public boolean isNmsRemappingEnabled() {
        return this.remapNms;
    }

    private static boolean isPlatformClass(String name) {
        return name.startsWith("java.")
                || name.startsWith("javax.")
                || name.startsWith("jdk.")
                || name.startsWith("sun.")
                || name.startsWith("org.bukkit.")
                || name.startsWith("com.destroystokyo.paper.")
                || name.startsWith("io.papermc.paper.")
                || name.startsWith("net.kyori.adventure.")
                || name.startsWith("net.kyori.examination.")
                || name.startsWith("com.mojang.")
                || name.startsWith("net.minecraft.")
                // Ambient, not part of the Bukkit API contract, present on every server the same
                // way com.mojang. is - and unlike com.mojang., also present a second time on a
                // hybrid server's mod loader classpath (NeoForge depends on it too). A plugin that
                // never declares it as a library (most don't; it has always just been there) can
                // have two of its own references resolve through two different steps of the
                // six-source scan below, handed two unrelated Class objects for the same name.
                // That is exactly what VerifyError: "Type 'JsonArray' is not assignable to
                // 'JsonElement'" is - not a plugin bug, a same-name-different-loader split, and it
                // took Essentials down in onDisable(). Routing gson through the platform backstop
                // first makes every reference resolve from the same one or two stable sources
                // (parent, then the mod loader) instead of whichever of six answered first; a
                // plugin that ships its own gson under this exact package name was already exposed
                // to this class of bug on any multi-plugin server, hybrid or not, so preferring the
                // platform's copy is the safer default. classload traces which of the two
                // deterministic sources actually answers, same as it does for everything else here.
                || name.startsWith("com.google.gson.");
    }

    @Override
    public URL getResource(String name) {
        URL url = findResource(name);
        if (url == null) url = getParent().getResource(name);
        if (url == null) {
            url = this.pluginLoader.getClassSpace().findDependencyResource(
                    this, name, visibleDependencies(), !this.paperPlugin);
        }
        if (url == null && paperLibraryLoader != null) url = paperLibraryLoader.getResource(name);
        if (url == null && libraryLoader != null) url = libraryLoader.getResource(name);
        if (url == null) {
            ClassLoader compatibility = compatibilityLibraryLoader();
            if (compatibility != null) url = compatibility.getResource(name);
        }
        return url;
    }

    @Override
    public java.util.Enumeration<URL> getResources(String name) throws IOException {
        java.util.LinkedHashSet<URL> resources = new java.util.LinkedHashSet<>();
        addResources(resources, super.findResources(name));
        addResources(resources, getParent().getResources(name));
        this.pluginLoader.getClassSpace().addDependencyResources(
                resources, this, name, visibleDependencies(), !this.paperPlugin);
        if (paperLibraryLoader != null) addResources(resources, paperLibraryLoader.getResources(name));
        if (libraryLoader != null) addResources(resources, libraryLoader.getResources(name));
        ClassLoader compatibility = compatibilityLibraryLoader();
        if (compatibility != null) addResources(resources, compatibility.getResources(name));
        return java.util.Collections.enumeration(resources);
    }

    private static void addResources(java.util.Set<URL> target, java.util.Enumeration<URL> source) {
        while (source.hasMoreElements()) target.add(source.nextElement());
    }

    private Set<String> visibleDependencies() {
        if (this.paperPlugin) {
            return this.joinedPaperDependencies;
        }
        Set<String> dependencies = new LinkedHashSet<>();
        dependencies.addAll(description.getDepend());
        dependencies.addAll(description.getSoftDepend());
        return dependencies;
    }

    private static boolean isPaperPlugin(File pluginFile) {
        try (java.util.jar.JarFile jar = new java.util.jar.JarFile(pluginFile)) {
            return jar.getJarEntry("paper-plugin.yml") != null;
        } catch (IOException error) {
            throw new IllegalStateException("Could not inspect plugin descriptor for " + pluginFile.getName(), error);
        }
    }

    private static String normalize(String name) {
        return name.replace(' ', '_').toLowerCase(java.util.Locale.ROOT);
    }

    public JavaPlugin getPluginInstance() {
        return plugin;
    }

    @Override
    public io.papermc.paper.plugin.configuration.PluginMeta getConfiguration() {
        return this.description;
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve, boolean checkGlobal, boolean checkLibraries)
            throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> found = findClass(name, checkGlobal);
            if (resolve) resolveClass(found);
            return found;
        }
    }

    @Override
    public void init(JavaPlugin plugin) {
        initialize(plugin);
    }

    @Override
    public JavaPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public io.papermc.paper.plugin.provider.classloader.PluginClassLoaderGroup getGroup() {
        return this.group;
    }

    public void setGroup(io.papermc.paper.plugin.provider.classloader.PluginClassLoaderGroup group) {
        this.group = group;
    }

    LunarArcPluginLoader getPluginLoaderInstance() {
        return pluginLoader;
    }

    public JavaPlugin createPluginMain() throws ReflectiveOperationException {
        Class<?> mainClass = findClass(description.getMain(), false);
        Class<? extends JavaPlugin> pluginClass = mainClass.asSubclass(JavaPlugin.class);
        java.lang.reflect.Constructor<? extends JavaPlugin> constructor = pluginClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    public synchronized void initialize(JavaPlugin plugin) {
        if (this.plugin != null) throw new IllegalStateException("Plugin already initialized!");

        this.plugin = plugin;
        // Real Paper's PluginDescriptionFile implements PluginMeta directly, so classic
        // (plugin.yml) plugins get the same object for both parameters. Wrapping it in
        // LunarArcPluginMeta here instead broke every `getPluginMeta() instanceof
        // PluginDescriptionFile` check in the codebase (e.g. PaperPluginInstanceManager's
        // enablePlugin(), which uses that check to decide whether to register plugin.yml
        // "commands:" into the CommandMap) - meaning no classic plugin ever got its declared
        // commands registered, so getCommand() always returned null for them (crashed Vault).
        plugin.init(
                pluginLoader.getServerInstance(),
                description,
                dataFolder,
                file,
                this,
                description,
                io.lunararcdevs.lunararc.common.server.LunarArcLogger.getLogger(description.getName())
        );
    }

    public static void shutdownSharedLoaders() {
        synchronized (PluginClassLoader.class) {
            ClassLoader current = compatibilityLibraryLoader;
            compatibilityLibraryLoader = null;
            if (current instanceof java.io.Closeable closeable) {
                try { closeable.close(); } catch (IOException ignored) {}
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (!closed.compareAndSet(false, true)) return;
        IOException failure = null;
        try {
            super.close();
        } catch (IOException error) {
            failure = error;
        } finally {

            this.pluginLoader.getClassSpace().unregister(this);
            if (paperLibraryLoader instanceof java.io.Closeable closeable) {
                try { closeable.close(); } catch (IOException error) { if (failure == null) failure = error; }
            }
            if (libraryLoader instanceof java.io.Closeable closeable) {
                try { closeable.close(); } catch (IOException error) { if (failure == null) failure = error; }
            }
            classes.clear();
            plugin = null;
        }
        if (failure != null) throw failure;
    }
}
