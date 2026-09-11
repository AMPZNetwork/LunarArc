package org.bukkit.craftbukkit.generator;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import org.bukkit.World;
import org.bukkit.craftbukkit.block.CraftBiome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/** Immutable world information usable before the ServerLevel is constructed. */
public final class CraftWorldInfo implements WorldInfo {
    private final String name;
    private final UUID uid;
    private final World.Environment environment;
    private final long seed;
    private final int minHeight;
    private final int maxHeight;
    private final ChunkGenerator vanillaGenerator;
    private final RegistryAccess.Frozen registryAccess;
    private final net.minecraft.world.flag.FeatureFlagSet enabledFeatures;

    public CraftWorldInfo(String name, UUID uid, World.Environment environment, long seed,
                          int minHeight, int maxHeight, ChunkGenerator vanillaGenerator,
                          RegistryAccess.Frozen registryAccess, net.minecraft.world.flag.FeatureFlagSet enabledFeatures) {
        this.name = name;
        this.uid = uid;
        this.environment = environment;
        this.seed = seed;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        this.vanillaGenerator = vanillaGenerator;
        this.registryAccess = registryAccess;
        this.enabledFeatures = java.util.Objects.requireNonNull(enabledFeatures, "enabledFeatures");
    }

    @Override public @NotNull String getName() { return this.name; }
    @Override public @NotNull UUID getUID() { return this.uid; }
    @Override public World.@NotNull Environment getEnvironment() { return this.environment; }
    @Override public long getSeed() { return this.seed; }
    @Override public int getMinHeight() { return this.minHeight; }
    @Override public int getMaxHeight() { return this.maxHeight; }
    @Override public @NotNull java.util.Set<org.bukkit.FeatureFlag> getFeatureFlags() { return io.papermc.paper.world.flag.PaperFeatureFlagProviderImpl.fromNms(this.enabledFeatures); }

    @Override
    public @NotNull BiomeProvider vanillaBiomeProvider() {
        final RandomState randomState;
        if (this.vanillaGenerator instanceof NoiseBasedChunkGenerator noise) {
            randomState = RandomState.create(noise.generatorSettings().value(),
                    this.registryAccess.lookupOrThrow(Registries.NOISE), this.seed);
        } else {
            randomState = RandomState.create(NoiseGeneratorSettings.dummy(),
                    this.registryAccess.lookupOrThrow(Registries.NOISE), this.seed);
        }
        final Climate.Sampler sampler = randomState.sampler();
        final List<org.bukkit.block.Biome> possible = this.vanillaGenerator.getBiomeSource().possibleBiomes().stream()
                .map(CraftBiome::minecraftHolderToBukkit).toList();
        return new BiomeProvider() {
            @Override
            public @NotNull org.bukkit.block.Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {
                return CraftBiome.minecraftHolderToBukkit(vanillaGenerator.getBiomeSource().getNoiseBiome(x >> 2, y >> 2, z >> 2, sampler));
            }
            @Override
            public @NotNull List<org.bukkit.block.Biome> getBiomes(@NotNull WorldInfo worldInfo) { return possible; }
        };
    }
}
