package io.ampznetwork.lunararc.common.mixin.core.server;

import io.ampznetwork.lunararc.common.LunarArcPlatform;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {

    @Shadow
    @Final
    protected ServerPlayer player;

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void lunararc$onUseItemOn(ServerPlayer player, Level level, net.minecraft.world.item.ItemStack stack, InteractionHand hand,
            BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        io.ampznetwork.lunararc.common.server.LunarArcContext.setCurrentPlayer(player);
        
        org.bukkit.event.player.PlayerInteractEvent event = org.bukkit.craftbukkit.v1_21_R1.event.CraftEventFactory.callPlayerInteractEvent(
            player, 
            org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK, 
            hitResult.getBlockPos(), 
            hitResult.getDirection(), 
            stack
        );

        if (event != null && event.isCancelled()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    private void lunararc$onUseItem(ServerPlayer player, Level level, net.minecraft.world.item.ItemStack stack, InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir) {
        io.ampznetwork.lunararc.common.server.LunarArcContext.setCurrentPlayer(player);
        
        org.bukkit.event.player.PlayerInteractEvent event = org.bukkit.craftbukkit.v1_21_R1.event.CraftEventFactory.callPlayerInteractEvent(
            player, 
            org.bukkit.event.block.Action.RIGHT_CLICK_AIR, 
            null, 
            null, 
            stack
        );

        if (event != null && event.isCancelled()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(method = "useItemOn", at = @At("RETURN"))
    private void lunararc$afterUseItemOn(ServerPlayer player, Level level, net.minecraft.world.item.ItemStack stack, InteractionHand hand,
            BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        io.ampznetwork.lunararc.common.server.LunarArcContext.clear();
    }

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void lunararc$onDestroyBlock(net.minecraft.core.BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        org.bukkit.event.block.BlockBreakEvent event = org.bukkit.craftbukkit.v1_21_R1.event.CraftEventFactory.callBlockBreakEvent(player.serverLevel(), pos, player);
        if (event.isCancelled()) {
            // Force block update to client to fix ghost blocks
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket(player.serverLevel(), pos));
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "useItem", at = @At("RETURN"))
    private void lunararc$afterUseItem(ServerPlayer player, Level level, net.minecraft.world.item.ItemStack stack, InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir) {
        io.ampznetwork.lunararc.common.server.LunarArcContext.clear();
    }
}
