package io.lunararcdevs.lunararc.common.server;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.nio.file.Path;
import java.util.Objects;

/** Concrete Paper 1.21.1 bootstrap context for one plugin. */
public final class LunarArcBootstrapContext implements BootstrapContext {
    private final PluginMeta configuration;
    private final Path dataDirectory;
    private final Path pluginSource;
    private final ComponentLogger logger;
    private final LifecycleEventManager<BootstrapContext> lifecycleManager;
    private volatile boolean registrationOpen = true;

    public LunarArcBootstrapContext(PluginMeta configuration, Path dataDirectory, Path pluginSource) {
        this.configuration = Objects.requireNonNull(configuration, "configuration");
        this.dataDirectory = Objects.requireNonNull(dataDirectory, "dataDirectory");
        this.pluginSource = Objects.requireNonNull(pluginSource, "pluginSource");
        this.logger = ComponentLogger.logger(configuration.getLoggerPrefix() != null
                ? configuration.getLoggerPrefix() : configuration.getName());
        this.lifecycleManager = LunarArcLifecycleEventManager.create(this, () -> this.registrationOpen);
    }

    @Override
    public PluginMeta getConfiguration() {
        return this.configuration;
    }

    @Override
    public PluginMeta getPluginMeta() {
        return this.configuration;
    }

    @Override
    public Path getDataDirectory() {
        return this.dataDirectory;
    }

    @Override
    public ComponentLogger getLogger() {
        return this.logger;
    }

    @Override
    public Path getPluginSource() {
        return this.pluginSource;
    }

    @Override
    public LifecycleEventManager<BootstrapContext> getLifecycleManager() {
        return this.lifecycleManager;
    }

    public void closeRegistration() {
        this.registrationOpen = false;
    }
}
