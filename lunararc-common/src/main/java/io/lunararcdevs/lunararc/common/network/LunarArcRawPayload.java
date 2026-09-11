package io.lunararcdevs.lunararc.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Raw Bukkit plugin-message payload for channels not registered as typed vanilla payloads. */
public record LunarArcRawPayload(ResourceLocation id, byte[] data) implements CustomPacketPayload {
    public static final int MAX_BYTES = 32766;

    @Override public Type<LunarArcRawPayload> type() { return new Type<>(id); }

    public static <B extends FriendlyByteBuf> StreamCodec<B, LunarArcRawPayload> codec(ResourceLocation id, int maxBytes) {
        return new StreamCodec<>() {
            @Override public LunarArcRawPayload decode(B buf) {
                int size = buf.readableBytes();
                if (size > maxBytes) throw new IllegalArgumentException("Payload exceeds " + maxBytes + " bytes");
                byte[] data = new byte[size];
                buf.readBytes(data);
                return new LunarArcRawPayload(id, data);
            }
            @Override public void encode(B buf, LunarArcRawPayload payload) { buf.writeBytes(payload.data()); }
        };
    }
}
