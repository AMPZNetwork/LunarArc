package io.papermc.paper.plugin.manager;

import io.papermc.paper.plugin.entrypoint.Entrypoint;
import io.papermc.paper.plugin.entrypoint.LaunchEntryPointHandler;
import io.papermc.paper.plugin.entrypoint.dependency.MetaDependencyTree;
import io.papermc.paper.plugin.provider.PluginProvider;
import io.papermc.paper.plugin.provider.type.paper.PaperPluginParent;
import io.papermc.paper.plugin.storage.ServerPluginProviderStorage;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class MultiRuntimePluginProviderStorage extends ServerPluginProviderStorage {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MultiRuntimePluginProviderStorage.class);
    private final List<JavaPlugin> provided = new ArrayList<>();

    private final MetaDependencyTree dependencyTree;

    MultiRuntimePluginProviderStorage(MetaDependencyTree dependencyTree) {
        this.dependencyTree = dependencyTree;
    }

    /**
     * Real Paper refuses paper-plugin.yml plugins here because this storage only ever serves the
     * runtime {@code PluginManager#loadPlugins} API, by which point boot's BOOTSTRAPPER phase is
     * long past. LunarArc reaches the same code at boot as well - it cannot use Paper's early
     * {@code LaunchEntryPointHandler} path, so discovery, BOOTSTRAPPER and PLUGIN are all entered
     * from {@code CraftServer#loadPlugins} - and there the refusal is wrong: the bootstrap has
     * just run, so the plugin is loadable. Left on for the runtime path, where Paper's reasoning
     * still holds, and overridden by {@link BootPluginProviderStorage}.
     */
    protected boolean skipsPaperPlugins() {
        return true;
    }

    @Override
    public void register(PluginProvider<JavaPlugin> provider) {
        if (this.skipsPaperPlugins() && provider instanceof PaperPluginParent.PaperServerPluginProvider) {
            LOGGER.warn("Skipping loading of paper plugin requested from SimplePluginManager.");
            return;
        }
        super.register(provider);
        /*
        Register the provider into the server entrypoint, this allows it to show in /plugins correctly. Generally it might be better in the future to make a separate storage,
         as putting it into the entrypoint handlers doesn't make much sense.
         */
        LaunchEntryPointHandler.INSTANCE.register(Entrypoint.PLUGIN, provider);
    }

    @Override
    public void processProvided(PluginProvider<JavaPlugin> provider, JavaPlugin provided) {
        super.processProvided(provider, provided);
        this.provided.add(provided);
    }

    @Override
    public boolean throwOnCycle() {
        return false;
    }

    public List<JavaPlugin> getLoaded() {
        return this.provided;
    }

    @Override
    public MetaDependencyTree createDependencyTree() {
        return this.dependencyTree;
    }
}
