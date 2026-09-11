package io.lunararcdevs.lunararc.neoforge.network;

import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import io.lunararcdevs.lunararc.common.bridge.ServerCommonPacketListenerBridge;
import io.lunararcdevs.lunararc.common.network.LunarArcRawPayload;
import io.lunararcdevs.lunararc.common.network.LunarArcPluginChannelPolicy;
import io.lunararcdevs.lunararc.neoforge.bridge.NeoForgeNetworkRegistryBridge;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import net.neoforged.neoforge.network.registration.PayloadRegistration;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class NeoForgePluginMessaging {
    private static final String VERSION = "lunararc:bukkit/1";
    private static final ConcurrentMap<ResourceLocation, PayloadRegistration<LunarArcRawPayload>> CHANNELS = new ConcurrentHashMap<>();
    private static final Set<ResourceLocation> BLOCKED = ConcurrentHashMap.newKeySet();

    private NeoForgePluginMessaging() {}

    public static void ensureChannel(String channel) {
        String corrected = LunarArcPluginChannelPolicy.correctedChannel(channel);
        ResourceLocation id = ResourceLocation.tryParse(corrected);
        if (id == null || CHANNELS.containsKey(id) || BLOCKED.contains(id)) return;

        synchronized (NeoForgePluginMessaging.class) {
            if (CHANNELS.containsKey(id) || BLOCKED.contains(id)) return;

            Map<ConnectionProtocol, Map<ResourceLocation, PayloadRegistration<?>>> registrations =
                    NeoForgeNetworkRegistryBridge.payloadRegistrations();
            Map<ResourceLocation, StreamCodec<net.minecraft.network.FriendlyByteBuf, ? extends CustomPacketPayload>> builtins =
                    NeoForgeNetworkRegistryBridge.builtinPayloads();

            boolean occupied = builtins.containsKey(id)
                    || registrations.values().stream().anyMatch(map -> map.containsKey(id));
            if (occupied) {
                BLOCKED.add(id);
                LunarArcPluginChannelPolicy.reportNativeConflict("NeoForge", id, "payload");
                return;
            }

            CustomPacketPayload.Type<LunarArcRawPayload> type = new CustomPacketPayload.Type<>(id);
            StreamCodec<RegistryFriendlyByteBuf, LunarArcRawPayload> codec = LunarArcRawPayload.codec(id, LunarArcRawPayload.MAX_BYTES);
            PayloadRegistration<LunarArcRawPayload> registration = new PayloadRegistration<>(
                    type,
                    codec,
                    (payload, context) -> receive(id, payload, context),
                    List.of(ConnectionProtocol.PLAY),
                    Optional.empty(),
                    VERSION,
                    true);

            Map<ResourceLocation, PayloadRegistration<?>> play = registrations.get(ConnectionProtocol.PLAY);
            if (play == null) {
                throw new IllegalStateException("NeoForge 1.21.1 has no PLAY payload registry");
            }
            play.put(id, registration);
            CHANNELS.put(id, registration);
            io.lunararcdevs.lunararc.common.network.LunarArcPluginMessageOwnership.markNativeInbound(id);
        }
    }

    public static boolean sendIfManaged(CraftPlayer player, Plugin plugin, String channel, byte[] message) {
        String corrected = LunarArcPluginChannelPolicy.correctedChannel(channel);
        ResourceLocation id = ResourceLocation.tryParse(corrected);
        if (id == null) return false;

        PayloadRegistration<LunarArcRawPayload> registration = CHANNELS.get(id);
        if (registration == null && !BLOCKED.contains(id)) return false;

        LunarArcPluginChannelPolicy.validateManagedOutbound(player, plugin, channel, message);
        if (registration == null || player.getHandle().connection == null) return true;

        ServerCommonPacketListenerBridge bridge =
                (ServerCommonPacketListenerBridge) (Object) player.getHandle().connection;
        if (!LunarArcPluginChannelPolicy.clientRegistered(player, corrected)) return true;

        if (!NetworkRegistry.hasChannel(
                bridge.lunararc$getConnection(),
                ConnectionProtocol.PLAY,
                id)) {
            return true;
        }

        PacketDistributor.sendToPlayer(
                player.getHandle(),
                new LunarArcRawPayload(id, message));
        return true;
    }

    private static void receive(ResourceLocation id, LunarArcRawPayload payload, IPayloadContext context) {
        if (context.flow() != PacketFlow.SERVERBOUND || context.protocol() != ConnectionProtocol.PLAY) return;

        byte[] data = payload.data();
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sender)) return;
            org.bukkit.entity.Entity bukkit = ((EntityBridge) (Object) sender).lunararc$getBukkitEntity();
            if (bukkit instanceof Player player) {
                io.lunararcdevs.lunararc.common.network.LunarArcPluginMessageDispatcher
                        .dispatch(sender.server, player, id, data);
            }
        });
    }
}
