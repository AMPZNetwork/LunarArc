package io.papermc.paper.plugin.provider.source;

import io.papermc.paper.plugin.entrypoint.EntrypointHandler;
import java.io.IOException;

public interface ProviderSource<I, C> {

    C prepareContext(I context) throws IOException;

    void registerProviders(EntrypointHandler entrypointHandler, C context) throws Exception;
}
