package io.lunararcdevs.lunararc.common.server;

import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.PluginClassLoader;

import java.io.File;
import java.io.IOException;

public final class LunarArcPluginProvider implements AutoCloseable {
    private final LunarArcPluginLoader loader;
    private final File source;
    private final PluginDescriptionFile description;
    private final File dataDirectory;
    private final boolean paperPlugin;
    private final java.util.List<LunarArcPaperPluginSupport.ServerDependency> bootstrapDependencies;
    private final java.util.List<LunarArcPaperPluginSupport.ServerDependency> serverDependencies;
    private io.papermc.paper.plugin.bootstrap.PluginBootstrap bootstrapper;
    private LunarArcBootstrapContext bootstrapContext;
    private boolean bootstrapped;
    private PluginClassLoader classLoader;
    private JavaPlugin plugin;
    private boolean committed;

    public LunarArcPluginProvider(LunarArcPluginLoader loader, File source,
                                  PluginDescriptionFile description, File dataDirectory) {
        this.loader = java.util.Objects.requireNonNull(loader, "loader");
        this.source = java.util.Objects.requireNonNull(source, "source");
        this.description = java.util.Objects.requireNonNull(description, "description");
        this.dataDirectory = java.util.Objects.requireNonNull(dataDirectory, "dataDirectory");
        this.paperPlugin = LunarArcPaperPluginSupport.isPaperPlugin(source);
        this.bootstrapDependencies = this.paperPlugin
                ? LunarArcPaperPluginSupport.bootstrapDependencies(source)
                : java.util.List.of();
        this.serverDependencies = this.paperPlugin
                ? LunarArcPaperPluginSupport.serverDependencies(source)
                : java.util.List.of();
    }

    /** Discover and validate immutable provider metadata before dependency sorting. */
    public static LunarArcPluginProvider discover(LunarArcPluginLoader loader, File source)
            throws InvalidPluginException {
        java.util.Objects.requireNonNull(loader, "loader");
        java.util.Objects.requireNonNull(source, "source");
        if (!source.isFile()) {
            throw new InvalidPluginException(new java.io.FileNotFoundException(source.getPath()));
        }
        final PluginDescriptionFile description;
        try {
            description = loader.getPluginDescription(source);
        } catch (org.bukkit.plugin.InvalidDescriptionException error) {
            throw new InvalidPluginException(error);
        }
        io.lunararcdevs.lunararc.common.config.IncompatibleList.Entry incompatible =
                io.lunararcdevs.lunararc.common.config.IncompatibleList.check(
                        description.getMain(), description.getVersion());
        if (incompatible != null) {
            io.lunararcdevs.lunararc.common.config.IncompatibleList.reportPlugin(
                    description.getMain(), description.getName(), description.getVersion(), incompatible);
        }
        // Paper/CraftBukkit validates api-version here and initializes the legacy
        // Bukkit compatibility layer for descriptors without api-version. Keeping
        // this at provider discovery makes every load path share the same gate.
        loader.getServerInstance().getUnsafe().checkSupported(description);
        return new LunarArcPluginProvider(loader, source, description,
                new File(source.getParentFile(), description.getName()));
    }

    public File source() { return source; }
    public PluginDescriptionFile description() { return description; }
    public File dataDirectory() { return dataDirectory; }
    public boolean paperPlugin() { return paperPlugin; }
    public java.util.List<LunarArcPaperPluginSupport.ServerDependency> bootstrapDependencies() { return bootstrapDependencies; }
    public java.util.List<LunarArcPaperPluginSupport.ServerDependency> serverDependencies() { return serverDependencies; }

    /**
     * Prepare and publish this provider's configured classloader without executing
     * plugin code. Batch loading uses this phase for every viable provider first,
     * so Paper join-classpath dependencies are visible even when their bootstrap
     * execution order is later in the graph.
     */
    public PluginClassLoader prepareClassLoader() throws InvalidPluginException {
        if (this.classLoader != null) return this.classLoader;
        try {
            java.nio.file.Files.createDirectories(dataDirectory.toPath());
            ClassLoader parent = loader.getClass().getClassLoader();
            try {
                ClassLoader libraryLoader = description.getLibraries().isEmpty() ? null
                        : new org.bukkit.plugin.java.LibraryLoader(loader.getServerInstance().getLogger())
                                .createLoader(description);
                if (libraryLoader != null) parent = libraryLoader;
            } catch (Throwable libraryError) {
                loader.getServerInstance().getLogger().log(java.util.logging.Level.WARNING,
                        "[LunarArc] Failed to resolve declared libraries for " + description.getName()
                                + " — continuing without them", libraryError);
            }
            this.classLoader = new PluginClassLoader(
                    loader,
                    parent,
                    description,
                    dataDirectory,
                    source);
            // Same assignment SpigotPluginProvider makes: the classloader's Paper group resolves
            // shared libraries through dependencyContext, so it has to be set before the plugin's
            // own classes start loading through that group.
            this.classLoader.dependencyContext = io.papermc.paper.plugin.manager.PaperPluginManagerImpl.getInstance();
            loader.getServerInstance().getLogger().fine("[LunarArc] " + description.getName() + " mappings="
                    + classLoader.getMappingNamespace().name().toLowerCase(java.util.Locale.ROOT)
                    + (classLoader.isNmsRemappingEnabled()
                    ? " (Spigot -> Mojang remap enabled)"
                    : " (Mojang runtime, remap skipped)"));
            return this.classLoader;
        } catch (Throwable error) {
            rollback(error);
            Throwable cause = error instanceof java.lang.reflect.InvocationTargetException ite && ite.getCause() != null
                    ? ite.getCause() : error;
            if (cause instanceof Error fatal) throw fatal;
            if (cause instanceof InvalidPluginException invalid) throw invalid;
            throw new InvalidPluginException(cause);
        }
    }

