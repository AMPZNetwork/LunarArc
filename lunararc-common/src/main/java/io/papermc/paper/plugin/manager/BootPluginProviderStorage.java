package io.papermc.paper.plugin.manager;

import io.papermc.paper.plugin.entrypoint.dependency.MetaDependencyTree;

public class BootPluginProviderStorage extends MultiRuntimePluginProviderStorage {

    BootPluginProviderStorage(MetaDependencyTree dependencyTree) {
        super(dependencyTree);
    }

    @Override
    protected boolean skipsPaperPlugins() {
        return false;
    }
}
