package io.ampznetwork.lunararc.forge.server;

import io.ampznetwork.lunararc.common.server.LunarArcServer;
import net.minecraft.server.MinecraftServer;

public class ForgeServer extends LunarArcServer {
    public ForgeServer(MinecraftServer server) {
        super(server);
    }

    @Override
    public String getName() {
        return "LunarArc-Forge";
    }
}
