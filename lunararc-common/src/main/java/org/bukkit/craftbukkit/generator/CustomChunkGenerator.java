package org.bukkit.craftbukkit.generator;

import com.mojang.serialization.MapCodec;
import io.lunararcdevs.lunararc.common.bridge.access.ChunkGeneratorAccessBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.bukkit.craftbukkit.CraftHeightMap;
import org.bukkit.craftbukkit.util.RandomSourceWrapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Concrete Bukkit ChunkGenerator adapter around the loader-owned NMS generator.
 * It never replaces loader lifecycle/registries; it only controls generation
 * stages according to Bukkit's ChunkGenerator contract.
 */
public final class CustomChunkGenerator extends ChunkGenerator {
    private final ChunkGenerator delegate;
    private final org.bukkit.generator.ChunkGenerator generator;
    private final CraftWorldInfo worldInfo;
    private volatile ServerLevel world;
    private boolean implementBaseHeight = true;

    public CustomChunkGenerator(ChunkGenerator delegate, org.bukkit.generator.ChunkGenerator generator,
                                CraftWorldInfo worldInfo, BiomeSource biomeSource) {
        super(biomeSource, ((ChunkGeneratorAccessBridge) (Object) delegate).lunararc$getGenerationSettingsGetter());
        this.delegate = delegate;
        this.generator = generator;
        this.worldInfo = worldInfo;
    }

    public void attachWorld(ServerLevel world) { this.world = world; }
    public ChunkGenerator getDelegate() { return this.delegate; }
    private ServerLevel world() {
        ServerLevel value = this.world;
        if (value == null) throw new IllegalStateException("CustomChunkGenerator used before ServerLevel attachment");
        return value;
    }
    private static WorldgenRandom seededRandom() { return new WorldgenRandom(new LegacyRandomSource(0L)); }
    private java.util.Random random(WorldgenRandom random) { return new RandomSourceWrapper.RandomWrapper(random); }
    private CraftChunkData data(ChunkAccess chunk) { return new CraftChunkData(this.worldInfo.getMinHeight(), this.worldInfo.getMaxHeight(), chunk); }

    @Override public BiomeSource getBiomeSource() { return super.getBiomeSource(); }
    @Override public int getMinY() { return this.delegate.getMinY(); }
    @Override public int getSeaLevel() { return this.delegate.getSeaLevel(); }
    @Override public int getGenDepth() { return this.delegate.getGenDepth(); }
    @Override public int getSpawnHeight(LevelHeightAccessor level) { return this.delegate.getSpawnHeight(level); }

