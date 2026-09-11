package org.bukkit.craftbukkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.generator.structure.GeneratedStructure;
import org.bukkit.generator.structure.Structure;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;


public final class CraftChunk implements Chunk {
    private final CraftWorld world;
    private final int x;
    private final int z;
    private final LevelChunk knownHandle;

    public CraftChunk(CraftWorld world, int x, int z) {
        this.world = Objects.requireNonNull(world, "world");
        this.x = x;
        this.z = z;
        this.knownHandle = null;
    }

    public CraftChunk(LevelChunk chunk, CraftWorld world) {
        this.world = Objects.requireNonNull(world, "world");
        this.x = chunk.getPos().x;
        this.z = chunk.getPos().z;
        this.knownHandle = chunk;
    }

    public LevelChunk getHandle() {
        // A real, live confirmed deadlock: ChunkLoadEvent hands out a CraftChunk built directly
        // from the chunk currently completing its own load - looking that same chunk back up via
        // the chunk map from inside its own load-event listener (ClearLaggEnhanced's
        // GlobalEntityRegistry does exactly this) blocks the server thread waiting on the very
        // load task it is itself running. We already hold the real object here, so skip the
        // lookup entirely rather than trying to detect reentrancy after the fact.
        return knownHandle != null ? knownHandle : world.getHandle().getChunk(x, z);
    }

    private LevelChunk getHandleIfLoaded() {
        return world.getHandle().getChunkSource().getChunkNow(x, z);
    }

    @Override public int getX() { return x; }
    @Override public int getZ() { return z; }
    @Override public @NotNull World getWorld() { return world; }

    @Override
    public @NotNull Block getBlock(int localX, int y, int localZ) {
        if (localX < 0 || localX > 15) throw new IllegalArgumentException("x out of range (expected 0-15, got " + localX + ")");
        if (localZ < 0 || localZ > 15) throw new IllegalArgumentException("z out of range (expected 0-15, got " + localZ + ")");
        if (y < world.getMinHeight() || y >= world.getMaxHeight()) throw new IllegalArgumentException("y out of range");
        return CraftBlock.at(world.getHandle(), new BlockPos((x << 4) + localX, y, (z << 4) + localZ));
    }

    @Override public @NotNull ChunkSnapshot getChunkSnapshot() { return getChunkSnapshot(true, true, true, true); }
    @Override public @NotNull ChunkSnapshot getChunkSnapshot(boolean maxY, boolean biome, boolean temp) { return getChunkSnapshot(maxY, biome, temp, false); }
    @Override public @NotNull ChunkSnapshot getChunkSnapshot(boolean maxY, boolean biome, boolean temp, boolean light) {
        return CraftChunkSnapshot.capture(this, maxY, biome, temp, light);
    }

    @Override public boolean isEntitiesLoaded() { return getHandleIfLoaded() != null; }

