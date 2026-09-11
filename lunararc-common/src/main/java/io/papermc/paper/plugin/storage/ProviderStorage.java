package io.papermc.paper.plugin.storage;

import io.papermc.paper.plugin.entrypoint.dependency.MetaDependencyTree;
import io.papermc.paper.plugin.provider.PluginProvider;

public interface ProviderStorage<T> {

    void register(PluginProvider<T> provider);

    MetaDependencyTree createDependencyTree();

    void enter();

    Iterable<PluginProvider<T>> getRegisteredProviders();

}
