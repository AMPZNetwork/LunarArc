package io.lunararcdevs.lunararc.fabric.network;

import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import io.lunararcdevs.lunararc.common.bridge.ServerCommonPacketListenerBridge;
import java.util.LinkedHashSet;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.bukkit.entity.Player;

public final class FabricChannelRegistration {
    private FabricChannelRegistration() {}

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerCommonPacketListenerBridge bridge = (ServerCommonPacketListenerBridge) (Object) handler;
            net.minecraft.server.level.ServerPlayer serverPlayer = bridge.lunararc$getPlayer();
            if (serverPlayer == null) return;
            org.bukkit.entity.Entity bukkit = ((EntityBridge) (Object) serverPlayer).lunararc$getBukkitEntity();
            if (!(bukkit instanceof Player player)) return;

            LinkedHashSet<String> channels = new LinkedHashSet<>();
            ServerPlayNetworking.getSendable(handler).forEach(id -> channels.add(id.toString()));
            bridge.lunararc$replaceLoaderPluginChannels(player, channels);
        });
    }
}
