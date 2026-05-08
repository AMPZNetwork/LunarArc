package io.ampznetwork.lunararc.common.mixin.core.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("LunarArc");

    @Inject(method = "placeBlock", at = @At("HEAD"), cancellable = true)
    private void lunararc$onPlaceBlock(BlockPlaceContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (!(context.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;
        if (!(context.getPlayer() instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) return;

        net.minecraft.core.BlockPos placePos = context.getClickedPos();
        
        org.bukkit.event.block.BlockPlaceEvent event = org.bukkit.craftbukkit.v1_21_R1.event.CraftEventFactory.callBlockPlaceEvent(
            serverLevel, 
            placePos, 
            serverPlayer, 
            context.getHand(),
            state
        );

        if (event != null && event.isCancelled()) {
            logger.info("[LunarArc] Block placement at {} CANCELLED by event.", placePos);
            serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket(serverLevel, placePos));
            if (!placePos.equals(context.getClickedPos())) {
                serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket(serverLevel, context.getClickedPos()));
            }
            cir.setReturnValue(false);
        }
    }
}
