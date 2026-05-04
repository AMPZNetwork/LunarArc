package io.ampznetwork.lunararc.common.mixin.core.server;

import net.minecraft.server.players.PlayerList;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {
    @Accessor("playerList")
    PlayerList getPlayerList();

    @Accessor("serverThread")
    Thread getServerThread();
}
