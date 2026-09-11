package io.lunararcdevs.lunararc.common.server;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.PluginClassLoader;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/** Paper-plugin metadata helpers. */
public final class LunarArcPaperPluginSupport {
    private LunarArcPaperPluginSupport() {
    }


    public record ServerDependency(String name, String load, boolean required, boolean joinClasspath) { }

    public static java.util.List<ServerDependency> bootstrapDependencies(File pluginFile) {
        return dependencies(pluginFile, "bootstrap");
    }

    public static java.util.List<ServerDependency> serverDependencies(File pluginFile) {
        return dependencies(pluginFile, "server");
    }

    private static java.util.List<ServerDependency> dependencies(File pluginFile, String phase) {
        Map<String, Object> config = readPaperConfig(pluginFile);
        Object dependenciesValue = config.get("dependencies");
        if (!(dependenciesValue instanceof Map<?, ?> dependencies)) return java.util.List.of();
        Object phaseValue = dependencies.get(phase);
        if (!(phaseValue instanceof Map<?, ?> phaseDependencies)) return java.util.List.of();
        java.util.ArrayList<ServerDependency> result = new java.util.ArrayList<>();
        for (Map.Entry<?, ?> entry : phaseDependencies.entrySet()) {
            String name = String.valueOf(entry.getKey()).trim();
            if (name.isEmpty()) continue;
            String load = "OMIT";
            boolean required = true;
            boolean joinClasspath = true;
            if (entry.getValue() instanceof Map<?, ?> dependency) {
                Object value = dependency.get("load");
                if (value != null) load = String.valueOf(value).trim().toUpperCase(java.util.Locale.ROOT);
                if (!load.equals("BEFORE") && !load.equals("AFTER") && !load.equals("OMIT")) {
                    throw new IllegalArgumentException("Invalid Paper dependency load order '" + load
                            + "' for " + name + " in " + pluginFile.getName());
                }
                value = dependency.get("required");
                if (value != null) required = Boolean.parseBoolean(String.valueOf(value));
                value = dependency.get("join-classpath");
                if (value != null) joinClasspath = Boolean.parseBoolean(String.valueOf(value));
            }
            result.add(new ServerDependency(name, load, required, joinClasspath));
        }
        return java.util.List.copyOf(result);
    }

    public static java.util.Set<String> joinedDependencies(File pluginFile) {
        java.util.LinkedHashSet<String> result = new java.util.LinkedHashSet<>();
        for (ServerDependency dependency : bootstrapDependencies(pluginFile)) {
            if (dependency.joinClasspath()) result.add(dependency.name());
        }
        for (ServerDependency dependency : serverDependencies(pluginFile)) {
            if (dependency.joinClasspath()) result.add(dependency.name());
        }
        return java.util.Collections.unmodifiableSet(result);
    }

    public static java.util.Set<String> joinedServerDependencies(File pluginFile) {
        java.util.LinkedHashSet<String> result = new java.util.LinkedHashSet<>();
        for (ServerDependency dependency : serverDependencies(pluginFile)) {
            if (dependency.joinClasspath()) result.add(dependency.name());
        }
        return java.util.Collections.unmodifiableSet(result);
    }

    public static boolean isPaperPlugin(File pluginFile) {
        try (JarFile jar = new JarFile(pluginFile)) {
            return jar.getJarEntry("paper-plugin.yml") != null;
        } catch (IOException error) {
            throw new IllegalStateException("Could not inspect plugin descriptor for " + pluginFile.getName(), error);
        }
    }

    public static java.util.Set<String> noJoinClasspathDependencies(File pluginFile) {
        Map<String, Object> config = readPaperConfig(pluginFile);
        Object dependenciesValue = config.get("dependencies");
        if (!(dependenciesValue instanceof Map<?, ?> dependencies)) return java.util.Set.of();
        Object serverValue = dependencies.get("server");
        if (!(serverValue instanceof Map<?, ?> serverDependencies)) return java.util.Set.of();
        java.util.LinkedHashSet<String> result = new java.util.LinkedHashSet<>();
        for (Map.Entry<?, ?> entry : serverDependencies.entrySet()) {
            if (!(entry.getValue() instanceof Map<?, ?> dependency)) continue;
            Object join = dependency.get("join-classpath");
            if (join != null && !Boolean.parseBoolean(String.valueOf(join))) {
                result.add(normalizePluginName(String.valueOf(entry.getKey())));
            }
        }
        return java.util.Collections.unmodifiableSet(result);
    }

