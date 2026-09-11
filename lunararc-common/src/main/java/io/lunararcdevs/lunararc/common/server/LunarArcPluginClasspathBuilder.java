package io.lunararcdevs.lunararc.common.server;

import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.library.ClassPathLibrary;
import io.papermc.paper.plugin.loader.library.LibraryLoadingException;
import io.papermc.paper.plugin.loader.library.LibraryStore;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/** Concrete Paper 1.21.1 plugin classpath builder. */
public final class LunarArcPluginClasspathBuilder implements PluginClasspathBuilder {
    private final PluginProviderContext context;
    private final List<ClassPathLibrary> libraries = new ArrayList<>();

    public LunarArcPluginClasspathBuilder(PluginProviderContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    @Override
    public PluginClasspathBuilder addLibrary(ClassPathLibrary library) {
        libraries.add(Objects.requireNonNull(library, "library"));
        return this;
    }

    @Override
    public PluginProviderContext getContext() {
        return context;
    }

    public List<Path> resolve() throws LibraryLoadingException {
        LinkedHashSet<Path> paths = new LinkedHashSet<>();
        LibraryStore store = library -> {
            Path path = Objects.requireNonNull(library, "library").toAbsolutePath().normalize();
            if (!Files.isRegularFile(path)) {
                throw new IllegalArgumentException("Paper plugin library is not a file: " + path);
            }
            paths.add(path);
        };
        for (ClassPathLibrary library : libraries) library.register(store);
        return List.copyOf(paths);
    }
}
