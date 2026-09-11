package io.lunararcdevs.lunararc.common.mod.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface IVelocityForwardQuery {
    ResourceLocation id();
    void write(FriendlyByteBuf buf);
    Object getPayload();
}
