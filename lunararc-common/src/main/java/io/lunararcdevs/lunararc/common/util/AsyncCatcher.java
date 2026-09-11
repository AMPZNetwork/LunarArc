package io.lunararcdevs.lunararc.common.util;

import io.lunararcdevs.lunararc.common.LunarArcServerAccess;
import net.minecraft.server.MinecraftServer;

public final class AsyncCatcher {
    public static boolean enabled = true;

    private AsyncCatcher() {}

    public static void catchOp(String reason) {
        if (!enabled) {
            return;
        }

        MinecraftServer server = LunarArcServerAccess.getMinecraftServer();
        if (!server.isSameThread()) {
            throw new IllegalStateException(
                    "Asynchronous " + reason + "! This is a plugin/mod bug and can cause severe corruption.");
        }
    }
}
