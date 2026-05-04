package io.ampznetwork.lunararc.common.mixin.core.server;

import io.ampznetwork.lunararc.common.LunarArcPlatform;
import io.ampznetwork.lunararc.common.stubs.org.bukkit.craftbukkit.v1_21_R1.CraftPlayer;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Inject(method = "placeNewPlayer", at = @At("HEAD"))
    private void lunararc$catchAsyncLogin(Connection connection, ServerPlayer player, net.minecraft.server.network.CommonListenerCookie cookie, CallbackInfo ci) {
        io.ampznetwork.lunararc.common.util.AsyncCatcher.catchOp("player login");
    }

    @Inject(method = "placeNewPlayer", at = @At("RETURN"))
    private void lunararc$onPlayerJoin(Connection connection, ServerPlayer player, net.minecraft.server.network.CommonListenerCookie cookie, CallbackInfo ci) {
        try {
            Object craftServer = LunarArcPlatform.getServer();
            if (craftServer == null) return;

            Class<?> eventClass = Class.forName("org.bukkit.event.player.PlayerJoinEvent");
            Object bukkitPlayer = new CraftPlayer(player);
            String joinMessage = "§e" + player.getScoreboardName() + " joined the game";

            Object event = eventClass.getConstructor(Class.forName("org.bukkit.entity.Player"), String.class)
                    .newInstance(bukkitPlayer, joinMessage);

            Object pm = craftServer.getClass().getMethod("getPluginManager").invoke(craftServer);
            pm.getClass().getMethod("callEvent", Class.forName("org.bukkit.event.Event")).invoke(pm, event);
        } catch (Throwable ignored) {}
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void lunararc$onPlayerQuit(ServerPlayer player, CallbackInfo ci) {
        try {
            Object craftServer = LunarArcPlatform.getServer();
            if (craftServer == null) return;

            Class<?> eventClass = Class.forName("org.bukkit.event.player.PlayerQuitEvent");
            Object bukkitPlayer = new CraftPlayer(player);
            String quitMessage = "§e" + player.getScoreboardName() + " left the game";

            Object event = eventClass.getConstructor(Class.forName("org.bukkit.entity.Player"), String.class)
                    .newInstance(bukkitPlayer, quitMessage);

            Object pm = craftServer.getClass().getMethod("getPluginManager").invoke(craftServer);
            pm.getClass().getMethod("callEvent", Class.forName("org.bukkit.event.Event")).invoke(pm, event);
        } catch (Throwable ignored) {}
    }
}
