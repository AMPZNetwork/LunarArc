package io.lunararcdevs.lunararc.quilt.server;

import io.lunararcdevs.lunararc.common.mod.server.LunarArcServer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class QuiltServerLifecycle {
    private QuiltServerLifecycle() {}

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(LunarArcServer::attach);
        ServerLifecycleEvents.SERVER_STOPPED.register(LunarArcServer::detach);
    }
}