    /**
     * Resolves Paper's custom PluginLoader libraries into a real classloader.
     * The loader class itself is intentionally created in a temporary classloader,
     * matching Paper's documented isolation semantics.
     */
    public static ClassLoader createLibraryLoader(File pluginFile, PluginDescriptionFile description,
                                                   File dataFolder, ClassLoader parent) {
        Map<String, Object> config = readPaperConfig(pluginFile);
        String loaderName = string(config.get("loader"));
        if (loaderName == null) return null;

        LunarArcPluginMeta meta = new LunarArcPluginMeta(description);
        LunarArcPluginProviderContext context = new LunarArcPluginProviderContext(
                meta, dataFolder.toPath(), pluginFile.toPath());
        LunarArcPluginClasspathBuilder builder = new LunarArcPluginClasspathBuilder(context);

        // Run the Paper PluginLoader directly against LunarArc's loader-owned Paper
        // runtime. Resolver API classes are part of that same runtime; there is no
        // secondary LunarArc runtime JAR/classloader. The temporary loader owns only
        // the plugin's PluginLoader implementation class.
        ClassLoader platformRuntime = parent;
        try (java.net.URLClassLoader loaderClassLoader = new java.net.URLClassLoader(
                new java.net.URL[]{pluginFile.toURI().toURL()}, platformRuntime)) {
            Thread thread = Thread.currentThread();
            ClassLoader previous = thread.getContextClassLoader();
            try {
                thread.setContextClassLoader(loaderClassLoader);
                Class<?> raw = loaderClassLoader.loadClass(loaderName);
                Class<? extends io.papermc.paper.plugin.loader.PluginLoader> loaderType =
                        raw.asSubclass(io.papermc.paper.plugin.loader.PluginLoader.class);
                java.lang.reflect.Constructor<? extends io.papermc.paper.plugin.loader.PluginLoader> constructor =
                        loaderType.getDeclaredConstructor();
                constructor.setAccessible(true);
                io.papermc.paper.plugin.loader.PluginLoader pluginLoader = constructor.newInstance();
                pluginLoader.classloader(builder);
            } finally {
                thread.setContextClassLoader(previous);
            }
            java.util.List<java.nio.file.Path> libraries = builder.resolve();
            if (libraries.isEmpty()) return null;
            java.net.URL[] urls = libraries.stream().map(path -> {
                try { return path.toUri().toURL(); }
                catch (java.net.MalformedURLException exception) { throw new IllegalArgumentException(exception); }
            }).toArray(java.net.URL[]::new);
            return new java.net.URLClassLoader(urls, platformRuntime);
        } catch (io.papermc.paper.plugin.loader.library.LibraryLoadingException exception) {
            throw new IllegalStateException("Could not resolve Paper plugin libraries for "
                    + description.getName(), exception);
        } catch (ReflectiveOperationException | IOException exception) {
            throw new IllegalStateException("Could not run Paper PluginLoader " + loaderName
                    + " for " + description.getName(), exception);
        }
    }

    public record BootstrapHandle(
            io.papermc.paper.plugin.bootstrap.PluginBootstrap bootstrapper,
            LunarArcBootstrapContext context) { }

    /** Execute the Paper bootstrap phase without creating the JavaPlugin yet. */
    public static BootstrapHandle bootstrap(PluginClassLoader pluginLoader, File pluginFile,
                                            PluginDescriptionFile description, File dataFolder) {
        Map<String, Object> config = readPaperConfig(pluginFile);
        String bootstrapperName = string(config.get("bootstrapper"));
        if (bootstrapperName == null) return null;

        LunarArcPluginMeta meta = new LunarArcPluginMeta(description);
        LunarArcBootstrapContext context = new LunarArcBootstrapContext(
                meta, dataFolder.toPath(), pluginFile.toPath());
        boolean success = false;
        try {
            Class<?> raw = pluginLoader.findClass(bootstrapperName, false);
            Class<? extends io.papermc.paper.plugin.bootstrap.PluginBootstrap> bootstrapType =
                    raw.asSubclass(io.papermc.paper.plugin.bootstrap.PluginBootstrap.class);
            java.lang.reflect.Constructor<? extends io.papermc.paper.plugin.bootstrap.PluginBootstrap> constructor =
                    bootstrapType.getDeclaredConstructor();
            constructor.setAccessible(true);
            io.papermc.paper.plugin.bootstrap.PluginBootstrap bootstrap = constructor.newInstance();
            try {
                bootstrap.bootstrap(context);
            } finally {
                context.closeRegistration();
            }
            success = true;
            return new BootstrapHandle(bootstrap, context);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not create Paper bootstrapper " + bootstrapperName
                    + " for " + description.getName(), exception);
        } finally {
            if (!success) LunarArcLifecycleEventRunner.unregisterAllOwner(context);
        }
    }

    /** Create the JavaPlugin from an already-completed Paper bootstrap phase. */
    public static JavaPlugin createPlugin(io.papermc.paper.plugin.bootstrap.PluginBootstrap bootstrap,
                                          LunarArcBootstrapContext context,
                                          PluginDescriptionFile description) {
        JavaPlugin plugin = bootstrap.createPlugin(context);
        if (plugin == null) {
            throw new IllegalStateException("Paper bootstrapper returned null for " + description.getName());
        }
        return plugin;
    }

    /** Compatibility helper for direct/single-plugin loads. */
    public static JavaPlugin createBootstrappedPlugin(PluginClassLoader pluginLoader, File pluginFile,
                                                       PluginDescriptionFile description, File dataFolder) {
        BootstrapHandle handle = bootstrap(pluginLoader, pluginFile, description, dataFolder);
        return handle == null ? null : createPlugin(handle.bootstrapper(), handle.context(), description);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> readPaperConfig(File pluginFile) {
        try (JarFile jar = new JarFile(pluginFile)) {
            JarEntry entry = jar.getJarEntry("paper-plugin.yml");
            if (entry == null) return Map.of();
            try (InputStream stream = jar.getInputStream(entry)) {
                Object value = new Yaml().load(stream);
                return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
            }
        } catch (IOException error) {
            throw new IllegalStateException("Could not read paper-plugin.yml from " + pluginFile.getName(), error);
        }
    }

    private static String normalizePluginName(String value) {
        return value.replace(' ', '_').toLowerCase(java.util.Locale.ROOT);
    }

    private static String string(Object value) {
        if (value == null) return null;
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }
}
