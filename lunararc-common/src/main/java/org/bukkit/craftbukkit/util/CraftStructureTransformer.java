package org.bukkit.craftbukkit.util;

import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import java.util.Collection;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.generator.CraftLimitedRegion;
import org.bukkit.util.BlockTransformer;
import org.bukkit.util.BlockTransformer.TransformationState;
import org.bukkit.util.EntityTransformer;

public final class CraftStructureTransformer {
    private static final class CraftTransformationState implements TransformationState {
        private final BlockState original;
        private final BlockState world;
        private BlockState originalCopy;
        private BlockState worldCopy;

        private CraftTransformationState(BlockState original, BlockState world) {
            this.original = original;
            this.world = world;
        }

        @Override
        public BlockState getOriginal() {
            if (originalCopy == null) originalCopy = original.copy();
            return originalCopy;
        }

        @Override
        public BlockState getWorld() {
            if (worldCopy == null) worldCopy = world.copy();
            return worldCopy;
        }

        private void clearCopies() {
            originalCopy = null;
            worldCopy = null;
        }
    }

    private CraftLimitedRegion limitedRegion;
    private BlockTransformer[] blockTransformers;
    private EntityTransformer[] entityTransformers;

    public CraftStructureTransformer(WorldGenLevel access, ChunkPos center,
            Collection<BlockTransformer> blockTransformers,
            Collection<EntityTransformer> entityTransformers) {
        this.blockTransformers = Objects.requireNonNull(blockTransformers, "blockTransformers").toArray(BlockTransformer[]::new);
        this.entityTransformers = Objects.requireNonNull(entityTransformers, "entityTransformers").toArray(EntityTransformer[]::new);
        this.limitedRegion = new CraftLimitedRegion(Objects.requireNonNull(access, "access"), Objects.requireNonNull(center, "center"));
    }

    public boolean canTransformBlocks() {
        return limitedRegion != null && blockTransformers != null && blockTransformers.length != 0;
    }

    public boolean transformEntity(Entity entity) {
        EntityTransformer[] transformers = entityTransformers;
        CraftLimitedRegion region = limitedRegion;
        if (region == null || transformers == null || transformers.length == 0) return true;

        org.bukkit.entity.Entity bukkit = ((EntityBridge) entity).lunararc$getBukkitEntity();
        if (bukkit == null) return true;
        int x = entity.getBlockX();
        int y = entity.getBlockY();
        int z = entity.getBlockZ();
        boolean allowed = true;
        for (EntityTransformer transformer : transformers) {
            allowed = transformer.transform(region, x, y, z, bukkit, allowed);
        }
        return allowed;
    }

    public CraftBlockState transformCraftState(CraftBlockState originalState) {
        BlockTransformer[] transformers = blockTransformers;
        CraftLimitedRegion region = limitedRegion;
        if (region == null || transformers == null || transformers.length == 0) return originalState;

        BlockPos pos = new BlockPos(originalState.getX(), originalState.getY(), originalState.getZ());
        BlockState transformed = originalState.copy();
        CraftTransformationState state = new CraftTransformationState(
                originalState,
                region.getBlockState(pos.getX(), pos.getY(), pos.getZ()));
        for (BlockTransformer transformer : transformers) {
            transformed = Objects.requireNonNull(
                    transformer.transform(region, pos.getX(), pos.getY(), pos.getZ(), transformed, state),
                    "BlockTransformer returned null");
            state.clearCopies();
        }
        if (!(transformed instanceof CraftBlockState craft)) {
            throw new IllegalArgumentException("BlockTransformer must return a CraftBlockState-backed BlockState");
        }
        return craft;
    }

    public void discard() {
        CraftLimitedRegion region = limitedRegion;
        try {
            if (region != null) region.saveEntities();
        } finally {
            if (region != null) region.breakLink();
            limitedRegion = null;
            blockTransformers = null;
            entityTransformers = null;
        }
    }
}
