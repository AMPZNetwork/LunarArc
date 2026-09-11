package io.lunararcdevs.lunararc.forge.network;

import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import io.lunararcdevs.lunararc.common.bridge.ServerCommonPacketListenerBridge;
import io.lunararcdevs.lunararc.common.network.LunarArcRawPayload;
import io.lunararcdevs.lunararc.common.network.LunarArcPluginChannelPolicy;
import io.lunararcdevs.lunararc.forge.bridge.ForgeNetworkRegistryBridge;
import io.netty.buffer.Unpooled;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.EventNetworkChannel;
import net.minecraftforge.network.NetworkRegistry;
import org.bukkit.entity.Player;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.plugin.Plugin;

public final class ForgePluginMessaging {
    private static final ConcurrentMap<ResourceLocation, EventNetworkChannel> CHANNELS = new ConcurrentHashMap<>();
    private static final Set<ResourceLocation> BLOCKED = ConcurrentHashMap.newKeySet();

    private ForgePluginMessaging() {}

    public static void ensureChannel(String channel) {
        String corrected = LunarArcPluginChannelPolicy.correctedChannel(channel);
        ResourceLocation id = ResourceLocation.tryParse(corrected);
        if (id == null || CHANNELS.containsKey(id) || BLOCKED.contains(id)) return;

        synchronized (ForgePluginMessaging.class) {
            if (CHANNELS.containsKey(id) || BLOCKED.contains(id)) return;
            if (NetworkRegistry.findTarget(id) != null) {
                BLOCKED.add(id);
                LunarArcPluginChannelPolicy.reportNativeConflict("Forge", id, "channel");
                return;
            }

            boolean wasLocked = ForgeNetworkRegistryBridge.isLocked();
            try {
                ForgeNetworkRegistryBridge.setLocked(false);
                EventNetworkChannel nativeChannel = ChannelBuilder.named(id)
                        .acceptedVersions((status, version) -> true)
                        .optional()
                        .eventNetworkChannel();
                nativeChannel.addListener((CustomPayloadEvent event) -> receive(id, event));
                CHANNELS.put(id, nativeChannel);
            io.lunararcdevs.lunararc.common.network.LunarArcPluginMessageOwnership.markNativeInbound(id);
            } finally {
                ForgeNetworkRegistryBridge.setLocked(wasLocked);
            }
        }
    }

    public static boolean sendIfManaged(CraftPlayer player, Plugin plugin, String channel, byte[] message) {
        String corrected = LunarArcPluginChannelPolicy.correctedChannel(channel);
        ResourceLocation id = ResourceLocation.tryParse(corrected);
        if (id == null) return false;
        EventNetworkChannel nativeChannel = CHANNELS.get(id);
        if (nativeChannel == null && !BLOCKED.contains(id)) return false;

        LunarArcPluginChannelPolicy.validateManagedOutbound(player, plugin, channel, message);
        if (nativeChannel == null || player.getHandle().connection == null) return true;

        ServerCommonPacketListenerBridge bridge =
                (ServerCommonPacketListenerBridge) (Object) player.getHandle().connection;
        if (!LunarArcPluginChannelPolicy.clientRegistered(player, corrected)) return true;

        nativeChannel.send(new FriendlyByteBuf(Unpooled.wrappedBuffer(message)), bridge.lunararc$getConnection());
        return true;
    }

    private static void receive(ResourceLocation id, CustomPayloadEvent event) {
        FriendlyByteBuf buffer = event.getPayload();
        if (buffer == null) return;

        byte[] data = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), data);
        CustomPayloadEvent.Context context = event.getSource();
        context.setPacketHandled(true);
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null) return;
            org.bukkit.entity.Entity bukkit = ((EntityBridge) (Object) sender).lunararc$getBukkitEntity();
            if (bukkit instanceof Player player) {
                io.lunararcdevs.lunararc.common.network.LunarArcPluginMessageDispatcher
                        .dispatch(sender.server, player, id, data);
            }
        });
    }
}
