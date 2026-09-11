package io.lunararcdevs.lunararc.common.mixin.core.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {

    @Inject(method = "placeBlock", at = @At("HEAD"), cancellable = true)
    private void lunararc$onPlaceBlock(BlockPlaceContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (!(context.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;
        if (!(context.getPlayer() instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) return;

        net.minecraft.core.BlockPos placePos = context.getClickedPos();

        // Forge/NeoForge already expose a native placement event after their BlockSnapshot
        // has captured the replaced state. Firing Bukkit here at the HEAD of placeBlock is
        // too early for modded blocks: the new block/block entity does not exist yet, and
        // Repair 86 then reused that premature Bukkit event when the loader event arrived.
        // Let the loader-owned event be the single source of truth on those runtimes.
        boolean loaderOwnsCancellation = ((io.lunararcdevs.lunararc.common.bridge.MinecraftServerBridge) (Object) serverPlayer.server)
                .lunararc$loaderHandlesBlockPlaceEvent();
        if (loaderOwnsCancellation) return;

        org.bukkit.event.block.BlockPlaceEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callBlockPlaceEvent(
                serverLevel, placePos, serverPlayer, context.getHand(), state);
        io.lunararcdevs.lunararc.common.mod.util.LunarArcBlockPlaceCapture.capture(serverPlayer, placePos, event);

        if (event != null && event.isCancelled()) {
            serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket(serverLevel, placePos));
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "place", at = @At("RETURN"))
    private void lunararc$clearPlaceCapture(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        io.lunararcdevs.lunararc.common.mod.util.LunarArcBlockPlaceCapture.clear();
    }
}
