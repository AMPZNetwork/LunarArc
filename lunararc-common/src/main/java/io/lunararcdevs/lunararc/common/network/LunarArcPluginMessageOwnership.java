package io.lunararcdevs.lunararc.common.network;

import net.minecraft.resources.ResourceLocation;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Records plugin payload ids whose inbound delivery is owned by a loader-native
 * networking API. The common packet hook consults this registry only to avoid a
 * second Bukkit dispatch; it never replaces, wraps, or proxies loader networking.
 */
public final class LunarArcPluginMessageOwnership {
    private static final Set<ResourceLocation> NATIVE_INBOUND = ConcurrentHashMap.newKeySet();

    private LunarArcPluginMessageOwnership() {}

    public static void markNativeInbound(ResourceLocation id) {
        if (id != null) NATIVE_INBOUND.add(id);
    }

    public static boolean isNativeInbound(ResourceLocation id) {
        return id != null && NATIVE_INBOUND.contains(id);
    }

    /** Final server-shutdown cleanup for same-JVM restarts/test harnesses. */
    public static void clear() {
        NATIVE_INBOUND.clear();
    }
}
