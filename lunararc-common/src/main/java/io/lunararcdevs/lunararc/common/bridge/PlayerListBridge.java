package io.lunararcdevs.lunararc.common.bridge;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.bukkit.craftbukkit.CraftServer;

import java.net.SocketAddress;

public interface PlayerListBridge {
    CraftServer lunararc$getCraftServer();
    ServerPlayer lunararc$canPlayerLogin(SocketAddress address, GameProfile profile, ServerLoginPacketListenerImpl handler);
    void lunararc$reloadRecipeData();
}
