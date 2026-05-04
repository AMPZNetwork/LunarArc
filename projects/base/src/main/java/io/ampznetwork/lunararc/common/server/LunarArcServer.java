package io.ampznetwork.lunararc.common.server;

import net.minecraft.server.MinecraftServer;
import io.ampznetwork.lunararc.common.stubs.org.bukkit.craftbukkit.v1_21_R1.CraftServer;

public abstract class LunarArcServer extends CraftServer {
    public LunarArcServer(MinecraftServer server) {
        super(server, server.getPlayerList());
    }

    @Override
    public abstract String getName();
}
