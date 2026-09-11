package io.lunararcdevs.lunararc.common.network;

import io.lunararcdevs.lunararc.common.bridge.ServerCommonPacketListenerBridge;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.StandardMessenger;

/** Shared Bukkit plugin-channel policy; loader modules only own native registration/transport. */
public final class LunarArcPluginChannelPolicy {
    private LunarArcPluginChannelPolicy() {}

    public static String correctedChannel(String channel) {
        return StandardMessenger.validateAndCorrectChannel(channel);
    }

    public static ResourceLocation channelId(String channel) {
        return ResourceLocation.tryParse(correctedChannel(channel));
    }

    public static void validateManagedOutbound(CraftPlayer player, Plugin plugin, String channel, byte[] message) {
        StandardMessenger.validatePluginMessage(player.getServer().getMessenger(), plugin, channel, message);
    }

    public static boolean clientRegistered(CraftPlayer player, String correctedChannel) {
        if (player.getHandle().connection == null) return false;
        ServerCommonPacketListenerBridge bridge =
                (ServerCommonPacketListenerBridge) (Object) player.getHandle().connection;
        return bridge.lunararc$getPluginChannels().contains(correctedChannel);
    }

    public static void reportNativeConflict(String loaderName, ResourceLocation id, String nativeKind) {
        Bukkit.getLogger().severe("[LunarArc] Bukkit plugin channel '" + id
                + "' conflicts with an existing " + loaderName + " " + nativeKind
                + "; the Bukkit channel will not replace it.");
    }
}
