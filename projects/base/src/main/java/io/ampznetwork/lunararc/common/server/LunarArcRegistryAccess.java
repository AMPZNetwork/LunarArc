package io.ampznetwork.lunararc.common.server;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;

/**
 * A custom RegistryAccess provider for LunarArc.
 * This is used to resolve NoClassDefFoundError during server bootstrap
 * by providing a bridge to the underlying Paper registries.
 */
public class LunarArcRegistryAccess implements RegistryAccess {
    public static final RegistryAccess INSTANCE = new LunarArcRegistryAccess();

    private LunarArcRegistryAccess() {
    }

    @Override
    public <T extends Keyed> @NotNull Registry<T> getRegistry(@NotNull RegistryKey<T> key) {
        org.bukkit.Server server = org.bukkit.Bukkit.getServer();
        if (server != null) {
            try {
                // Try to find a type class via reflection since RegistryKey doesn't expose it
                // directly in all versions
                java.lang.reflect.Method typeMethod = key.getClass().getMethod("type");
                Class<?> type = (Class<?>) typeMethod.invoke(key);
                return (Registry<T>) server.getRegistry((Class) type);
            } catch (Exception e) {
                // Fallback: search by key name if possible, or return a generic dummy
                return (Registry<T>) server.getRegistry(org.bukkit.Keyed.class);
            }
        }
        throw new UnsupportedOperationException("Server is not yet initialized");
    }

    @Override
    public <T extends Keyed> @NotNull Registry<T> getRegistry(@NotNull Class<T> type) {
        org.bukkit.Server server = org.bukkit.Bukkit.getServer();
        if (server != null) {
            return server.getRegistry(type);
        }
        throw new UnsupportedOperationException("Server is not yet initialized");
    }
}
