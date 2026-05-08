package io.ampznetwork.lunararc.common.mixin.core.server;

import com.mojang.authlib.GameProfile;
import io.ampznetwork.lunararc.common.config.LunarArcConfig;
import io.ampznetwork.lunararc.common.mod.util.VelocitySupport;
import io.netty.buffer.Unpooled;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import io.ampznetwork.lunararc.common.bridge.MinecraftServerBridge;
import io.ampznetwork.lunararc.common.bridge.ServerLoginPacketListenerBridge;
import net.minecraft.Util;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerMixin implements ServerLoginPacketListenerBridge {

    @Shadow
    @Final
    MinecraftServer server;

    @Shadow
    private GameProfile authenticatedProfile;
    @Shadow
    public Connection connection;

    @Shadow
    public abstract void disconnect(Component reason);

    /**
     * Unique transaction ID we generate for our Velocity query. -1 means not sent
     * yet.
     */
    @Unique
    private int lunararc$velocityLoginId = -1;

    @Unique
    private static final Logger lunararc$logger = LoggerFactory.getLogger("LunarArc");

    @Unique
    private boolean lunararc$preLoginCompleted = false;

    /**
     * After the vanilla handleHello sends the encryption request (or skips it in
     * offline mode),
     * inject a Velocity player_info query if Velocity forwarding is enabled.
     * We inject at RETURN so vanilla logic has already validated state.
     */
    @Inject(method = "handleHello", at = @At("HEAD"), cancellable = true)
    private void lunararc$onHandleHello(ServerboundHelloPacket packet, CallbackInfo ci) {
        if (LunarArcConfig.isVelocityEnabled()) {
            this.lunararc$velocityLoginId = ThreadLocalRandom.current().nextInt();
            this.connection.send(new ClientboundCustomQueryPacket(
                    this.lunararc$velocityLoginId,
                    java.util.Objects.requireNonNull((net.minecraft.network.protocol.login.custom.CustomQueryPayload) (Object) VelocitySupport.createPacket())));
            ci.cancel();
            return;
        }

        if (!this.server.usesAuthentication()) {
            Util.backgroundExecutor().execute(() -> {
                try {
                    GameProfile offlineProfile = new GameProfile(UUID.nameUUIDFromBytes(
                            ("OfflinePlayer:" + packet.name()).getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                            packet.name());
                    this.lunararc$preLogin(offlineProfile);
                } catch (Exception e) {
                    this.disconnect(Component.literal("Failed to verify offline player"));
                    e.printStackTrace();
                }
            });
            ci.cancel();
        }
    }

    /**
     * Verify the HMAC signature and inject the real player IP and profile.
     */
    @Inject(method = "handleCustomQueryPacket", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;disconnect(Lnet/minecraft/network/chat/Component;)V"))
    private void lunararc$handleVelocityResponse(ServerboundCustomQueryAnswerPacket packet, CallbackInfo ci) {
        if (!LunarArcConfig.isVelocityEnabled())
            return;
        if (packet.transactionId() != this.lunararc$velocityLoginId)
            return;

        var rawPayload = packet.payload();
        if (rawPayload == null) {
            this.disconnect(Component.literal("This server requires you to connect with Velocity."));
            ci.cancel();
            return;
        }

        // Re-serialize the payload into a FriendlyByteBuf for parsing
        FriendlyByteBuf buf = new FriendlyByteBuf(java.util.Objects.requireNonNull(io.netty.buffer.Unpooled.buffer()));
        rawPayload.write(buf);

        if (!VelocitySupport.checkIntegrity(buf, LunarArcConfig.getVelocitySecret())) {
            lunararc$logger.error("Velocity forwarding integrity check failed! Verify velocity.secret.");
            this.disconnect(Component.literal("Unable to verify player details (Velocity integrity failed)."));
            ci.cancel();
            return;
        }

        int version = buf.readVarInt();
        if (version > VelocitySupport.MAX_SUPPORTED_FORWARDING_VERSION) {
            throw new IllegalStateException("[LunarArc] Unsupported Velocity forwarding version: " + version);
        }

        // Get the real port before replacing the address
        int port = 0;
        if (this.connection.getRemoteAddress() instanceof InetSocketAddress isa) {
            port = isa.getPort();
        }

        ((io.ampznetwork.lunararc.common.mixin.core.network.ConnectionAccessor) this.connection)
                .setAddress(new InetSocketAddress(VelocitySupport.readAddress(buf), port));
        this.authenticatedProfile = VelocitySupport.createProfile(buf);

        lunararc$logger.info("Velocity forwarded: {} @ {}", this.authenticatedProfile.getName(),
                ((io.ampznetwork.lunararc.common.mixin.core.network.ConnectionAccessor) this.connection).getAddress());

        // Proceed with login logic (AsyncPlayerPreLoginEvent)
        Util.backgroundExecutor().execute(() -> {
            try {
                this.lunararc$preLogin(this.authenticatedProfile);
            } catch (Exception e) {
                this.disconnect(Component.literal("Exception verifying " + this.authenticatedProfile.getName()));
                e.printStackTrace();
            }
        });

        ci.cancel();
    }

    @Unique
    @Override
    public void lunararc$preLogin(GameProfile profile) throws Exception {
        try {
            Class<?> eventClass = Class.forName("org.bukkit.event.player.AsyncPlayerPreLoginEvent");
            Class<?> resultClass = Class.forName("org.bukkit.event.player.AsyncPlayerPreLoginEvent$Result");
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");

            String playerName = profile.getName();
            InetAddress address = ((InetSocketAddress) connection.getRemoteAddress()).getAddress();
            UUID uniqueId = profile.getId();

            // Constructor: AsyncPlayerPreLoginEvent(String name, InetAddress ipAddress,
            // UUID uniqueId)
            Object asyncEvent = eventClass.getConstructor(String.class, InetAddress.class, UUID.class)
                    .newInstance(playerName, address, uniqueId);

            Object pm = bukkitClass.getMethod("getPluginManager").invoke(null);
            pm.getClass().getMethod("callEvent", Class.forName("org.bukkit.event.Event")).invoke(pm, asyncEvent);

            Object result = eventClass.getMethod("getLoginResult").invoke(asyncEvent);
            Object allowed = resultClass.getField("ALLOWED").get(null);

            if (result != allowed) {
                String kickMessage = (String) eventClass.getMethod("getKickMessage").invoke(asyncEvent);
                this.disconnect(Component.literal(kickMessage != null ? kickMessage : "Disconnected by plugin"));
                return;
            }
        } catch (ClassNotFoundException e) {
            // Bukkit not present, proceed normally
        } catch (Exception e) {
            lunararc$logger.error("Error calling AsyncPlayerPreLoginEvent", e);
        }

        // Proceed to vanilla verification
        ((MinecraftServerBridge) this.server).lunararc$queueTask(() -> {
            try {
                this.lunararc$preLoginCompleted = true;
                this.startClientVerification(profile);
            } catch (Exception e) {
                this.disconnect(Component.literal("Error starting client verification"));
                e.printStackTrace();
            }
        });
    }

    @Shadow
    abstract void startClientVerification(GameProfile profile);

    /**
     * Inject into handleKey to intercept online-mode login after authentication.
     * In vanilla, handleKey starts an authentication thread. We want to catch the
     * RESULT of that.
     * However, it's easier to inject into the method that is called AFTER
     * successful auth.
     * In 1.21.1, that is startClientVerification(GameProfile).
     */
    @Inject(method = "startClientVerification", at = @At("HEAD"), cancellable = true)
    private void lunararc$onStartClientVerification(GameProfile profile, CallbackInfo ci) {
        // If we are already in our custom login flow, let it proceed
        if (this.lunararc$preLoginCompleted)
            return;

        // Start our custom flow
        Util.backgroundExecutor().execute(() -> {
            try {
                this.lunararc$preLogin(profile);
            } catch (Exception e) {
                this.disconnect(Component.literal("Error during pre-login event"));
                e.printStackTrace();
            }
        });
        ci.cancel();
    }
}
