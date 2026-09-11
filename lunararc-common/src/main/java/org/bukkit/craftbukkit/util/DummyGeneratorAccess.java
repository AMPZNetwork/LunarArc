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
import net.minecraft.world.level.block.Blocks;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;

public class DummyGeneratorAccess implements WorldGenLevel {
    protected DummyGeneratorAccess() {}

    @Override public long getSeed() { throw unsupported(); }
    @Override public ServerLevel getLevel() { throw unsupported(); }
    @Override public long nextSubTickCount() { throw unsupported(); }
    @Override public LevelTickAccess<Block> getBlockTicks() { return BlackholeTickAccess.emptyLevelList(); }
    @Override public void scheduleTick(BlockPos pos, Block block, int delay) {}
    @Override public LevelTickAccess<Fluid> getFluidTicks() { return BlackholeTickAccess.emptyLevelList(); }
    @Override public LevelData getLevelData() { throw unsupported(); }
    @Override public DifficultyInstance getCurrentDifficultyAt(BlockPos pos) { throw unsupported(); }
    @Override public MinecraftServer getServer() { throw unsupported(); }
    @Override public ChunkSource getChunkSource() { throw unsupported(); }
    @Override public RandomSource getRandom() { throw unsupported(); }
    @Override public void playSound(Player source, BlockPos pos, SoundEvent sound, SoundSource category, float volume, float pitch) { throw unsupported(); }
    @Override public void addParticle(ParticleOptions parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ) { throw unsupported(); }
    @Override public void levelEvent(Player player, int eventId, BlockPos pos, int data) {}
    @Override public void gameEvent(Holder<GameEvent> event, Vec3 emitterPos, GameEvent.Context emitter) {}
    @Override public List<Entity> getEntities(Entity except, AABB box, Predicate<? super Entity> predicate) { throw unsupported(); }
    @Override public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> filter, AABB box, Predicate<? super T> predicate) { throw unsupported(); }
    @Override public List<? extends Player> players() { throw unsupported(); }
    @Override public ChunkAccess getChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create) { throw unsupported(); }
    @Override public int getHeight(Heightmap.Types heightmap, int x, int z) { throw unsupported(); }
    @Override public int getSkyDarken() { throw unsupported(); }
    @Override public BiomeManager getBiomeManager() { throw unsupported(); }
    @Override public Holder<Biome> getUncachedNoiseBiome(int biomeX, int biomeY, int biomeZ) { throw unsupported(); }
    @Override public boolean isClientSide() { return false; }
    @Override public int getSeaLevel() { throw unsupported(); }
    @Override public DimensionType dimensionType() { throw unsupported(); }
    @Override public RegistryAccess registryAccess() { throw unsupported(); }
    @Override public FeatureFlagSet enabledFeatures() { throw unsupported(); }
    @Override public float getShade(Direction direction, boolean shaded) { throw unsupported(); }
    @Override public LevelLightEngine getLightEngine() { throw unsupported(); }
    @Override public BlockEntity getBlockEntity(BlockPos pos) { return null; }
    @Override public BlockState getBlockState(BlockPos pos) { return Blocks.AIR.defaultBlockState(); }
    @Override public FluidState getFluidState(BlockPos pos) { return Fluids.EMPTY.defaultFluidState(); }

    public FluidState getFluidIfLoaded(BlockPos pos) {
        return Fluids.EMPTY.defaultFluidState();
    }
    @Override public WorldBorder getWorldBorder() { throw unsupported(); }
    @Override public boolean isStateAtPosition(BlockPos pos, Predicate<BlockState> state) { throw unsupported(); }
    @Override public boolean isFluidAtPosition(BlockPos pos, Predicate<FluidState> state) { throw unsupported(); }
    @Override public boolean setBlock(BlockPos pos, BlockState state, int flags, int maxUpdateDepth) { return false; }
    @Override public boolean removeBlock(BlockPos pos, boolean move) { throw unsupported(); }
    @Override public boolean destroyBlock(BlockPos pos, boolean drop, Entity breakingEntity, int maxUpdateDepth) { return false; }
    @Override public void scheduleTick(BlockPos pos, Fluid fluid, int delay) {}
    @Override public void scheduleTick(BlockPos pos, Block block, int delay, net.minecraft.world.ticks.TickPriority priority) {}
    @Override public void scheduleTick(BlockPos pos, Fluid fluid, int delay, net.minecraft.world.ticks.TickPriority priority) {}
    public ChunkAccess getChunkIfLoadedImmediately(int x, int z) { throw unsupported(); }
    public BlockState getBlockStateIfLoaded(BlockPos pos) { throw unsupported(); }

    private static UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException("Operation is not available on the tree-generation capture view");
    }
}
