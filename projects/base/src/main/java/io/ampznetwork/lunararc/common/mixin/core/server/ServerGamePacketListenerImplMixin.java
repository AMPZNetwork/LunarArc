package io.ampznetwork.lunararc.common.mixin.core.server;

import io.ampznetwork.lunararc.common.LunarArcPlatform;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(method = "handleChat", at = @At("HEAD"), cancellable = true)
    private void lunararc$onChat(ServerboundChatPacket packet, CallbackInfo ci) {
        try {
            Object craftServer = LunarArcPlatform.getServer();
            if (craftServer == null)
                return;

            Class<?> eventClass = Class.forName("org.bukkit.event.player.AsyncPlayerChatEvent");
            Object bukkitPlayer = ((io.ampznetwork.lunararc.common.bridge.EntityBridge) this.player).lunararc$getBukkitEntity();
            String message = packet.message();

            java.util.Set<?> recipients = new java.util.HashSet<>(
                    (java.util.Collection<?>) craftServer.getClass().getMethod("getOnlinePlayers").invoke(craftServer));

            // AsyncPlayerChatEvent(boolean async, Player who, String message, Set<Player>
            // players)
            Object event = eventClass
                    .getConstructor(boolean.class, Class.forName("org.bukkit.entity.Player"), String.class,
                            java.util.Set.class)
                    .newInstance(false, bukkitPlayer, message, recipients);

            Object pm = craftServer.getClass().getMethod("getPluginManager").invoke(craftServer);
            pm.getClass().getMethod("callEvent", Class.forName("org.bukkit.event.Event")).invoke(pm, event);

            Boolean isCancelled = (Boolean) eventClass.getMethod("isCancelled").invoke(event);
            if (isCancelled) {
                ci.cancel();
            }
        } catch (Throwable ignored) {
        }
    }
}
