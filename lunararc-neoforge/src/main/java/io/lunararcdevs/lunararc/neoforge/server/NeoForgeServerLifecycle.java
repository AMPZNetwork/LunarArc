package io.lunararcdevs.lunararc.neoforge.server;

import io.lunararcdevs.lunararc.common.mod.server.LunarArcServer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

public final class NeoForgeServerLifecycle {
    private NeoForgeServerLifecycle() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(NeoForgeServerLifecycle::onServerStarting);
        NeoForge.EVENT_BUS.addListener(NeoForgeServerLifecycle::onServerStopping);
    }

    private static void onServerStarting(ServerStartingEvent event) {
        LunarArcServer.attach(event.getServer());
    }

    private static void onServerStopping(ServerStoppingEvent event) {
        LunarArcServer.detach(event.getServer());
    }
}
