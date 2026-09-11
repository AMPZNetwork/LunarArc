package io.lunararcdevs.lunararc.common.mixin.core.world.block.piston;

import com.llamalad7.mixinextras.sugar.Local;
import io.lunararcdevs.lunararc.common.mod.util.LunarArcLogicWorlds;
import io.lunararcdevs.lunararc.common.mod.util.LunarArcPistonAffectedBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PistonBaseBlock.class)
public abstract class PistonBaseBlockMixin {

    @Inject(
            method = "moveBlocks",
            at = @At(value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/level/block/piston/PistonStructureResolver;getToDestroy()Ljava/util/List;"),
            cancellable = true,
            require = 0)
    private void lunararc$pistonMoveEvent(Level level, BlockPos pos, Direction facing, boolean extending,
            CallbackInfoReturnable<Boolean> cir, @Local PistonStructureResolver resolver) {
        // Guarded by the logic-world test rather than instanceof ServerLevel: mods run block
        // mechanics against simulated and scratch levels that are ServerLevel subclasses, and a
        // Bukkit event naming blocks in a world no plugin has seen is noise at best.
        if (!(level instanceof ServerLevel serverLevel) || !LunarArcLogicWorlds.isLogicWorld(serverLevel)) {
            return;
        }

        org.bukkit.block.Block piston = CraftBlock.at(serverLevel, pos);
        List<BlockPos> moved = resolver.getToPush();
        List<BlockPos> broken = resolver.getToDestroy();
        Direction direction = extending ? facing : facing.getOpposite();

        BlockPistonEvent event = extending
                ? new BlockPistonExtendEvent(piston,
                        new LunarArcPistonAffectedBlocks(piston.getWorld(), moved, broken),
                        CraftBlock.notchToBlockFace(direction))
                : new BlockPistonRetractEvent(piston,
                        new LunarArcPistonAffectedBlocks(piston.getWorld(), moved, broken),
                        CraftBlock.notchToBlockFace(direction));
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) return;

        for (BlockPos position : broken) {
            serverLevel.sendBlockUpdated(position, Blocks.AIR.defaultBlockState(),
                    serverLevel.getBlockState(position), 3);
        }
        for (BlockPos position : moved) {
            serverLevel.sendBlockUpdated(position, Blocks.AIR.defaultBlockState(),
                    serverLevel.getBlockState(position), 3);
            BlockPos ahead = position.relative(direction);
            serverLevel.sendBlockUpdated(ahead, Blocks.AIR.defaultBlockState(),
                    serverLevel.getBlockState(ahead), 3);
        }
        cir.setReturnValue(false);
    }
}
