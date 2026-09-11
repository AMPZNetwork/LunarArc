package io.lunararcdevs.lunararc.common.mod.util;

import net.minecraft.core.BlockPos;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.AbstractList;
import java.util.List;

/** The blocks a piston is about to move, as {@code BlockPistonEvent.getBlocks()} wants them: moved blocks first, then the ones being destroyed. */
public final class LunarArcPistonAffectedBlocks extends AbstractList<Block> {

    private final World world;
    private final List<BlockPos> moved;
    private final List<BlockPos> broken;

    public LunarArcPistonAffectedBlocks(World world, List<BlockPos> moved, List<BlockPos> broken) {
        this.world = world;
        this.moved = moved;
        this.broken = broken;
    }

    @Override
    public int size() {
        return moved.size() + broken.size();
    }

    @Override
    public Block get(int index) {
        if (index >= size() || index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        BlockPos position = index < moved.size() ? moved.get(index) : broken.get(index - moved.size());
        return world.getBlockAt(position.getX(), position.getY(), position.getZ());
    }
}
