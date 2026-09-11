package io.lunararcdevs.lunararc.common.mixin.core.entity.monster;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Ravager.class)
public abstract class RavagerMixin {

    @WrapOperation(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;destroyBlock("
                            + "Lnet/minecraft/core/BlockPos;Z"
                            + "Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean lunararc$ravagerBreakLeaves(
            Level level, BlockPos pos, boolean drop, Entity breaker, Operation<Boolean> original) {
        // Read before the block goes, so the event describes the position after the break.
        net.minecraft.world.level.block.state.BlockState after =
                level.getBlockState(pos).getFluidState().createLegacyBlock();
        if (!CraftEventFactory.callEntityChangeBlockEvent((Ravager) (Object) this, pos, after)) {
            return false;
        }
        return original.call(level, pos, drop, breaker);
    }
}
