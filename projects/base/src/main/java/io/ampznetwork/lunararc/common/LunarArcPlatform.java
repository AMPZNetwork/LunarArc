package io.ampznetwork.lunararc.common;

import org.bukkit.Bukkit;
import io.ampznetwork.lunararc.common.stubs.org.bukkit.craftbukkit.v1_21_R1.CraftServer;

public class LunarArcPlatform {
    private static CraftServer server;

    public static void setServer(CraftServer server) {
        LunarArcPlatform.server = server;
    }

    public static CraftServer getServer() {
        return server;
    }
}
