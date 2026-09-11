package io.lunararcdevs.lunararc.common.mixin.core.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin {

    @Unique
    private record LunarArcBucketEmptyContext(BlockPos clicked, net.minecraft.core.Direction face,
                                                InteractionHand hand, ItemStack input) {}

    @Unique private static final ThreadLocal<ItemStack> LUNARARC_BUCKET_RESULT = new ThreadLocal<>();
    @Unique private static final ThreadLocal<LunarArcBucketEmptyContext> LUNARARC_BUCKET_EMPTY = new ThreadLocal<>();

    @Inject(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BucketPickup;pickupBlock(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/item/ItemStack;"),
            cancellable = true,
            require = 0)
    private void lunararc$bucketFillBefore(Level level, Player player, InteractionHand hand,
            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir,
            @Local BlockHitResult hit,
            @Local BlockPos blockPos,
            @Local BlockState blockState,
            @Local BucketPickup pickup,
            @Local ItemStack stack) {
        LUNARARC_BUCKET_RESULT.remove();
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) return;

        // Vanilla fluid pickups can be predicted without mutating the world.
        // Powder snow is the one vanilla BucketPickup whose result is not
        // represented by FluidState. Unknown modded non-fluid BucketPickup
        // implementations are deliberately left to the loader rather than
        // calling pickupBlock twice and risking mod side effects.
        net.minecraft.world.level.material.FluidState fluidState = serverLevel.getFluidState(blockPos);
        ItemStack predicted;
        if (!fluidState.isEmpty()) {
            predicted = new ItemStack(fluidState.getType().getBucket());
        } else if (blockState.is(Blocks.POWDER_SNOW)) {
            predicted = new ItemStack(Items.POWDER_SNOW_BUCKET);
        } else {
            return;
        }
        if (predicted.isEmpty()) return;

        var event = org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerBucketFillEvent(
                serverLevel, serverPlayer, blockPos, hit.getBlockPos(), hit.getDirection(), stack, predicted, hand);
        if (event == null) return;
        if (event.isCancelled()) {
            lunararc$resyncCancelledBucket(serverPlayer, level, blockPos, stack);
            cir.setReturnValue(InteractionResultHolder.fail(stack));
            return;
        }
        LUNARARC_BUCKET_RESULT.set(event.getItemStack() == null
                ? ItemStack.EMPTY : CraftItemStack.asNMSCopy(event.getItemStack()));
    }

    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemUtils;createFilledResult(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"),
            require = 0)
    private ItemStack lunararc$bucketFillResult(ItemStack input, Player player, ItemStack vanillaResult,
                                                 Operation<ItemStack> original) {
        ItemStack override = LUNARARC_BUCKET_RESULT.get();
        if (override != null) LUNARARC_BUCKET_RESULT.remove();
        return original.call(input, player, override == null ? vanillaResult : override);
    }

    @Inject(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BucketItem;emptyContents(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/BlockHitResult;)Z"),
            require = 0)
    private void lunararc$captureBucketEmptyVanilla(Level level, Player player, InteractionHand hand,
            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir,
            @Local BlockHitResult hit,
            @Local ItemStack stack) {
        lunararc$captureBucketEmpty(hit, hand, stack);
    }

    // Forge/NeoForge 1.21.1 may route BucketItem#use through their ItemStack-
    // sensitive emptyContents overload. This optional target keeps the common
    // event context available without replacing either loader implementation.
    @Inject(
            method = "use",
            // remap = false because this overload is the loader's own addition and is in no vanilla
            // mapping, so asking for it to be remapped can only fail to find it. Arclight marks the
            // same call site the same way. Harmless on NeoForge, whose runtime is Mojang-mapped
            // anyway; it is Forge, remapping to SRG, where the difference shows.
            at = @At(value = "INVOKE", remap = false, target = "Lnet/minecraft/world/item/BucketItem;emptyContents(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/BlockHitResult;Lnet/minecraft/world/item/ItemStack;)Z"),
            require = 0)
    private void lunararc$captureBucketEmptyLoader(Level level, Player player, InteractionHand hand,
            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir,
            @Local BlockHitResult hit,
            @Local ItemStack stack) {
        lunararc$captureBucketEmpty(hit, hand, stack);
    }

    @Unique
    private static void lunararc$captureBucketEmpty(BlockHitResult hit, InteractionHand hand, ItemStack stack) {
        LUNARARC_BUCKET_RESULT.remove();
        LUNARARC_BUCKET_EMPTY.set(new LunarArcBucketEmptyContext(
                hit.getBlockPos(), hit.getDirection(), hand, stack.copy()));
    }

    // This point is reached only after vanilla has established that pPos can
    // accept the fluid, but before ultra-warm vaporisation/placeLiquid/setBlock.
    @Inject(
            method = "emptyContents(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/BlockHitResult;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/DimensionType;ultraWarm()Z"),
            cancellable = true,
            require = 0)
    private void lunararc$bucketEmptyBeforeMutation(Player player, Level level, BlockPos changed,
            BlockHitResult hit, CallbackInfoReturnable<Boolean> cir) {
        lunararc$fireBucketEmpty(player, level, changed, cir);
    }

    // The five-argument emptyContents, taking the container stack, is a loader addition; vanilla
    // has only the four-argument form injected above. lunararc-common compiles against vanilla, so
    // the processor reports this target as not found - correctly, for the reference it can see -
    // while at runtime it resolves on the loaders that add it. require = 0 is what makes that
    // legal: apply where the overload exists, stay out of the way where it does not.
    @Inject(
            method = "emptyContents(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/BlockHitResult;Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/DimensionType;ultraWarm()Z"),
            cancellable = true,
            require = 0)
    private void lunararc$bucketEmptyBeforeLoaderMutation(Player player, Level level, BlockPos changed,
            BlockHitResult hit, ItemStack container, CallbackInfoReturnable<Boolean> cir) {
        lunararc$fireBucketEmpty(player, level, changed, cir);
    }

    @Unique
    private static void lunararc$fireBucketEmpty(Player player, Level level, BlockPos changed,
                                                  CallbackInfoReturnable<Boolean> cir) {
        LunarArcBucketEmptyContext context = LUNARARC_BUCKET_EMPTY.get();
        if (context == null || !(level instanceof ServerLevel serverLevel)
                || !(player instanceof ServerPlayer serverPlayer)) return;

        // Consume the context at the successful pre-mutation point so recursive
        // fallback attempts cannot double-fire the Bukkit event.
        LUNARARC_BUCKET_EMPTY.remove();
        var event = org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerBucketEmptyEvent(
                serverLevel, serverPlayer, changed, context.clicked(), context.face(),
                context.input(), new ItemStack(Items.BUCKET), context.hand());
        if (event == null) return;
        if (event.isCancelled()) {
            lunararc$resyncCancelledBucket(serverPlayer, level, changed, context.input());
            cir.setReturnValue(false);
            return;
        }
        LUNARARC_BUCKET_RESULT.set(event.getItemStack() == null
                ? ItemStack.EMPTY : CraftItemStack.asNMSCopy(event.getItemStack()));
    }

    @Inject(
            method = "emptyContents(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/BlockHitResult;)Z",
            at = @At("RETURN"), require = 0)
    private void lunararc$traceEmptyVanilla(Player player, Level level, BlockPos pos, BlockHitResult hit,
            CallbackInfoReturnable<Boolean> cir) {
        lunararc$traceEmptyContents("vanilla-4arg", level, pos, cir.getReturnValueZ());
    }

    @Inject(
            method = "emptyContents(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/BlockHitResult;Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At("RETURN"), require = 0)
    private void lunararc$traceEmptyLoader(Player player, Level level, BlockPos pos, BlockHitResult hit,
            ItemStack container, CallbackInfoReturnable<Boolean> cir) {
        lunararc$traceEmptyContents("loader-5arg", level, pos, cir.getReturnValueZ());
    }

    @Unique
    private static void lunararc$traceEmptyContents(String overload, Level level, BlockPos pos, boolean result) {
        if (!io.lunararcdevs.lunararc.common.LunarArcDebug.FLUID) return;
        if (!(level instanceof ServerLevel serverLevel)) {
            io.lunararcdevs.lunararc.common.LunarArcDebug.fluid(
                    "emptyContents({}) at {} returned {} on a non-server level {}",
                    overload, pos, result, level.getClass().getName());
            return;
        }
        BlockState placed = serverLevel.getBlockState(pos);
        net.minecraft.world.level.material.FluidState fluid = serverLevel.getFluidState(pos);
        boolean scheduled = !fluid.isEmpty()
                && serverLevel.getFluidTicks().hasScheduledTick(pos, fluid.getType());
        io.lunararcdevs.lunararc.common.LunarArcDebug.fluid(
                "emptyContents({}) at {} returned {} -> block={} fluid={} source={} amount={} tickScheduled={}",
                overload, pos, result, placed.getBlock(),
                fluid.isEmpty() ? "empty" : fluid.getType(), fluid.isSource(), fluid.getAmount(), scheduled);
    }

    @WrapOperation(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BucketItem;getEmptySuccessItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/item/ItemStack;"),
            require = 0)
    private ItemStack lunararc$bucketEmptyResult(ItemStack input, Player player, Operation<ItemStack> original) {
        ItemStack override = LUNARARC_BUCKET_RESULT.get();
        if (override == null) return original.call(input, player);
        LUNARARC_BUCKET_RESULT.remove();
        return override;
    }

    @Inject(method = "use", at = @At("RETURN"), require = 0)
    private void lunararc$clearBucketContext(Level level, Player player, InteractionHand hand,
            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        LUNARARC_BUCKET_RESULT.remove();
        LUNARARC_BUCKET_EMPTY.remove();
    }

    @Unique
    private static void lunararc$resyncCancelledBucket(ServerPlayer player, Level level,
                                                        BlockPos changed, ItemStack input) {
        if (player.connection != null) {
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket(level, changed));
        }
        Object bukkit = ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) player).lunararc$getBukkitEntity();
        if (bukkit instanceof org.bukkit.craftbukkit.entity.CraftPlayer craftPlayer) craftPlayer.updateInventory();
    }
}
