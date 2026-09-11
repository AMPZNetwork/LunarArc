package io.lunararcdevs.lunararc.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * LunarArc-owned runtime/install state. Normal server content stays in the
 * standard locations (plugins, mods, worlds, libraries, logs). Generated
 * implementation state lives under .lunararc so it can be managed or removed
 * independently without touching user data.
 */
public final class LunarArcPaths {
    private LunarArcPaths() {}

    public static void initialize() {
        ensure(home());
        ensure(cache());
        ensure(transformedPlugins());
        ensure(mappings());
        ensure(runtime());
        ensure(state());
        ensure(pluginLibraries());
    }

    public static Path home() {
        String configured = System.getProperty("lunararc.home");
        Path path = configured == null || configured.isBlank()
                ? Path.of(".lunararc").toAbsolutePath().normalize()
                : Path.of(configured).toAbsolutePath().normalize();
        return ensure(path);
    }

    public static Path pluginLibraries() {
        return ensure(home().resolve("libraries/plugins"));
    }

    public static Path transformedPlugins() {
        return ensure(cache().resolve("transformed-plugins"));
    }

    public static Path mappings() {
        return ensure(home().resolve("mappings"));
    }

    public static Path cache() {
        return ensure(home().resolve("cache"));
    }

    public static Path runtime() {
        return ensure(home().resolve("runtime"));
    }

    public static Path state() {
        return ensure(home().resolve("state"));
    }

    /** Loader-private transient state without duplicating the loader's own libraries. */
    public static Path platformRuntime(String platform) {
        String safe = platform == null || platform.isBlank() ? "unknown"
                : platform.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "_");
        return ensure(runtime().resolve(safe));
    }

    private static Path ensure(Path path) {
        try {
            Files.createDirectories(path);
            return path;
        } catch (IOException error) {
            throw new IllegalStateException("Could not create LunarArc runtime directory " + path, error);
        }
    }
}
