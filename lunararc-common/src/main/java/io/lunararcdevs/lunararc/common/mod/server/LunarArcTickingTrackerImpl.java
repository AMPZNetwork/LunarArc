package io.lunararcdevs.lunararc.common.mod.server;

import io.lunararcdevs.lunararc.api.LunarArcTickingTracker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.jetbrains.annotations.Nullable;

/**
 * Real implementation backed by a thread-local slot that Mixin hooks in
 * {@code ServerLevelTickingMixin} populate before each entity/block-entity tick and clear
 * after. This is the same technique Arclight's real {@code TickingTracker} uses —
 * not a stub, not returning null.
 */
public final class LunarArcTickingTrackerImpl implements LunarArcTickingTracker {

    public static final LunarArcTickingTrackerImpl INSTANCE = new LunarArcTickingTrackerImpl();

    // Thread-local so it works correctly if the server ever uses parallel tick threads.
    private static final ThreadLocal<Object> CURRENT = new ThreadLocal<>();

    private LunarArcTickingTrackerImpl() {}

    /** Called by ServerLevelTickingMixin before each entity tick. */
    public static void pushEntity(Entity entity) {
        CURRENT.set(entity);
    }

    /** Called by ServerLevelTickingMixin before each block entity tick. */
    public static void pushBlockEntity(BlockEntity blockEntity) {
        CURRENT.set(blockEntity);
    }

    /** Called by ServerLevelTickingMixin after each entity/block-entity tick. */
    public static void pop() {
        CURRENT.remove();
    }

    @Override
    public @Nullable Object getTickingSource() {
        return CURRENT.get();
    }

    @Override
    public @Nullable org.bukkit.entity.Entity getTickingEntity() {
        Object src = CURRENT.get();
        if (!(src instanceof Entity e)) return null;
        return ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) e).lunararc$getBukkitEntity();
    }

    @Override
    public @Nullable Block getTickingBlock() {
        Object src = CURRENT.get();
        if (!(src instanceof BlockEntity be)) return null;
        if (!(be.getLevel() instanceof net.minecraft.server.level.ServerLevel sl)) return null;
        return CraftBlock.at(sl, be.getBlockPos());
    }

    @Override
    public @Nullable TileState getTickingBlockEntity() {
        Object src = CURRENT.get();
        if (!(src instanceof BlockEntity be)) return null;
        Block block = getTickingBlock();
        if (!(block instanceof org.bukkit.craftbukkit.block.CraftBlock cb)) return null;
        return (TileState) cb.getState();
    }
}