    @Override
    public @NotNull Entity[] getEntities() {

        getHandle();
        List<Entity> result = new ArrayList<>();
        for (net.minecraft.world.entity.Entity nms : world.getHandle().getAllEntities()) {
            if ((nms.getBlockX() >> 4) != x || (nms.getBlockZ() >> 4) != z) continue;
            try {
                Entity bukkit = (Entity) ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) nms).lunararc$getBukkitEntity();
                if (bukkit != null) result.add(bukkit);
            } catch (Throwable ignored) {}
        }
        return result.toArray(Entity[]::new);
    }

    @Override
    public @NotNull BlockState[] getTileEntities(boolean useSnapshot) {
        return getTileEntities(block -> true, useSnapshot).toArray(BlockState[]::new);
    }

    @Override
    public @NotNull Collection<BlockState> getTileEntities(@NotNull Predicate<? super Block> predicate, boolean useSnapshot) {
        Objects.requireNonNull(predicate, "blockPredicate");
        LevelChunk chunk = getHandle();
        List<BlockState> states = new ArrayList<>();
        for (BlockPos pos : chunk.getBlockEntities().keySet()) {
            Block block = CraftBlock.at(world.getHandle(), pos);
            if (predicate.test(block)) states.add(block.getState(useSnapshot));
        }
        return List.copyOf(states);
    }

    @Override public boolean isGenerated() { return getHandleIfLoaded() != null; }
    @Override public boolean isLoaded() { return world.isChunkLoaded(x, z); }
    @Override public boolean load() { return load(true); }
    @Override public boolean load(boolean generate) { return world.loadChunk(x, z, generate); }
    @Override public boolean unload() { return unload(true); }
    @Override public boolean unload(boolean save) { return world.unloadChunk(x, z, save); }

    @Override
    public boolean isSlimeChunk() {

        long seed = world.getSeed();
        java.util.Random random = new java.util.Random(seed + (long) (x * x * 4987142) + (long) (x * 5947611)
                + (long) (z * z) * 4392871L + (long) (z * 389711) ^ 987234911L);
        return random.nextInt(10) == 0;
    }

    @Override public boolean isForceLoaded() { return world.isChunkForceLoaded(x, z); }
    @Override public void setForceLoaded(boolean forced) { world.setChunkForceLoaded(x, z, forced); }
    @Override public boolean addPluginChunkTicket(@NotNull Plugin plugin) { return world.addPluginChunkTicket(x, z, plugin); }
    @Override public boolean removePluginChunkTicket(@NotNull Plugin plugin) { return world.removePluginChunkTicket(x, z, plugin); }
    @Override public @NotNull Collection<Plugin> getPluginChunkTickets() { return world.getPluginChunkTickets(x, z); }

    @Override public long getInhabitedTime() { return getHandle().getInhabitedTime(); }
    @Override public void setInhabitedTime(long ticks) {
        if (ticks < 0) throw new IllegalArgumentException("ticks cannot be negative");
        getHandle().setInhabitedTime(ticks);
    }

    @Override
    public boolean contains(@NotNull BlockData block) {
        Objects.requireNonNull(block, "block");
        if (!(block instanceof CraftBlockData craft)) return false;
        LevelChunk chunk = getHandle();
        for (var section : chunk.getSections()) {
            if (section != null && section.getStates().maybeHas(state -> state.equals(craft.getState()))) return true;
        }
        return false;
    }

    @Override
    public boolean contains(@NotNull Biome biome) {
        Objects.requireNonNull(biome, "biome");
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight();
        for (int yy = minY; yy < maxY; yy += 4) {
            for (int xx = 0; xx < 16; xx += 4) for (int zz = 0; zz < 16; zz += 4) {
                if (world.getBiome((x << 4) + xx, yy, (z << 4) + zz) == biome) return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull LoadLevel getLoadLevel() {
        LevelChunk chunk = getHandleIfLoaded();
        if (chunk == null) return LoadLevel.UNLOADED;
        try {
            int ordinal = chunk.getFullStatus().ordinal();
            LoadLevel[] levels = {LoadLevel.INACCESSIBLE, LoadLevel.BORDER, LoadLevel.TICKING, LoadLevel.ENTITY_TICKING};
            return levels[Math.max(0, Math.min(levels.length - 1, ordinal))];
        } catch (Throwable ignored) {
            return LoadLevel.INACCESSIBLE;
        }
    }

    @Override public @NotNull Collection<GeneratedStructure> getStructures() { return world.getStructures(x, z); }
    @Override public @NotNull Collection<GeneratedStructure> getStructures(@NotNull Structure structure) { return world.getStructures(x, z, structure); }
    @Override public @NotNull Collection<Player> getPlayersSeeingChunk() { return world.getPlayersSeeingChunk(x, z); }
    @Override
    public @NotNull PersistentDataContainer getPersistentDataContainer() {
        LevelChunk chunk = getHandle();
        if (!(chunk instanceof io.lunararcdevs.lunararc.common.bridge.LevelChunkBridge bridge)) {
            throw new IllegalStateException("LevelChunk bridge was not applied");
        }
        return bridge.lunararc$getPersistentDataContainer();
    }

    @Override public String toString() { return "CraftChunk{x=" + x + ",z=" + z + ",world=" + world.getName() + "}"; }
    @Override public int hashCode() { return Objects.hash(world.getUID(), x, z); }
    @Override public boolean equals(Object other) {
        return other instanceof CraftChunk that && x == that.x && z == that.z && world.getUID().equals(that.world.getUID());
    }

    /** CraftBukkit's narrowed {@code getWorld}; plugins holding a CraftChunk use it to skip the cast. */
    public CraftWorld getCraftWorld() {
        return (CraftWorld) this.getWorld();
    }
}
