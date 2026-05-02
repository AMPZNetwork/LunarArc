package io.ampznetwork.lunararc.common.mixin.core.server;

import com.mojang.authlib.GameProfile;
import io.ampznetwork.lunararc.common.config.LunarArcConfig;
import io.ampznetwork.lunararc.common.mod.util.VelocitySupport;
import io.netty.buffer.Unpooled;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadLocalRandom;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerMixin {

    @Shadow private GameProfile authenticatedProfile;
    @Shadow public Connection connection;
    @Shadow public abstract void disconnect(Component reason);

    /** Unique transaction ID we generate for our Velocity query. -1 means not sent yet. */
    @Unique private int lunararc$velocityLoginId = -1;

    /**
     * After the vanilla handleHello sends the encryption request (or skips it in offline mode),
     * inject a Velocity player_info query if Velocity forwarding is enabled.
     * We inject at RETURN so vanilla logic has already validated state.
     */
    @Inject(method = "handleHello", at = @At("RETURN"))
    private void lunararc$sendVelocityQuery(CallbackInfo ci) {
        if (!LunarArcConfig.isVelocityEnabled()) return;
        String secret = LunarArcConfig.getVelocitySecret();
        if (secret.isEmpty()) {
            System.err.println("[LunarArc] Velocity is enabled but 'velocity.secret' is empty in lunararc.properties!");
            return;
        }

        this.lunararc$velocityLoginId = ThreadLocalRandom.current().nextInt();
        this.connection.send(new ClientboundCustomQueryPacket(
                this.lunararc$velocityLoginId,
                (net.minecraft.network.protocol.login.custom.CustomQueryPayload) VelocitySupport.createPacket()
        ));
    }

    /**
     * Intercept the custom query response. If it matches our Velocity transaction ID,
     * verify the HMAC signature and inject the real player IP and profile.
     * Injecting at the disconnect INVOKE site mirrors the Arclight approach.
     */
    @Inject(
        method = "handleCustomQueryPacket",
        cancellable = true,
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;disconnect(Lnet/minecraft/network/chat/Component;)V")
    )
    private void lunararc$handleVelocityResponse(ServerboundCustomQueryAnswerPacket packet, CallbackInfo ci) {
        if (!LunarArcConfig.isVelocityEnabled()) return;
        if (packet.transactionId() != this.lunararc$velocityLoginId) return;

        var rawPayload = packet.payload();
        if (rawPayload == null) {
            this.disconnect(Component.literal("This server requires you to connect with Velocity."));
            ci.cancel();
            return;
        }

        // Re-serialize the payload into a FriendlyByteBuf for parsing
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        rawPayload.write(buf);

        if (!VelocitySupport.checkIntegrity(buf, LunarArcConfig.getVelocitySecret())) {
            System.err.println("[LunarArc] Velocity forwarding integrity check failed! Verify velocity.secret.");
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

        this.connection.address = new InetSocketAddress(VelocitySupport.readAddress(buf), port);
        this.authenticatedProfile = VelocitySupport.createProfile(buf);

        System.out.println("[LunarArc] Velocity forwarded: " + this.authenticatedProfile.getName()
                + " @ " + this.connection.address);

        ci.cancel();
    }
}
