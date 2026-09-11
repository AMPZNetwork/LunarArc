package io.papermc.paper.plugin.provider.configuration;

import io.papermc.paper.plugin.configuration.PluginMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface LoadOrderConfiguration {

    @NotNull
    List<String> getLoadBefore();

    @NotNull
    List<String> getLoadAfter();

    @NotNull
    PluginMeta getMeta();
}
