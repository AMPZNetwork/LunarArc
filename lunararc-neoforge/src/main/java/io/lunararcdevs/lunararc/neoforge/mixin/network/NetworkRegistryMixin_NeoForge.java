package io.lunararcdevs.lunararc.neoforge.mixin.network;

import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import io.lunararcdevs.lunararc.common.bridge.ServerCommonPacketListenerBridge;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
import net.neoforged.neoforge.network.payload.CommonRegisterPayload;
import net.neoforged.neoforge.network.registration.ChannelAttributes;
import net.neoforged.neoforge.network.registration.NetworkPayloadSetup;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NetworkRegistry.class, remap = false)
public abstract class NetworkRegistryMixin_NeoForge {

    @Inject(method = "onMinecraftRegister", at = @At("TAIL"))
    private static void lunararc$onMinecraftRegister(Connection connection, Set<ResourceLocation> channels, CallbackInfo ci) {
        lunararc$withServerListener(connection, (bridge, player) ->
                channels.forEach(channel -> bridge.lunararc$addLoaderPluginChannel(player, channel.toString())));
    }

    @Inject(method = "onMinecraftUnregister", at = @At("TAIL"))
    private static void lunararc$onMinecraftUnregister(Connection connection, Set<ResourceLocation> channels, CallbackInfo ci) {
        lunararc$withServerListener(connection, (bridge, player) ->
                channels.forEach(channel -> bridge.lunararc$removeLoaderPluginChannel(player, channel.toString())));
    }

    @Inject(method = "onCommonRegister", at = @At("TAIL"))
    private static void lunararc$onCommonRegister(ICommonPacketListener listener, CommonRegisterPayload payload, CallbackInfo ci) {
        if (payload.protocol() != ConnectionProtocol.PLAY) return;
        lunararc$syncAllPlayChannels(listener.getConnection());
    }

    @Inject(method = "onConfigurationFinished", at = @At("TAIL"))
    private static void lunararc$onConfigurationFinished(ICommonPacketListener listener, CallbackInfo ci) {
        lunararc$syncAllPlayChannels(listener.getConnection());
    }

    @Unique
    private static void lunararc$syncAllPlayChannels(Connection connection) {
        lunararc$withServerListener(connection, (bridge, player) -> {
            LinkedHashSet<String> channels = new LinkedHashSet<>();
            NetworkPayloadSetup setup = ChannelAttributes.getPayloadSetup(connection);
            if (setup != null) {
                setup.getChannels(ConnectionProtocol.PLAY).keySet().forEach(id -> channels.add(id.toString()));
            }
            ChannelAttributes.getOrCreateCommonChannels(connection, ConnectionProtocol.PLAY)
                    .forEach(id -> channels.add(id.toString()));
            ChannelAttributes.getOrCreateAdHocChannels(connection)
                    .forEach(id -> channels.add(id.toString()));
            bridge.lunararc$replaceLoaderPluginChannels(player, channels);
        });
    }

    @Unique
    private static void lunararc$withServerListener(Connection connection, LunarArcChannelAction action) {
        Object packetListener = connection.getPacketListener();
        if (!(packetListener instanceof ServerCommonPacketListenerImpl common)) return;
        ServerCommonPacketListenerBridge bridge = (ServerCommonPacketListenerBridge) (Object) common;
        ServerPlayer serverPlayer = bridge.lunararc$getPlayer();
        if (serverPlayer == null) return;

        Runnable task = () -> {
            org.bukkit.entity.Entity bukkit = ((EntityBridge) (Object) serverPlayer).lunararc$getBukkitEntity();
            if (bukkit instanceof Player player) action.apply(bridge, player);
        };
        if (serverPlayer.server.isSameThread()) task.run();
        else serverPlayer.server.execute(task);
    }

    @FunctionalInterface
    @Unique
    private interface LunarArcChannelAction {
        void apply(ServerCommonPacketListenerBridge bridge, Player player);
    }
}
