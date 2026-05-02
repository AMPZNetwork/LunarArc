package io.ampznetwork.lunararc.common;

import io.ampznetwork.lunararc.common.server.LunarArcServer;
import org.bukkit.Bukkit;

public class LunarArcPlatform {
    private static LunarArcServer server;

    public static void setServer(LunarArcServer server) {
        LunarArcPlatform.server = server;
        Bukkit.setServer(server);
    }

    public static LunarArcServer getServer() {
        return server;
    }
}
