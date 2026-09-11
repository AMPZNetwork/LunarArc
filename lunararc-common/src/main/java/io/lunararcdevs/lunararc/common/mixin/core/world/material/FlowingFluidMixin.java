package io.lunararcdevs.lunararc.common.mixin.core.world.material;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.lunararcdevs.lunararc.common.LunarArcDebug;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FlowingFluid;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.block.BlockFromToEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FlowingFluid.class)
public abstract class FlowingFluidMixin {

    @Inject(method = "tick", at = @At("HEAD"), require = 0)
    private void lunararc$traceTick(Level level, BlockPos pos, FluidState fluidState, CallbackInfo ci) {
        if (!LunarArcDebug.FLUID) return;
        LunarArcDebug.fluid("tick {} at {} amount={} source={} level={}",
                fluidState.getType(), pos, fluidState.getAmount(), fluidState.isSource(),
                level.getClass().getName());
    }

    @Inject(method = "spread", at = @At("HEAD"), require = 0)
    private void lunararc$traceSpread(Level level, BlockPos pos, FluidState fluidState, CallbackInfo ci) {
        if (!LunarArcDebug.FLUID) return;
        LunarArcDebug.fluid("spread {} at {} source={} logicWorld={} listeners={}",
                fluidState.getType(), pos, fluidState.isSource(),
                io.lunararcdevs.lunararc.common.mod.util.LunarArcLogicWorlds.isLogicWorld(level),
                BlockFromToEvent.getHandlerList().getRegisteredListeners().length);
    }

    @Inject(
            method = "spread",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FlowingFluid;spreadTo(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/material/FluidState;)V"),
            cancellable = true)
    private void lunararc$blockFromToDown(Level level, BlockPos pos, FluidState fluidState, CallbackInfo ci) {
        boolean cancelled = lunararc$flowCancelled(level, pos, Direction.DOWN);
        if (LunarArcDebug.FLUID) {
            LunarArcDebug.fluid("spread down from {} cancelled={}", pos, cancelled);
        }
        if (cancelled) {
            ci.cancel();
        }
    }

    @WrapOperation(
            method = "spreadToSides",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FlowingFluid;canSpreadTo(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/material/Fluid;)Z"),
            require = 0)
    private boolean lunararc$blockFromToSides(
            FlowingFluid self,
            net.minecraft.world.level.BlockGetter reader,
            BlockPos fromPos,
            BlockState fromState,
            Direction direction,
            BlockPos toPos,
            BlockState toState,
            FluidState toFluid,
            net.minecraft.world.level.material.Fluid fluid,
            Operation<Boolean> original) {
        boolean vanilla = original.call(self, reader, fromPos, fromState, direction, toPos, toState, toFluid, fluid);
        boolean cancelled = vanilla && reader instanceof Level level && lunararc$flowCancelled(level, fromPos, direction);
        if (LunarArcDebug.FLUID) {
            LunarArcDebug.fluid("spread {} from {} vanillaAllows={} cancelled={}",
                    direction, fromPos, vanilla, cancelled);
        }
        return vanilla && !cancelled;
    }

    @Unique
    private static boolean lunararc$flowCancelled(Level level, BlockPos source, Direction direction) {
        if (BlockFromToEvent.getHandlerList().getRegisteredListeners().length == 0) return false;
        if (!(level instanceof ServerLevel serverLevel)
                || !io.lunararcdevs.lunararc.common.mod.util.LunarArcLogicWorlds.isLogicWorld(serverLevel)) {
            return false;
        }
        BlockFromToEvent event = new BlockFromToEvent(
                CraftBlock.at(serverLevel, source), CraftBlock.notchToBlockFace(direction));
        Bukkit.getPluginManager().callEvent(event);
        return event.isCancelled();
    }
}
