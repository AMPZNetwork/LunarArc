package io.ampznetwork.lunararc.common.mixin.core.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import net.minecraft.network.Varint21FrameDecoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Varint21FrameDecoder.class)
public abstract class Varint21FrameDecoderMixin {
    @Inject(method = "decode(Lio/netty/channel/ChannelHandlerContext;Lio/netty/buffer/ByteBuf;Ljava/util/List;)V", at = @At("HEAD"), cancellable = true, require = 1)
    private void lunararc$discardDisconnectedInput(ChannelHandlerContext context, ByteBuf input, List<Object> output, CallbackInfo ci) {
        if (!context.channel().isActive()) {
            input.skipBytes(input.readableBytes());
            ci.cancel();
        }
    }
}
