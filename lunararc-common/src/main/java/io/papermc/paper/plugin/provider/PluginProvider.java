package io.papermc.paper.plugin.provider;

import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.provider.configuration.LoadOrderConfiguration;
import io.papermc.paper.plugin.provider.entrypoint.DependencyContext;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

@ApiStatus.Internal
public interface PluginProvider<T> {

    @NotNull
    Path getSource();

    default Path getFileName() {
        return this.getSource().getFileName();
    }

    default Path getParentSource() {
        return this.getSource().getParent();
    }

    JarFile file();

    T createInstance();

    PluginMeta getMeta();

    ComponentLogger getLogger();

    LoadOrderConfiguration createConfiguration(@NotNull Map<String, PluginProvider<?>> toLoad);

    // Returns a list of missing dependencies
    List<String> validateDependencies(@NotNull DependencyContext context);

}
