package io.lunararcdevs.lunararc.common.server;

import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.nio.file.Path;
import java.util.Objects;

/** Concrete immutable provider context used by Paper plugin loaders. */
public final class LunarArcPluginProviderContext implements PluginProviderContext {
    private final PluginMeta configuration;
    private final Path dataDirectory;
    private final Path pluginSource;
    private final ComponentLogger logger;

    public LunarArcPluginProviderContext(PluginMeta configuration, Path dataDirectory, Path pluginSource) {
        this.configuration = Objects.requireNonNull(configuration, "configuration");
        this.dataDirectory = Objects.requireNonNull(dataDirectory, "dataDirectory");
        this.pluginSource = Objects.requireNonNull(pluginSource, "pluginSource");
        this.logger = ComponentLogger.logger(configuration.getLoggerPrefix() != null
                ? configuration.getLoggerPrefix() : configuration.getName());
    }

    @Override public PluginMeta getConfiguration() { return configuration; }
    @Override public Path getDataDirectory() { return dataDirectory; }
    @Override public ComponentLogger getLogger() { return logger; }
    @Override public Path getPluginSource() { return pluginSource; }
}
