package io.ampznetwork.lunararc.common.mixin.core.server;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Inject(method = "canPlayerLogin", at = @At("HEAD"), cancellable = true)
    private void lunararc$onPlayerLogin(java.net.SocketAddress socketAddress, com.mojang.authlib.GameProfile profile,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<net.minecraft.network.chat.Component> cir) {
        try {
            Object craftServer = io.ampznetwork.lunararc.common.LunarArcPlatform.getServer();
            if (craftServer != null) {
                // Pre-create the player entity to satisfy plugins
                ServerPlayer player = new ServerPlayer(((org.bukkit.craftbukkit.v1_21_R1.CraftServer) craftServer).getHandle(),
                        ((org.bukkit.craftbukkit.v1_21_R1.CraftServer) craftServer).getHandle().overworld(), profile,
                        net.minecraft.server.level.ClientInformation.createDefault());
                
                org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) ((io.ampznetwork.lunararc.common.bridge.EntityBridge) player).lunararc$getBukkitEntity();

                org.bukkit.event.player.PlayerLoginEvent event = new org.bukkit.event.player.PlayerLoginEvent(
                        bukkitPlayer, "localhost", ((java.net.InetSocketAddress) socketAddress).getAddress());

                org.bukkit.Bukkit.getPluginManager().callEvent(event);

                if (event.getResult() != org.bukkit.event.player.PlayerLoginEvent.Result.ALLOWED) {
                    cir.setReturnValue(net.minecraft.network.chat.Component.literal(event.getKickMessage()));
                }
            }
        } catch (Throwable e) {
            lunararc$logger.error("Error during PlayerLoginEvent for {}", profile.getName(), e);
        }
    }

    @Unique
    private static final Logger lunararc$logger = LoggerFactory.getLogger("LunarArc");

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void lunararc$onPlayerJoin(Connection connection, ServerPlayer player,
            net.minecraft.server.network.CommonListenerCookie cookie, CallbackInfo ci) {
        try {
            Object craftServer = io.ampznetwork.lunararc.common.LunarArcPlatform.getServer();
            if (craftServer != null) {
                org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) ((io.ampznetwork.lunararc.common.bridge.EntityBridge) player).lunararc$getBukkitEntity();
                if (bukkitPlayer != null) {
                    org.bukkit.event.player.PlayerJoinEvent event = new org.bukkit.event.player.PlayerJoinEvent(
                            bukkitPlayer,
                            net.kyori.adventure.text.Component
                                    .text("§e" + bukkitPlayer.getName() + " joined the game"));
                    org.bukkit.Bukkit.getPluginManager().callEvent(event);
                }
                // In-Game update notification
                try {
                    Class<?> ucClass = Class.forName("io.ampznetwork.lunararc.launcher.UpdateChecker");
                    String latest = (String) ucClass.getField("LATEST_VERSION").get(null);
                    String url = (String) ucClass.getField("UPDATE_URL").get(null);

                    java.lang.reflect.Method getPerm = player.getClass().getDeclaredMethod("getPermissionLevel");
                    getPerm.setAccessible(true);
                    int permLevel = (int) getPerm.invoke(player);
                    if (permLevel >= 4 && latest != null) {
                        java.lang.reflect.Method sendMsg = player.getClass().getDeclaredMethod("sendSystemMessage",
                                net.minecraft.network.chat.Component.class);
                        sendMsg.setAccessible(true);
                        sendMsg.invoke(player, net.minecraft.network.chat.Component
                                .literal("§b[LunarArc] §fA new update is available: §a" + latest));
                        sendMsg.invoke(player, net.minecraft.network.chat.Component
                                .literal("§b[LunarArc] §fDownload it at: §d" + url));
                    }
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable e) { // Use Throwable to catch all errors, including reflection issues
            lunararc$logger.error("Error during PlayerJoinEvent for {}", player.getName().getString(), e);
        }
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void lunararc$onPlayerQuit(ServerPlayer player, CallbackInfo ci) {
        io.ampznetwork.lunararc.common.server.LunarArcContext.clear();
        lunararc$logger.info("Player {} disconnected, cleaning up...", player.getName().getString());
        try {
            Object craftServer = io.ampznetwork.lunararc.common.LunarArcPlatform.getServer();
            if (craftServer != null) {
                org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) ((io.ampznetwork.lunararc.common.bridge.EntityBridge) player).lunararc$getBukkitEntity();
                if (bukkitPlayer != null) {
                    org.bukkit.event.player.PlayerQuitEvent event = new org.bukkit.event.player.PlayerQuitEvent(
                            bukkitPlayer,
                            net.kyori.adventure.text.Component.text("§e" + bukkitPlayer.getName() + " left the game"),
                            org.bukkit.event.player.PlayerQuitEvent.QuitReason.DISCONNECTED);
                    org.bukkit.Bukkit.getPluginManager().callEvent(event);
                }
            }
        } catch (Throwable e) { // Use Throwable to catch all errors, including reflection issues
            lunararc$logger.error("Error during PlayerQuitEvent for {}", player.getName().getString(), e);
        }
    }
}