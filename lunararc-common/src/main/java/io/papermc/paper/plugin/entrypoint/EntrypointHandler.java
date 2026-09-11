package io.papermc.paper.plugin.entrypoint;

import io.papermc.paper.plugin.provider.PluginProvider;

public interface EntrypointHandler {

    <T> void register(Entrypoint<T> entrypoint, PluginProvider<T> provider);

    void enter(Entrypoint<?> entrypoint);
}
