package org.bukkit.craftbukkit.util;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.TickPriority;
import org.bukkit.craftbukkit.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.block.CraftBlockState;

public final class TransformerGeneratorAccess extends DummyGeneratorAccess {
    private final WorldGenLevel handle;
    private final CraftStructureTransformer structureTransformer;

    public TransformerGeneratorAccess(WorldGenLevel handle, CraftStructureTransformer structureTransformer) {
        this.handle = java.util.Objects.requireNonNull(handle, "handle");
        this.structureTransformer = java.util.Objects.requireNonNull(structureTransformer, "structureTransformer");
    }

    public CraftStructureTransformer getStructureTransformer() { return structureTransformer; }

    @Override public long getSeed() { return handle.getSeed(); }
    @Override public ServerLevel getLevel() { return handle.getLevel(); }
    @Override public long nextSubTickCount() { return handle.nextSubTickCount(); }
    @Override public LevelTickAccess<Block> getBlockTicks() { return handle.getBlockTicks(); }
    @Override public void scheduleTick(BlockPos pos, Block block, int delay) { handle.scheduleTick(pos, block, delay); }
    @Override public LevelTickAccess<Fluid> getFluidTicks() { return handle.getFluidTicks(); }
    @Override public LevelData getLevelData() { return handle.getLevelData(); }
    @Override public DifficultyInstance getCurrentDifficultyAt(BlockPos pos) { return handle.getCurrentDifficultyAt(pos); }
    @Override public MinecraftServer getServer() { return handle.getServer(); }
    @Override public ChunkSource getChunkSource() { return handle.getChunkSource(); }
    @Override public RandomSource getRandom() { return handle.getRandom(); }
    @Override public void playSound(Player source, BlockPos pos, SoundEvent sound, SoundSource category, float volume, float pitch) { handle.playSound(source, pos, sound, category, volume, pitch); }
    @Override public void addParticle(ParticleOptions parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) { handle.addParticle(parameters, x, y, z, velocityX, velocityY, velocityZ); }
    @Override public void levelEvent(Player player, int eventId, BlockPos pos, int data) { handle.levelEvent(player, eventId, pos, data); }
    @Override public void gameEvent(Holder<GameEvent> event, Vec3 emitterPos, GameEvent.Context emitter) { handle.gameEvent(event, emitterPos, emitter); }
    @Override public List<Entity> getEntities(Entity except, AABB box, Predicate<? super Entity> predicate) { return handle.getEntities(except, box, predicate); }
    @Override public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> filter, AABB box, Predicate<? super T> predicate) { return handle.getEntities(filter, box, predicate); }
    @Override public List<? extends Player> players() { return handle.players(); }
    @Override public ChunkAccess getChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) { return handle.getChunk(chunkX, chunkZ, leastStatus, create); }
    @Override public int getHeight(Heightmap.Types heightmap, int x, int z) { return handle.getHeight(heightmap, x, z); }
    @Override public int getSkyDarken() { return handle.getSkyDarken(); }
    @Override public BiomeManager getBiomeManager() { return handle.getBiomeManager(); }
    @Override public Holder<Biome> getUncachedNoiseBiome(int biomeX, int biomeY, int biomeZ) { return handle.getUncachedNoiseBiome(biomeX, biomeY, biomeZ); }
    @Override public boolean isClientSide() { return handle.isClientSide(); }
    @Override public int getSeaLevel() { return handle.getSeaLevel(); }
    @Override public DimensionType dimensionType() { return handle.dimensionType(); }
    @Override public RegistryAccess registryAccess() { return handle.registryAccess(); }
    @Override public FeatureFlagSet enabledFeatures() { return handle.enabledFeatures(); }
    @Override public float getShade(Direction direction, boolean shaded) { return handle.getShade(direction, shaded); }
    @Override public LevelLightEngine getLightEngine() { return handle.getLightEngine(); }
    @Override public BlockEntity getBlockEntity(BlockPos pos) { return handle.getBlockEntity(pos); }
    @Override public BlockState getBlockState(BlockPos pos) { return handle.getBlockState(pos); }
    @Override public FluidState getFluidState(BlockPos pos) { return handle.getFluidState(pos); }
    @Override public FluidState getFluidIfLoaded(BlockPos pos) { return handle.getFluidState(pos); }
    @Override public WorldBorder getWorldBorder() { return handle.getWorldBorder(); }
    @Override public boolean isStateAtPosition(BlockPos pos, Predicate<BlockState> state) { return handle.isStateAtPosition(pos, state); }
    @Override public boolean isFluidAtPosition(BlockPos pos, Predicate<FluidState> state) { return handle.isFluidAtPosition(pos, state); }
    @Override public boolean removeBlock(BlockPos pos, boolean move) { return handle.removeBlock(pos, move); }
    @Override public boolean destroyBlock(BlockPos pos, boolean drop, Entity breakingEntity, int maxUpdateDepth) { return handle.destroyBlock(pos, drop, breakingEntity, maxUpdateDepth); }
    @Override public void scheduleTick(BlockPos pos, Fluid fluid, int delay) { handle.scheduleTick(pos, fluid, delay); }
    @Override public void scheduleTick(BlockPos pos, Block block, int delay, TickPriority priority) { handle.scheduleTick(pos, block, delay, priority); }
    @Override public void scheduleTick(BlockPos pos, Fluid fluid, int delay, TickPriority priority) { handle.scheduleTick(pos, fluid, delay, priority); }
    @Override public ChunkAccess getChunkIfLoadedImmediately(int x, int z) { return handle.getChunk(x, z, ChunkStatus.FULL, false); }
    @Override public BlockState getBlockStateIfLoaded(BlockPos pos) { return handle.getBlockState(pos); }

    @Override
    public boolean addFreshEntity(Entity entity) {
        if (!structureTransformer.transformEntity(entity)) return false;
        return handle.addFreshEntity(entity);
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState state, int flags, int maxUpdateDepth) {
        if (!structureTransformer.canTransformBlocks()) {
            return handle.setBlock(pos, state, flags, maxUpdateDepth);
        }
        CraftBlockState craft = structureTransformer.transformCraftState(CraftBlockState.unplacedAt(pos, state));
        boolean result = handle.setBlock(pos, craft.getHandle(), flags, maxUpdateDepth);
        if (!result) return false;

        FluidState fluid = handle.getFluidState(pos);
        if (!fluid.isEmpty()) handle.scheduleTick(pos, fluid.getType(), 0);

        BlockEntity tile = handle.getBlockEntity(pos);
        if (tile != null && craft instanceof CraftBlockEntityState<?> blockEntityState) {
            tile.loadWithComponents(blockEntityState.getSnapshotNBT(), handle.registryAccess());
            tile.setChanged();
        }
        return true;
    }
}
