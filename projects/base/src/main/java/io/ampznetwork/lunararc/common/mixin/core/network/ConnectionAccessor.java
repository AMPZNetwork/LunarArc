package io.ampznetwork.lunararc.common.mixin.core.network;

import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.net.SocketAddress;

@Mixin(Connection.class)
public interface ConnectionAccessor {
    @Accessor("address")
    void setAddress(SocketAddress address);

    @Accessor("address")
    SocketAddress getAddress();
}
