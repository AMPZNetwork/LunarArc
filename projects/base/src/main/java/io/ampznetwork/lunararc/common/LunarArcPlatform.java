package io.ampznetwork.lunararc.common;

import org.bukkit.craftbukkit.v1_21_R1.CraftServer;

public class LunarArcPlatform {
    private static CraftServer server;
    public static String LATEST_VERSION = null;
    public static String UPDATE_URL = null;

    public static void setServer(CraftServer server) {
        LunarArcPlatform.server = server;
    }

    public static CraftServer getServer() {
        return server;
    }
}
