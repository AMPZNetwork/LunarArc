package io.ampznetwork.lunararc.common.util;

import net.minecraft.server.MinecraftServer;

/**
 * Utility to catch operations that must be performed on the main server thread.
 * This prevents data corruption and hard-to-debug crashes caused by
 * asynchronous
 * calls from plugins.
 */
public class AsyncCatcher {
    public static boolean enabled = true;

    public static void catchOp(String reason) {
        if (!enabled)
            return;

        org.bukkit.craftbukkit.v1_21_R1.CraftServer server = (org.bukkit.craftbukkit.v1_21_R1.CraftServer) io.ampznetwork.lunararc.common.LunarArcPlatform
                .getServer();
        if (server == null)
            return;

        net.minecraft.server.MinecraftServer console = server.getServer();
        if (console == null)
            return;

        Thread runningThread = ((io.ampznetwork.lunararc.common.mixin.core.server.MinecraftServerAccessor) console)
                .getServerThread();
        if (runningThread != null && !runningThread.equals(Thread.currentThread())) {
            throw new IllegalStateException(
                    "Asynchronous " + reason + "! This is a plugin/mod bug and can cause severe corruption.");
        }
    }
}