    @Override
    public void createStructures(RegistryAccess access, ChunkGeneratorStructureState state, StructureManager structures,
                                 ChunkAccess chunk, StructureTemplateManager templates) {
        if (this.generator == null) {
            this.delegate.createStructures(access, state, structures, chunk, templates);
            return;
        }
        WorldgenRandom random = seededRandom();
        int x = chunk.getPos().x, z = chunk.getPos().z;
        random.setSeed(Mth.getSeed(x, "should-structures".hashCode(), z) ^ this.worldInfo.getSeed());
        if (this.generator.shouldGenerateStructures(this.worldInfo, random(random), x, z)) {
            this.delegate.createStructures(access, state, structures, chunk, templates);
        }
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState,
                                                         StructureManager structures, ChunkAccess chunk) {
        if (this.generator == null) return this.delegate.fillFromNoise(blender, randomState, structures, chunk);
        int x = chunk.getPos().x, z = chunk.getPos().z;
        WorldgenRandom decision = seededRandom();
        decision.setSeed(Mth.getSeed(x, "should-noise".hashCode(), z) ^ this.worldInfo.getSeed());
        CompletableFuture<ChunkAccess> base = this.generator.shouldGenerateNoise(this.worldInfo, random(decision), x, z)
                ? this.delegate.fillFromNoise(blender, randomState, structures, chunk)
                : CompletableFuture.completedFuture(chunk);
        return base.thenApply(result -> {
            WorldgenRandom random = seededRandom();
            random.setSeed((long) x * 341873128712L + (long) z * 132897987541L);
            CraftChunkData data = data(result);
            try {
                this.generator.generateNoise(this.worldInfo, random(random), x, z, data);
                data.applyTo(result);
                return result;
            } finally {
                data.breakLink();
            }
        });
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState randomState, ChunkAccess chunk) {
        if (this.generator == null) { this.delegate.buildSurface(region, structures, randomState, chunk); return; }
        int x = chunk.getPos().x, z = chunk.getPos().z;
        WorldgenRandom decision = seededRandom();
        decision.setSeed(Mth.getSeed(x, "should-surface".hashCode(), z) ^ region.getSeed());
        if (this.generator.shouldGenerateSurface(this.worldInfo, random(decision), x, z)) {
            this.delegate.buildSurface(region, structures, randomState, chunk);
        }
        WorldgenRandom random = seededRandom();
        random.setSeed((long) x * 341873128712L + (long) z * 132897987541L);
        CraftChunkData data = data(chunk);
        try {
            this.generator.generateSurface(this.worldInfo, random(random), x, z, data);
            this.generator.generateBedrock(this.worldInfo, random(random), x, z, data);
            data.applyTo(chunk);
        } finally {
            data.breakLink();
        }
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomes,
                             StructureManager structures, ChunkAccess chunk, GenerationStep.Carving step) {
        if (this.generator == null) { this.delegate.applyCarvers(region, seed, randomState, biomes, structures, chunk, step); return; }
        int x = chunk.getPos().x, z = chunk.getPos().z;
        WorldgenRandom decision = seededRandom();
        decision.setSeed(Mth.getSeed(x, "should-caves".hashCode(), z) ^ region.getSeed());
        if (this.generator.shouldGenerateCaves(this.worldInfo, random(decision), x, z)) {
            this.delegate.applyCarvers(region, seed, randomState, biomes, structures, chunk, step);
        }
        WorldgenRandom random = seededRandom();
        random.setDecorationSeed(seed, x, z);
        CraftChunkData data = data(chunk);
        try {
            this.generator.generateCaves(this.worldInfo, random(random), x, z, data);
            data.applyTo(chunk);
        } finally {
            data.breakLink();
        }
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structures) {
        if (this.generator == null) {
            super.applyBiomeDecoration(level, chunk, structures);
            io.lunararcdevs.lunararc.common.mod.util.LunarArcChunkPopulators.populate(level, chunk);
            return;
        }
        int x = chunk.getPos().x, z = chunk.getPos().z;
        WorldgenRandom decision = seededRandom();
        decision.setSeed(Mth.getSeed(x, "should-decoration".hashCode(), z) ^ level.getSeed());
        if (this.generator.shouldGenerateDecorations(this.worldInfo, random(decision), x, z)) {
            super.applyBiomeDecoration(level, chunk, structures);
        }
        io.lunararcdevs.lunararc.common.mod.util.LunarArcChunkPopulators.populate(level, chunk);
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
        if (this.generator == null) { this.delegate.spawnOriginalMobs(region); return; }
        int x = region.getCenter().x, z = region.getCenter().z;
        WorldgenRandom decision = seededRandom();
        decision.setSeed(Mth.getSeed(x, "should-mobs".hashCode(), z) ^ region.getSeed());
        if (this.generator.shouldGenerateMobs(this.worldInfo, random(decision), x, z)) {
            this.delegate.spawnOriginalMobs(region);
        }
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState randomState) {
        if (this.generator != null && this.implementBaseHeight) {
            try {
                WorldgenRandom random = seededRandom();
                random.setSeed((long) (x >> 4) * 341873128712L + (long) (z >> 4) * 132897987541L);
                return this.generator.getBaseHeight(this.worldInfo, random(random), x, z, CraftHeightMap.fromNMS(type));
            } catch (UnsupportedOperationException ignored) {
                this.implementBaseHeight = false;
            }
        }
        return this.delegate.getBaseHeight(x, z, type, level, randomState);
    }

    @Override public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState state) {
        return this.delegate.getBaseColumn(x, z, level, state);
    }
    @Override public WeightedRandomList<MobSpawnSettings.SpawnerData> getMobsAt(Holder<net.minecraft.world.level.biome.Biome> biome,
            StructureManager structures, MobCategory category, BlockPos pos) {
        return this.delegate.getMobsAt(biome, structures, category, pos);
    }
    @Override public void addDebugScreenInfo(List<String> lines, RandomState state, BlockPos pos) {
        this.delegate.addDebugScreenInfo(lines, state, pos);
    }
    @Override protected MapCodec<? extends ChunkGenerator> codec() {
        throw new UnsupportedOperationException("Custom Bukkit ChunkGenerator is runtime-only and cannot be serialized");
    }
}
