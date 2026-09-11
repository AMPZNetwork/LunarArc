package io.papermc.paper.plugin.entrypoint.strategy;

import io.papermc.paper.plugin.entrypoint.dependency.MetaDependencyTree;
import io.papermc.paper.plugin.provider.PluginProvider;

import java.util.List;

public interface ProviderLoadingStrategy<P> {

    List<ProviderPair<P>> loadProviders(List<PluginProvider<P>> providers, MetaDependencyTree dependencyTree);

    record ProviderPair<P>(PluginProvider<P> provider, P provided) {

    }
}
