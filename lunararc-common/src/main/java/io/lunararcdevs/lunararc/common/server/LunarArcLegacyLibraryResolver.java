package io.lunararcdevs.lunararc.common.server;

import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.bukkit.plugin.PluginDescriptionFile;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.artifact.JavaScopes;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class LunarArcLegacyLibraryResolver {
    private static final long TIMEOUT_SECONDS = 120;

    private LunarArcLegacyLibraryResolver() {}

    public static ClassLoader create(PluginDescriptionFile description, ClassLoader parent) {
        Objects.requireNonNull(description, "description");
        List<String> declared = description.getLibraries();
        if (declared == null || declared.isEmpty()) return null;

        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder(
                "central", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build());
        int count = 0;
        for (String coordinates : declared) {
            if (coordinates == null || coordinates.isBlank()) continue;
            try {
                resolver.addDependency(new Dependency(new DefaultArtifact(coordinates.trim()), JavaScopes.RUNTIME));
                count++;
            } catch (RuntimeException malformed) {
                throw new IllegalArgumentException("Invalid library coordinates '" + coordinates
                        + "' declared by " + description.getName(), malformed);
            }
        }
        if (count == 0) return null;

        LinkedHashSet<Path> paths = new LinkedHashSet<>();
        ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "lunararc-library-resolver-" + description.getName());
            thread.setDaemon(true);
            return thread;
        });
        try {
            Future<Void> future = executor.submit(() -> {
                resolver.register(library -> {
                    Path path = Objects.requireNonNull(library, "library").toAbsolutePath().normalize();
                    if (!Files.isRegularFile(path)) {
                        throw new IllegalArgumentException("Resolved plugin library is not a file: " + path);
                    }
                    paths.add(path);
                });
                return null;
            });
            try {
                future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (TimeoutException timedOut) {
                future.cancel(true);
                throw new IllegalStateException("Timed out resolving libraries for " + description.getName()
                        + " after " + TIMEOUT_SECONDS + "s - the remote repository never responded. Check the"
                        + " library coordinates the plugin declares, or whether this server can reach it.", timedOut);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while resolving libraries for "
                        + description.getName(), interrupted);
            } catch (ExecutionException failed) {
                Throwable cause = failed.getCause() != null ? failed.getCause() : failed;
                throw new IllegalStateException("Could not resolve libraries for " + description.getName(), cause);
            }
        } finally {
            executor.shutdownNow();
        }
        if (paths.isEmpty()) return null;
        URL[] urls = paths.stream().map(path -> {
            try {
                return path.toUri().toURL();
            } catch (java.net.MalformedURLException impossible) {
                throw new IllegalArgumentException(impossible);
            }
        }).toArray(URL[]::new);
        return new URLClassLoader(urls, parent);
    }
}
