package io.lunararcdevs.lunararc.common.mod.util;

import net.minecraft.server.WorldLoader;

/** One-shot capture used while the vanilla WorldLoader constructs the MinecraftServer. */
public final class LunarArcWorldLoaderCapture {
    private static WorldLoader.DataLoadContext dataLoadContext;

    private LunarArcWorldLoaderCapture() {
    }

    public static synchronized void capture(WorldLoader.DataLoadContext context) {
        dataLoadContext = java.util.Objects.requireNonNull(context, "context");
    }

    public static synchronized WorldLoader.DataLoadContext take() {
        WorldLoader.DataLoadContext context = java.util.Objects.requireNonNull(
                dataLoadContext, "WorldLoader.DataLoadContext was not captured");
        dataLoadContext = null;
        return context;
    }
}
