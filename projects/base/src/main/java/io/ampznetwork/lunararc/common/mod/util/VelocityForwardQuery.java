package io.ampznetwork.lunararc.common.mod.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.custom.CustomQueryPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record VelocityForwardQuery(FriendlyByteBuf data) {
    public @NotNull ResourceLocation id() {
        return VelocitySupport.PLAYER_INFO_CHANNEL;
    }

    public void write(@NotNull FriendlyByteBuf buf) {
        buf.writeBytes(this.data.slice());
    }
}
