package io.lunararcdevs.lunararc.common.mixin.core.world.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
public abstract class FireBlockMixin {

    @Unique
    private BlockPos lunararc$tickingFire;

    @Inject(method = "tick", at = @At("HEAD"))
    private void lunararc$recordTickingFire(
            BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        this.lunararc$tickingFire = pos;
    }

    // setBlock is declared on Level but invoked here through tick's ServerLevel parameter, and the
    // owner written into a call site is the static type of the receiver - so ServerLevel, matching
    // PortalForcerMixin, which already targets this same method the same way.
    @WrapOperation(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;setBlock("
                            + "Lnet/minecraft/core/BlockPos;"
                            + "Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean lunararc$blockIgniteOnSpread(
            ServerLevel level,
            BlockPos target,
            BlockState newState,
            int flags,
            Operation<Boolean> original) {
        BlockPos source = this.lunararc$tickingFire;
        if (source != null && !target.equals(source)
                && CraftEventFactory.callBlockIgniteEvent(level, target, source).isCancelled()) {
            // Vanilla continues the spread loop after a failed placement, so reporting "not placed"
            // is exactly what cancelling means here.
            return false;
        }
        return original.call(level, target, newState, flags);
    }
}
