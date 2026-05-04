package io.ampznetwork.lunararc.quilt.server;

import io.ampznetwork.lunararc.common.server.LunarArcServer;
import net.minecraft.server.MinecraftServer;

public class QuiltServer extends LunarArcServer {
    public QuiltServer(MinecraftServer server) {
        super(server);
    }

    @Override
    public String getName() {
        return "LunarArc-Quilt";
    }
}
