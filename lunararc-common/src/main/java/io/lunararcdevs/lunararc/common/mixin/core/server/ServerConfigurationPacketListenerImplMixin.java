package io.lunararcdevs.lunararc.common.mixin.core.server;

import com.mojang.authlib.GameProfile;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.lunararcdevs.lunararc.common.bridge.ConnectionBridge;
import io.lunararcdevs.lunararc.common.bridge.ServerCommonPacketListenerBridge;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import java.net.SocketAddress;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class ServerConfigurationPacketListenerImplMixin {

    @WrapOperation(
            method = "handleConfigurationFinished",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;canPlayerLogin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/network/chat/Component;"))
    private Component lunararc$skipSecondLoginCheckVanilla(
            PlayerList playerList,
            SocketAddress address,
            GameProfile profile,
            Operation<Component> original) {
        ServerPlayer player = ((ServerCommonPacketListenerBridge) (Object) this).lunararc$getPlayer();
        return player == null ? original.call(playerList, address, profile) : null;
    }

    @WrapOperation(
            method = "handleConfigurationFinished",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;canPlayerLogin(Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/server/level/ServerPlayer;"))
    private ServerPlayer lunararc$reuseLoginPlayerPaper(
            PlayerList playerList,
            ServerLoginPacketListenerImpl loginListener,
            GameProfile profile,
            Operation<ServerPlayer> original) {
        ServerPlayer player = ((ServerCommonPacketListenerBridge) (Object) this).lunararc$getPlayer();
        return player == null ? original.call(playerList, loginListener, profile) : player;
    }

    @WrapOperation(
            method = "handleConfigurationFinished",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;getPlayerForLogin(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/server/level/ClientInformation;)Lnet/minecraft/server/level/ServerPlayer;"))
    private ServerPlayer lunararc$useLoginPlayerVanilla(
            PlayerList playerList,
            GameProfile profile,
            ClientInformation information,
            Operation<ServerPlayer> original) {
        ServerPlayer player = ((ServerCommonPacketListenerBridge) (Object) this).lunararc$getPlayer();
        if (player == null) {
            return original.call(playerList, profile, information);
        }
        player.updateOptions(information);
        return player;
    }

    @WrapOperation(
            method = "handleConfigurationFinished",
            require = 0,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;getPlayerForLogin(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/server/level/ClientInformation;Lnet/minecraft/server/level/ServerPlayer;)Lnet/minecraft/server/level/ServerPlayer;"))
    private ServerPlayer lunararc$useLoginPlayerPaper(
            PlayerList playerList,
            GameProfile profile,
            ClientInformation information,
            ServerPlayer loginPlayer,
            Operation<ServerPlayer> original) {
        ServerPlayer cached = ((ServerCommonPacketListenerBridge) (Object) this).lunararc$getPlayer();
        return original.call(playerList, profile, information, cached != null ? cached : loginPlayer);
    }

    @Inject(method = "handleConfigurationFinished", at = @At("RETURN"), require = 0)
    private void lunararc$clearLoginPlayer(CallbackInfo ci) {
        ServerCommonPacketListenerBridge bridge = (ServerCommonPacketListenerBridge) (Object) this;
        ((ConnectionBridge) bridge.lunararc$getConnection()).lunararc$setLoginPlayer(null);
    }
}
