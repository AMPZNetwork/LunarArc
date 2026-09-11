package io.lunararcdevs.lunararc.quilt.network;

import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import io.lunararcdevs.lunararc.common.network.LunarArcRawPayload;
import io.lunararcdevs.lunararc.common.network.LunarArcPluginChannelPolicy;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class QuiltPluginMessaging {
    private static final ConcurrentMap<ResourceLocation, CustomPacketPayload.Type<LunarArcRawPayload>> CHANNELS = new ConcurrentHashMap<>();
    private static final Set<ResourceLocation> BLOCKED = ConcurrentHashMap.newKeySet();

    private QuiltPluginMessaging() {}

    public static void ensureChannel(String channel) {
        String corrected = LunarArcPluginChannelPolicy.correctedChannel(channel);
        ResourceLocation id = ResourceLocation.tryParse(corrected);
        if (id == null || CHANNELS.containsKey(id) || BLOCKED.contains(id)) return;

        synchronized (QuiltPluginMessaging.class) {
            if (CHANNELS.containsKey(id) || BLOCKED.contains(id)) return;
            if (PayloadTypeRegistryImpl.PLAY_C2S.get(id) != null || PayloadTypeRegistryImpl.PLAY_S2C.get(id) != null) {
                BLOCKED.add(id);
                LunarArcPluginChannelPolicy.reportNativeConflict("Quilt", id, "payload");
                return;
            }

            CustomPacketPayload.Type<LunarArcRawPayload> type = new CustomPacketPayload.Type<>(id);
            PayloadTypeRegistry.playC2S().register(type, LunarArcRawPayload.codec(id, LunarArcRawPayload.MAX_BYTES));
            PayloadTypeRegistry.playS2C().register(type, LunarArcRawPayload.codec(id, LunarArcRawPayload.MAX_BYTES));
            ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> {
                byte[] data = payload.data();
                org.bukkit.entity.Entity entity = ((EntityBridge) (Object) context.player()).lunararc$getBukkitEntity();
                if (entity instanceof Player player) {
                    io.lunararcdevs.lunararc.common.network.LunarArcPluginMessageDispatcher
                            .dispatch(context.server(), player, id, data);
                }
            });
            CHANNELS.put(id, type);
            io.lunararcdevs.lunararc.common.network.LunarArcPluginMessageOwnership.markNativeInbound(id);
        }
    }

    public static boolean sendIfManaged(CraftPlayer player, Plugin plugin, String channel, byte[] message) {
        String corrected = LunarArcPluginChannelPolicy.correctedChannel(channel);
        ResourceLocation id = ResourceLocation.tryParse(corrected);
        if (id == null) return false;
        CustomPacketPayload.Type<LunarArcRawPayload> type = CHANNELS.get(id);
        if (type == null && !BLOCKED.contains(id)) return false;

        LunarArcPluginChannelPolicy.validateManagedOutbound(player, plugin, channel, message);
        if (type == null || player.getHandle().connection == null) return true;

        if (!LunarArcPluginChannelPolicy.clientRegistered(player, corrected)) return true;

        if (!ServerPlayNetworking.canSend(player.getHandle(), type)) return false;

        ServerPlayNetworking.send(player.getHandle(), new LunarArcRawPayload(id, message));
        return true;
    }
}
