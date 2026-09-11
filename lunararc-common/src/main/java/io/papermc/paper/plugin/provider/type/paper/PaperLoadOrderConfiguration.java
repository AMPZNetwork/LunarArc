package io.papermc.paper.plugin.provider.type.paper;

import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.provider.configuration.LoadOrderConfiguration;
import io.papermc.paper.plugin.provider.configuration.PaperPluginMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PaperLoadOrderConfiguration implements LoadOrderConfiguration {

    private final PaperPluginMeta meta;
    private final List<String> loadBefore;
    private final List<String> loadAfter;

    public PaperLoadOrderConfiguration(PaperPluginMeta meta) {
        this.meta = meta;

        this.loadBefore = this.meta.getLoadBeforePlugins();
        this.loadAfter = this.meta.getLoadAfterPlugins();
    }

    @Override
    public @NotNull List<String> getLoadBefore() {
        return this.loadBefore;
    }

    @Override
    public @NotNull List<String> getLoadAfter() {
        return this.loadAfter;
    }

    @Override
    public @NotNull PluginMeta getMeta() {
        return this.meta;
    }
}
