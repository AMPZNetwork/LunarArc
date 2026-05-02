package io.ampznetwork.lunararc.common.mod.util;

import com.google.common.net.InetAddresses;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.custom.CustomQueryPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetAddress;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class VelocitySupport {

    public static final ResourceLocation PLAYER_INFO_CHANNEL = ResourceLocation.parse("velocity:player_info");
    public static final int MAX_SUPPORTED_FORWARDING_VERSION = 4;

    private static java.util.function.Function<FriendlyByteBuf, Object> packetProvider = (buf) -> null;

    public static void setPacketProvider(java.util.function.Function<FriendlyByteBuf, Object> provider) {
        packetProvider = provider;
    }

    public static Object createPacket() {
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeByte(MAX_SUPPORTED_FORWARDING_VERSION);
        return packetProvider.apply(buf);
    }

    public static boolean checkIntegrity(final FriendlyByteBuf buf, String secret) {
        final byte[] signature = new byte[32];
        buf.readBytes(signature);

        final byte[] data = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), data);

        try {
            final Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
            final byte[] mySignature = mac.doFinal(data);
            return MessageDigest.isEqual(signature, mySignature);
        } catch (final InvalidKeyException | NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    public static InetAddress readAddress(final FriendlyByteBuf buf) {
        return InetAddresses.forString(buf.readUtf(Short.MAX_VALUE));
    }

    public static GameProfile createProfile(final FriendlyByteBuf buf) {
        final GameProfile profile = new GameProfile(buf.readUUID(), buf.readUtf(16));
        int properties = buf.readVarInt();
        for (int i = 0; i < properties; i++) {
            String name = buf.readUtf(Short.MAX_VALUE);
            String value = buf.readUtf(Short.MAX_VALUE);
            String signature = buf.readBoolean() ? buf.readUtf(Short.MAX_VALUE) : null;
            profile.getProperties().put(name, new Property(name, value, signature));
        }
        return profile;
    }
}
