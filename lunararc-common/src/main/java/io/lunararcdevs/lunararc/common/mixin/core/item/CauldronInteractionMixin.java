package io.lunararcdevs.lunararc.common.mixin.core.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.lunararcdevs.lunararc.common.bridge.LunarArcCauldronContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

/** Bukkit bucket events for the separate vanilla cauldron bucket path. */
@Mixin(CauldronInteraction.class)
public interface CauldronInteractionMixin {
    @Inject(method = "fillBucket", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/Level;isClientSide:Z"), cancellable = true, require = 0)
    private static void lunararc$bucketFillCauldron(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            ItemStack stack, ItemStack output, Predicate<BlockState> fullPredicate, SoundEvent sound,
            CallbackInfoReturnable<ItemInteractionResult> cir) {
        LunarArcCauldronContext.takeResult();
        Direction direction = LunarArcCauldronContext.direction();
        if (direction == null || !(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) return;
        var event = org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerBucketFillEvent(
                serverLevel, serverPlayer, pos, pos, direction, stack, output, hand);
        if (event == null) return;
        if (event.isCancelled()) {
            cir.setReturnValue(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
            return;
        }
        LunarArcCauldronContext.setResult(event.getItemStack() == null
                ? ItemStack.EMPTY : CraftItemStack.asNMSCopy(event.getItemStack()));
    }

    @WrapOperation(method = "fillBucket",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemUtils;createFilledResult(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"),
            require = 0)
    private static ItemStack lunararc$bucketFillCauldronResult(
            ItemStack input, Player player, ItemStack vanillaResult, Operation<ItemStack> original) {
        ItemStack override = LunarArcCauldronContext.takeResult();
        return original.call(input, player, override == null ? vanillaResult : override);
    }

    @Inject(method = "emptyBucket", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/Level;isClientSide:Z"), cancellable = true, require = 0)
    private static void lunararc$bucketEmptyCauldron(
            Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack,
            BlockState state, SoundEvent sound, CallbackInfoReturnable<ItemInteractionResult> cir) {
        LunarArcCauldronContext.takeResult();
        Direction direction = LunarArcCauldronContext.direction();
        if (direction == null || !(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) return;
        var event = org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerBucketEmptyEvent(
                serverLevel, serverPlayer, pos, pos, direction, stack, new ItemStack(Items.BUCKET), hand);
        if (event == null) return;
        if (event.isCancelled()) {
            cir.setReturnValue(ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION);
            return;
        }
        LunarArcCauldronContext.setResult(event.getItemStack() == null
                ? ItemStack.EMPTY : CraftItemStack.asNMSCopy(event.getItemStack()));
    }

    @WrapOperation(method = "emptyBucket",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemUtils;createFilledResult(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"),
            require = 0)
    private static ItemStack lunararc$bucketEmptyCauldronResult(
            ItemStack input, Player player, ItemStack vanillaResult, Operation<ItemStack> original) {
        ItemStack override = LunarArcCauldronContext.takeResult();
        return original.call(input, player, override == null ? vanillaResult : override);
    }
}