    /** Execute only the Paper bootstrap phase. Safe to call more than once. */
    public void bootstrap() throws InvalidPluginException {
        if (this.bootstrapped || !this.paperPlugin) {
            this.bootstrapped = true;
            return;
        }
        try {
            PluginClassLoader prepared = prepareClassLoader();
            Thread thread = Thread.currentThread();
            ClassLoader previous = thread.getContextClassLoader();
            LunarArcPaperPluginSupport.BootstrapHandle handle;
            try {
                thread.setContextClassLoader(prepared);
                handle = LunarArcPaperPluginSupport.bootstrap(prepared, source, description, dataDirectory);
            } finally {
                thread.setContextClassLoader(previous);
            }
            if (handle != null) {
                this.bootstrapper = handle.bootstrapper();
                this.bootstrapContext = handle.context();
            }
            this.bootstrapped = true;
        } catch (Throwable error) {
            rollback(error);
            rethrow(error);
        }
    }

    public JavaPlugin create() throws InvalidPluginException {
        if (plugin != null) return plugin;
        try {
            PluginClassLoader prepared = prepareClassLoader();
            bootstrap();
            Thread thread = Thread.currentThread();
            ClassLoader previous = thread.getContextClassLoader();
            try {
                thread.setContextClassLoader(prepared);
                this.plugin = this.bootstrapper != null
                        ? LunarArcPaperPluginSupport.createPlugin(this.bootstrapper, this.bootstrapContext, description)
                        : prepared.createPluginMain();
            } finally {
                thread.setContextClassLoader(previous);
            }
            verifyPluginInstance(this.plugin);
            return this.plugin;
        } catch (Throwable error) {
            rollback(error);
            rethrow(error);
            throw new AssertionError("unreachable");
        }
    }

    private static void rethrow(Throwable error) throws InvalidPluginException {
        Throwable cause = error instanceof java.lang.reflect.InvocationTargetException ite && ite.getCause() != null
                ? ite.getCause() : error;
        if (cause instanceof Error fatal) throw fatal;
        if (cause instanceof InvalidPluginException invalid) throw invalid;
        if (cause instanceof RuntimeException runtime) throw runtime;
        throw new InvalidPluginException(cause);
    }

    public void commit() {
        if (plugin == null) throw new IllegalStateException("Cannot commit an uncreated plugin provider");
        this.committed = true;
    }

    public JavaPlugin plugin() {
        return plugin;
    }

    private void verifyPluginInstance(JavaPlugin instance) {
        if (instance == null) {
            throw new IllegalStateException("Plugin provider returned null for " + description.getName());
        }
        if (instance.getClass().getClassLoader() != classLoader) {
            throw new IllegalStateException("Plugin " + description.getName()
                    + " was constructed by an unexpected classloader: "
                    + instance.getClass().getClassLoader());
        }
        if (classLoader.getPluginInstance() != instance) {
            throw new IllegalStateException("Plugin " + description.getName()
                    + " did not initialize through its configured PluginClassLoader");
        }
    }

    private void rollback(Throwable primary) {
        if (this.bootstrapContext != null) {
            LunarArcLifecycleEventRunner.unregisterAllOwner(this.bootstrapContext);
            this.bootstrapContext = null;
            this.bootstrapper = null;
            this.bootstrapped = false;
        }
        if (classLoader == null) return;
        try {
            classLoader.close();
        } catch (IOException closeError) {
            primary.addSuppressed(closeError);
        }
        classLoader = null;
        plugin = null;
    }

    @Override
    public void close() throws IOException {
        if (!committed && classLoader != null) {
            classLoader.close();
            classLoader = null;
            plugin = null;
        }
    }
}
