package io.lunararcdevs.lunararc.common.mixin.core.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.lunararcdevs.lunararc.common.bridge.LunarArcCauldronContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Captures the actual clicked face without changing CauldronInteraction's loader-owned API. */
@Mixin(AbstractCauldronBlock.class)
public abstract class AbstractCauldronBlockMixin {
    @WrapOperation(
            method = "useItemOn",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/cauldron/CauldronInteraction;interact(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/ItemInteractionResult;"),
            require = 0)
    private ItemInteractionResult lunararc$cauldronDirection(
            CauldronInteraction interaction, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, ItemStack stack,
            Operation<ItemInteractionResult> original,
            ItemStack passedStack, BlockState passedState, Level passedLevel, BlockPos passedPos,
            Player passedPlayer, InteractionHand passedHand, BlockHitResult hit) {
        LunarArcCauldronContext.setDirection(hit.getDirection());
        try {
            return original.call(interaction, state, level, pos, player, hand, stack);
        } finally {
            LunarArcCauldronContext.clear();
        }
    }
}
