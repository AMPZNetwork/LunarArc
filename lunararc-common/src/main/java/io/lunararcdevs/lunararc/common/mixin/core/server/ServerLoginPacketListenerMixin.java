package io.lunararcdevs.lunararc.common.mixin.core.server;

import com.mojang.authlib.GameProfile;
import io.lunararcdevs.lunararc.common.bridge.MinecraftServerBridge;
import io.lunararcdevs.lunararc.common.bridge.PlayerListBridge;
import io.lunararcdevs.lunararc.common.bridge.ServerLoginPacketListenerBridge;
import io.lunararcdevs.lunararc.common.config.LunarArcConfig;
import io.lunararcdevs.lunararc.common.messaging.LunarArcComponentPipeline;
import io.lunararcdevs.lunararc.common.mod.util.VelocitySupport;
import net.minecraft.Util;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerMixin implements ServerLoginPacketListenerBridge {

    @Shadow @Final MinecraftServer server;
    @Shadow private GameProfile authenticatedProfile;
    @Shadow public Connection connection;
    @Shadow public abstract void disconnect(Component reason);
    @Shadow abstract void startClientVerification(GameProfile profile);

    @Unique private static final Logger lunararc$logger = LoggerFactory.getLogger("LunarArc");
    @Unique private int lunararc$velocityLoginId = -1;
    @Unique private volatile boolean lunararc$preLoginCompleted;
    @Unique private ServerPlayer lunararc$loginPlayer;

    @Override
    public Connection lunararc$getConnection() {
        return this.connection;
    }

    @Override
    public void lunararc$disconnect(Component reason) {
        this.disconnect(reason);
    }

    @Inject(method = "handleHello", at = @At("HEAD"), cancellable = true)
    private void lunararc$velocityHello(ServerboundHelloPacket packet, CallbackInfo ci) {
        if (!LunarArcConfig.isVelocityEnabled()) {
            return;
        }

        this.lunararc$velocityLoginId = ThreadLocalRandom.current().nextInt();
        this.connection.send(new ClientboundCustomQueryPacket(
                this.lunararc$velocityLoginId,
                (net.minecraft.network.protocol.login.custom.CustomQueryPayload) VelocitySupport.createPacket()));
        ci.cancel();
    }

    @Inject(
            method = "handleCustomQueryPacket",
            cancellable = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;disconnect(Lnet/minecraft/network/chat/Component;)V"))
    private void lunararc$velocityResponse(ServerboundCustomQueryAnswerPacket packet, CallbackInfo ci) {
        if (!LunarArcConfig.isVelocityEnabled() || packet.transactionId() != this.lunararc$velocityLoginId) {
            return;
        }

        var payload = packet.payload();
        if (payload == null) {
            this.disconnect(Component.literal("This server requires you to connect with Velocity."));
            ci.cancel();
            return;
        }

        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        payload.write(buf);

        if (!VelocitySupport.checkIntegrity(buf, LunarArcConfig.getVelocitySecret())) {
            this.disconnect(Component.literal("Unable to verify player details."));
            ci.cancel();
            return;
        }

        int version = buf.readVarInt();
        if (version > VelocitySupport.MAX_SUPPORTED_FORWARDING_VERSION) {
            throw new IllegalStateException("Unsupported Velocity forwarding version " + version);
        }

        int port = this.connection.getRemoteAddress() instanceof InetSocketAddress address ? address.getPort() : 0;
        this.connection.address = new InetSocketAddress(VelocitySupport.readAddress(buf), port);
        this.authenticatedProfile = VelocitySupport.createProfile(buf);

        Util.backgroundExecutor().execute(() -> {
            try {
                this.lunararc$preLogin(this.authenticatedProfile);
            } catch (Exception exception) {
                this.disconnect(Component.translatable("multiplayer.disconnect.unverified_username"));
                lunararc$logger.error("Login processing failed for {}", this.authenticatedProfile.getName(), exception);
            }
        });
        ci.cancel();
    }

    public GameProfile callPlayerPreLoginEvents(GameProfile gameprofile) throws Exception {
        String playerName = gameprofile.getName();
        SocketAddress remoteAddress = this.connection.getRemoteAddress();
        if (!(remoteAddress instanceof InetSocketAddress inetAddress)) {
            throw new IllegalStateException("Login connection does not expose an InetSocketAddress");
        }

        InetAddress address = inetAddress.getAddress();
        AsyncPlayerPreLoginEvent asyncEvent =
                new AsyncPlayerPreLoginEvent(playerName, address, gameprofile.getId());
        org.bukkit.Bukkit.getPluginManager().callEvent(asyncEvent);

        if (PlayerPreLoginEvent.getHandlerList().getRegisteredListeners().length != 0) {
            PlayerPreLoginEvent syncEvent =
                    new PlayerPreLoginEvent(playerName, address, gameprofile.getId());

            if (asyncEvent.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
                syncEvent.disallow(
                        PlayerPreLoginEvent.Result.valueOf(asyncEvent.getLoginResult().name()),
                        asyncEvent.kickMessage());
            }

            if (this.server.isSameThread()) {
                org.bukkit.Bukkit.getPluginManager().callEvent(syncEvent);
            } else {
                CompletableFuture<Void> result = new CompletableFuture<>();
                ((MinecraftServerBridge) this.server).lunararc$queueTask(() -> {
                    try {
                        org.bukkit.Bukkit.getPluginManager().callEvent(syncEvent);
                        result.complete(null);
                    } catch (Throwable throwable) {
                        result.completeExceptionally(throwable);
                    }
                });
                result.join();
            }

            if (syncEvent.getResult() != PlayerPreLoginEvent.Result.ALLOWED) {
                this.disconnect(LunarArcComponentPipeline.fromAdventure(syncEvent.kickMessage()));
            }
        } else if (asyncEvent.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            this.disconnect(LunarArcComponentPipeline.fromAdventure(asyncEvent.kickMessage()));
        }

        return gameprofile;
    }

    public void disconnect(String reason) {
        this.disconnect(LunarArcComponentPipeline.fromAdventure(
                net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                        .deserialize(reason)));
    }

    @Override
    public void lunararc$preLogin(GameProfile profile) throws Exception {
        GameProfile resolved = this.callPlayerPreLoginEvents(profile);

        // callPlayerPreLoginEvents disconnects on a denied result but still returns, the way
        // Paper's does. Paper guards the follow-up work with the same connection check rather
        // than an early return, so a kicked login stops here without a second code path.
        ((MinecraftServerBridge) this.server).lunararc$queueTask(() -> {
            if (!this.connection.isConnected()) return;
            this.lunararc$preLoginCompleted = true;
            this.startClientVerification(resolved);
        });
    }

    @Inject(method = "startClientVerification", at = @At("HEAD"), cancellable = true)
    private void lunararc$preLoginBeforeVerification(GameProfile profile, CallbackInfo ci) {
        if (this.lunararc$preLoginCompleted) {
            return;
        }

        Util.backgroundExecutor().execute(() -> {
            try {
                this.lunararc$preLogin(profile);
            } catch (Exception exception) {
                this.disconnect(Component.translatable("multiplayer.disconnect.unverified_username"));
                lunararc$logger.error("Pre-login processing failed for {}", profile.getName(), exception);
            }
        });
        ci.cancel();
    }

    @Redirect(
            method = "verifyLoginAndFinishConnectionSetup",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;canPlayerLogin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/network/chat/Component;"))
    private Component lunararc$playerLoginEvent(PlayerList playerList, SocketAddress address, GameProfile profile) {
        if (this.lunararc$loginPlayer == null) {
            this.lunararc$loginPlayer = ((PlayerListBridge) playerList)
                    .lunararc$canPlayerLogin(address, profile, (ServerLoginPacketListenerImpl) (Object) this);
        }
        return null;
    }

    @Inject(
            method = "verifyLoginAndFinishConnectionSetup",
            cancellable = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;canPlayerLogin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/network/chat/Component;",
                    shift = At.Shift.AFTER))
    private void lunararc$stopDeniedLogin(GameProfile profile, CallbackInfo ci) {
        if (this.lunararc$loginPlayer == null) {
            ci.cancel();
        }
    }
}
