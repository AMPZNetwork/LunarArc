package org.bukkit.craftbukkit.v1_21_R1;

import net.minecraft.server.level.ServerLevel;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_21_R1.block.CraftBlock;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.papermc.paper.world.MoonPhase;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.Consumer;
import org.bukkit.block.BlockState;

public class CraftWorld implements World {
    private final ServerLevel world;

    public CraftWorld(ServerLevel world) {
        this.world = world;
    }

    public ServerLevel getHandle() {
        return world;
    }

    @Override
    public @NotNull String getName() {
        return world.dimension().location().toString();
    }

    @Override
    public @NotNull UUID getUID() {
        return UUID.nameUUIDFromBytes(getName().getBytes());
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return NamespacedKey.minecraft(getName().toLowerCase().replace("minecraft:", ""));
    }

    @Override
    public @NotNull Block getBlockAt(int x, int y, int z) {
        return CraftBlock.create(world, new net.minecraft.core.BlockPos(x, y, z));
    }

    @Override
    public @NotNull Block getBlockAt(@NotNull Location location) {
        return getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public int getHighestBlockYAt(int x, int z) {
        return 0;
    }

    @Override
    public int getHighestBlockYAt(int x, int z, @NotNull HeightMap heightMap) {
        return 0;
    }

    @Override
    public int getHighestBlockYAt(@NotNull Location location) {
        return 0;
    }

    @Override
    public int getHighestBlockYAt(@NotNull Location location, @NotNull HeightMap heightMap) {
        return 0;
    }

    @Override
    public @NotNull Block getHighestBlockAt(int x, int z) {
        return getBlockAt(x, getHighestBlockYAt(x, z), z);
    }

    @Override
    public @NotNull Block getHighestBlockAt(int x, int z, @NotNull HeightMap heightMap) {
        return getBlockAt(x, getHighestBlockYAt(x, z, heightMap), z);
    }

    @Override
    public @NotNull Block getHighestBlockAt(@NotNull Location location) {
        return getHighestBlockAt(location.getBlockX(), location.getBlockZ());
    }

    @Override
    public @NotNull Block getHighestBlockAt(@NotNull Location location, @NotNull HeightMap heightMap) {
        return getHighestBlockAt(location.getBlockX(), location.getBlockZ(), heightMap);
    }

    @Override
    public @NotNull Chunk getChunkAt(int x, int z) {
        return null;
    }

    @Override
    public @NotNull Chunk getChunkAt(int x, int z, boolean generate) {
        return null;
    }

    @Override
    public @NotNull Chunk getChunkAt(@NotNull Location location) {
        return null;
    }

    @Override
    public @NotNull Chunk getChunkAt(@NotNull Block block) {
        return null;
    }

    @Override
    public boolean isChunkLoaded(int x, int z) {
        return false;
    }

    @Override
    public boolean isChunkLoaded(@NotNull org.bukkit.Chunk chunk) {
        return false;
    }

    @Override
    public boolean refreshChunk(int x, int z) {
        return false;
    }

    @Override
    public void loadChunk(int x, int z) {
    }

    @Override
    public void loadChunk(@NotNull org.bukkit.Chunk chunk) {
    }

    @Override
    public boolean loadChunk(int x, int z, boolean generate) {
        return false;
    }

    @Override
    public boolean unloadChunk(int x, int z) {
        return false;
    }

    @Override
    public boolean hasStructureAt(@NotNull io.papermc.paper.math.Position position,
            @NotNull org.bukkit.generator.structure.Structure structure) {
        return false;
    }

    @Override
    public boolean unloadChunk(int x, int z, boolean save) {
        return false;
    }

    @Override
    public boolean unloadChunk(@NotNull org.bukkit.Chunk chunk) {
        return false;
    }

    @Override
    public boolean unloadChunkRequest(int x, int z) {
        return false;
    }

    @Override
    public boolean isChunkInUse(int x, int z) {
        return false;
    }

    @Override
    public boolean isChunkGenerated(int x, int z) {
        return false;
    }

    @Override
    public boolean regenerateChunk(int x, int z) {
        return false;
    }

    @Override
    public @NotNull org.bukkit.Chunk[] getLoadedChunks() {
        return new org.bukkit.Chunk[0];
    }

    @Override
    public @NotNull List<Entity> getEntities() {
        return Collections.emptyList();
    }

    @Override
    public boolean generateTree(Location location, TreeType type) {
        return false;
    }

    @Override
    public boolean generateTree(Location location, TreeType type, BlockChangeDelegate delegate) {
        return false;
    }

    @Override
    public boolean generateTree(Location location, Random random, TreeType type) {
        return false;
    }

    @Override
    public boolean generateTree(Location location, Random random, TreeType type,
            Predicate<? super BlockState> statePredicate) {
        return false;
    }

    @Override
    public boolean generateTree(Location location, Random random, TreeType type,
            Consumer<? super BlockState> stateConsumer) {
        return false;
    }

    @Override
    public void setType(int x, int y, int z, @NotNull Material type) {
    }

    @Override
    public void setType(@NotNull Location location, @NotNull Material type) {
    }

    @Override
    public void setBlockData(int x, int y, int z, @NotNull org.bukkit.block.data.BlockData blockData) {
    }

    @Override
    public void setBlockData(@NotNull Location location, @NotNull org.bukkit.block.data.BlockData blockData) {
    }

    @Override
    public @NotNull Material getType(int x, int y, int z) {
        return Material.AIR;
    }

    @Override
    public @NotNull Material getType(@NotNull Location location) {
        return Material.AIR;
    }

    @Override
    public @NotNull org.bukkit.block.data.BlockData getBlockData(int x, int y, int z) {
        return org.bukkit.Bukkit.createBlockData(getType(x, y, z));
    }

    @Override
    public @NotNull org.bukkit.block.data.BlockData getBlockData(@NotNull Location location) {
        return getBlockData(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public @NotNull io.papermc.paper.block.fluid.FluidData getFluidData(int x, int y, int z) {
        return (io.papermc.paper.block.fluid.FluidData) java.lang.reflect.Proxy.newProxyInstance(
            io.papermc.paper.block.fluid.FluidData.class.getClassLoader(),
            new Class<?>[] { io.papermc.paper.block.fluid.FluidData.class },
            (p, m, a) -> null
        );
    }

    @Override
    public @NotNull io.papermc.paper.block.fluid.FluidData getFluidData(@NotNull Location location) {
        return getFluidData(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public @NotNull BlockState getBlockState(int x, int y, int z) {
        return CraftBlock.create(world, new net.minecraft.core.BlockPos(x, y, z)).getState();
    }

    @Override
    public @NotNull BlockState getBlockState(@NotNull Location location) {
        return getBlockState(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public void setBiome(int x, int y, int z, @NotNull org.bukkit.block.Biome biome) {
    }

    @Override
    public @NotNull org.bukkit.block.Biome getBiome(int x, int y, int z) {
        return org.bukkit.block.Biome.PLAINS;
    }

    @Override
    public @NotNull org.bukkit.block.Biome getComputedBiome(int x, int y, int z) {
        return org.bukkit.block.Biome.PLAINS;
    }

    @Override
    public @NotNull org.bukkit.block.Biome getBiome(@NotNull Location location) {
        return org.bukkit.block.Biome.PLAINS;
    }

    @Override
    public void setBiome(@NotNull Location location, @NotNull org.bukkit.block.Biome biome) {
    }

    @Override
    public <T extends org.bukkit.entity.AbstractArrow> @NotNull T spawnArrow(@NotNull Location location,
            @NotNull Vector direction, float speed, float spread, @NotNull Class<T> clazz) {
        return null;
    }

    @Override
    public @NotNull org.bukkit.entity.Arrow spawnArrow(@NotNull Location location, @NotNull Vector direction,
            float speed, float spread) {
        return null;
    }

    @Override
    public @NotNull List<Player> getPlayers() {
        return Collections.emptyList();
    }

    @Override
    public int getPlayerCount() {
        return 0;
    }

    @Override
    public int getChunkCount() {
        return 0;
    }

    @Override
    public @NotNull Collection<org.bukkit.Chunk> getForceLoadedChunks() {
        return Collections.emptyList();
    }

    @Override
    public void setChunkForceLoaded(int x, int z, boolean forced) {
    }

    @Override
    public boolean isChunkForceLoaded(int x, int z) {
        return false;
    }

    @Override
    public @NotNull Collection<org.bukkit.entity.Player> getPlayersSeeingChunk(int x, int z) {
        return Collections.emptyList();
    }

    @Override
    public @NotNull Collection<org.bukkit.entity.Player> getPlayersSeeingChunk(@NotNull org.bukkit.Chunk chunk) {
        return Collections.emptyList();
    }

    @Override
    public int getEntityCount() {
        return 0;
    }

    @Override
    public int getTileEntityCount() {
        return 0;
    }

    @Override
    public int getTickableTileEntityCount() {
        return 0;
    }

    @Override
    public void setVoidDamageMinBuildHeightOffset(double offset) {
    }

    @Override
    public double getVoidDamageMinBuildHeightOffset() {
        return 0;
    }

    @Override
    public void setVoidDamageAmount(float amount) {
    }

    @Override
    public float getVoidDamageAmount() {
        return 0;
    }

    @Override
    public void setVoidDamageEnabled(boolean enabled) {
    }

    @Override
    public boolean isVoidDamageEnabled() {
        return true;
    }

    @Override
    public boolean hasCollisionsIn(@NotNull BoundingBox boundingBox) {
        return false;
    }

    @Override
    public boolean lineOfSightExists(@NotNull Location from, @NotNull Location to) {
        return true;
    }

    @Override
    public @NotNull ChunkSnapshot getEmptyChunkSnapshot(int x, int z, boolean includeBiome,
            boolean includeBiomeTempRain) {
        return null;
    }

    @Override
    public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count) {
    }

    @Override
    public void playSound(@NotNull Location location, @NotNull Sound sound, float volume, float pitch) {
        try {
            net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation
                    .parse(sound.name().toLowerCase());
            net.minecraft.sounds.SoundEvent se = net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(rl);
            if (se != null) {
                world.playSound(null, location.getX(), location.getY(), location.getZ(),
                        net.minecraft.core.Holder.direct(se),
                        net.minecraft.sounds.SoundSource.MASTER, volume, pitch);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public long getTime() {
        return world.getDayTime();
    }

    @Override
    public void setTime(long time) {
        world.setDayTime(time);
    }

    @Override
    public long getFullTime() {
        return world.getGameTime();
    }

    @Override
    public void setFullTime(long time) {
    }

    @Override
    public boolean hasStorm() {
        return world.isThundering();
    }

    @Override
    public void setStorm(boolean hasStorm) {
    }

    @Override
    public int getWeatherDuration() {
        return 0;
    }

    @Override
    public void setWeatherDuration(int duration) {
    }

    @Override
    public boolean isThundering() {
        return world.isThundering();
    }

    @Override
    public void setThundering(boolean thundering) {
    }

    @Override
    public @NotNull MoonPhase getMoonPhase() {
        return MoonPhase.FULL_MOON;
    }

    @Override
    public @NotNull Environment getEnvironment() {
        return Environment.NORMAL;
    }

    @Override
    public long getSeed() {
        return world.getSeed();
    }

    @Override
    public boolean getPVP() {
        return true;
    }

    @Override
    public void setPVP(boolean pvp) {
    }

    @Override
    public @NotNull Difficulty getDifficulty() {
        return Difficulty.NORMAL;
    }

    @Override
    public void setDifficulty(@NotNull Difficulty difficulty) {
    }

    @Override
    public @NotNull Location getSpawnLocation() {
        return new Location(this, 0, 0, 0);
    }

    @Override
    public @NotNull WorldBorder getWorldBorder() {
        return null;
    }

    @Override
    public @NotNull org.bukkit.entity.Item dropItem(@NotNull Location location, @NotNull ItemStack item) {
        return null;
    }

    @Override
    public @NotNull org.bukkit.entity.Item dropItemNaturally(@NotNull Location location, @NotNull ItemStack item) {
        return null;
    }

    @Override
    public @NotNull org.bukkit.entity.Item dropItemNaturally(@NotNull Location location, @NotNull ItemStack item,
            @Nullable java.util.function.Consumer<? super org.bukkit.entity.Item> function) {
        return null;
    }

    @Override
    public @NotNull org.bukkit.entity.Item dropItem(@NotNull Location location, @NotNull ItemStack item,
            @Nullable java.util.function.Consumer<? super org.bukkit.entity.Item> function) {
        return null;
    }

    @Override
    public void setSendViewDistance(int distance) {
    }

    @Override
    public int getSendViewDistance() {
        return 10;
    }

    @Override
    public void setSimulationDistance(int distance) {
    }

    @Override
    public int getSimulationDistance() {
        return 10;
    }

    @Override
    public void setViewDistance(int distance) {
    }

    @Override
    public int getViewDistance() {
        return 10;
    }

    @Override
    public @NotNull Set<FeatureFlag> getFeatureFlags() {
        return Collections.emptySet();
    }

    @Override
    public @Nullable org.bukkit.boss.DragonBattle getEnderDragonBattle() {
        return null;
    }

    @Override
    public @NotNull List<org.bukkit.Raid> getRaids() {
        return Collections.emptyList();
    }

    @Override
    public @Nullable org.bukkit.Raid getRaid(int id) {
        return null;
    }

    @Override
    public @Nullable org.bukkit.Raid locateNearestRaid(@NotNull Location location, int radius) {
        return null;
    }

    @Override
    public @Nullable org.bukkit.util.BiomeSearchResult locateNearestBiome(@NotNull Location origin, int radius,
            int horizontalInterval, int verticalInterval, @NotNull org.bukkit.block.Biome... biomes) {
        return null;
    }

    @Override
    public @Nullable org.bukkit.util.BiomeSearchResult locateNearestBiome(@NotNull Location origin, int radius,
            @NotNull org.bukkit.block.Biome... biomes) {
        return null;
    }

    @Override
    public @NotNull org.bukkit.World.Spigot spigot() {
        return new org.bukkit.World.Spigot();
    }

    @Override
    public void sendGameEvent(@Nullable org.bukkit.entity.Entity source, @NotNull org.bukkit.GameEvent event,
            @NotNull org.bukkit.util.Vector position) {
    }

    @Override
    public @NotNull java.util.Collection<org.bukkit.Material> getInfiniburn() {
        return java.util.Collections.emptyList();
    }

    @Override
    public boolean isFixedTime() {
        return false;
    }

    @Override
    public double getCoordinateScale() {
        return 1.0;
    }

    @Override
    public org.bukkit.util.StructureSearchResult locateNearestStructure(Location origin,
            org.bukkit.generator.structure.Structure structure, int radius, boolean findUnexplored) {
        return null;
    }

    @Override
    public Location locateNearestStructure(Location origin, org.bukkit.StructureType structureType, int radius,
            boolean findUnexplored) {
        return null;
    }

    @Override
    public org.bukkit.util.StructureSearchResult locateNearestStructure(Location origin,
            org.bukkit.generator.structure.StructureType structureType, int radius, boolean findUnexplored) {
        return null;
    }

    @Override
    public <T> void spawnParticle(@NotNull org.bukkit.Particle particle, double x, double y, double z, int count,
            double offsetX, double offsetY, double offsetZ, double extra, @Nullable T data, boolean force) {
    }

    @Override
    public <T> void spawnParticle(@NotNull org.bukkit.Particle particle, @NotNull Location location, int count,
            double offsetX, double offsetY, double offsetZ, double extra, @Nullable T data, boolean force) {
    }

    @Override
    public <T> void spawnParticle(@NotNull org.bukkit.Particle particle, @NotNull Location location, int count,
            double offsetX, double offsetY, double offsetZ, double extra, @Nullable T data) {
    }

    @Override
    public void spawnParticle(@NotNull org.bukkit.Particle particle, double x, double y, double z, int count,
            double offsetX, double offsetY, double offsetZ, double extra) {
    }

    @Override
    public void spawnParticle(@NotNull org.bukkit.Particle particle, @NotNull Location location, int count,
            double offsetX, double offsetY, double offsetZ, double extra) {
    }

    @Override
    public <T> void spawnParticle(@NotNull org.bukkit.Particle particle, double x, double y, double z, int count,
            double offsetX, double offsetY, double offsetZ, @Nullable T data) {
    }

    @Override
    public <T> void spawnParticle(@NotNull org.bukkit.Particle particle, @NotNull Location location, int count,
            double offsetX, double offsetY, double offsetZ, @Nullable T data) {
    }

    @Override
    public void spawnParticle(@NotNull org.bukkit.Particle particle, double x, double y, double z, int count,
            double offsetX, double offsetY, double offsetZ) {
    }

    @Override
    public void spawnParticle(@NotNull org.bukkit.Particle particle, @NotNull Location location, int count,
            double offsetX, double offsetY, double offsetZ) {
    }

    @Override
    public <T> void spawnParticle(@NotNull org.bukkit.Particle particle, double x, double y, double z, int count,
            @Nullable T data) {
    }

    @Override
    public <T> void spawnParticle(@NotNull org.bukkit.Particle particle, @NotNull Location location, int count,
            @Nullable T data) {
    }

    @Override
    public void spawnParticle(@NotNull org.bukkit.Particle particle, double x, double y, double z, int count) {
    }

    @Override
    public <T> void spawnParticle(@NotNull org.bukkit.Particle particle,
            @NotNull java.util.List<org.bukkit.entity.Player> receivers, @Nullable org.bukkit.entity.Player source,
            double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra,
            @Nullable T data, boolean force) {
    }

    @Override
    public @NotNull Collection<org.bukkit.generator.structure.GeneratedStructure> getStructures(int x, int z) {
        return Collections.emptyList();
    }

    @Override
    public @NotNull Collection<org.bukkit.generator.structure.GeneratedStructure> getStructures(int x, int z,
            @NotNull org.bukkit.generator.structure.Structure structure) {
        return Collections.emptyList();
    }

    @Override
    public void setMetadata(@NotNull String metadataKey, @NotNull org.bukkit.metadata.MetadataValue newMetadataValue) {
    }

    @Override
    public @NotNull List<org.bukkit.metadata.MetadataValue> getMetadata(@NotNull String metadataKey) {
        return Collections.emptyList();
    }

    @Override
    public boolean hasMetadata(@NotNull String metadataKey) {
        return false;
    }

    @Override
    public void removeMetadata(@NotNull String metadataKey, @NotNull org.bukkit.plugin.Plugin owningPlugin) {
    }

    private org.bukkit.persistence.PersistentDataContainer persistentDataContainer;

    @Override
    public @NotNull org.bukkit.persistence.PersistentDataContainer getPersistentDataContainer() {
        if (persistentDataContainer == null) {
            persistentDataContainer = new org.bukkit.craftbukkit.v1_21_R1.persistence.CraftPersistentDataContainer();
        }
        return persistentDataContainer;
    }

    @Override
    public void sendPluginMessage(@NotNull org.bukkit.plugin.Plugin source, @NotNull String channel, byte[] message) {
    }

    @Override
    public @NotNull java.util.Set<String> getListeningPluginChannels() {
        return Collections.emptySet();
    }

    @Override
    public <T> boolean setGameRule(@NotNull org.bukkit.GameRule<T> rule, @NotNull T newValue) {
        return false;
    }

    @Override
    public <T> @Nullable T getGameRuleDefault(@NotNull org.bukkit.GameRule<T> rule) {
        return null;
    }

    @Override
    public <T> @Nullable T getGameRuleValue(@NotNull org.bukkit.GameRule<T> rule) {
        return null;
    }

    @Override
    public boolean isGameRule(@NotNull String rule) {
        return false;
    }

    @Override
    public boolean setGameRuleValue(@NotNull String rule, @NotNull String value) {
        return false;
    }

    @Override
    public @Nullable String getGameRuleValue(@NotNull String rule) {
        return null;
    }

    @Override
    public @NotNull String[] getGameRules() {
        return new String[0];
    }

    @Override
    public void playSound(@NotNull org.bukkit.entity.Entity entity, @NotNull String sound,
            @NotNull org.bukkit.SoundCategory category, float volume, float pitch, long seed) {
    }

    @Override
    public void playSound(@NotNull org.bukkit.entity.Entity entity, @NotNull org.bukkit.Sound sound,
            @NotNull org.bukkit.SoundCategory category, float volume, float pitch, long seed) {
    }

    @Override
    public void playSound(@NotNull org.bukkit.entity.Entity entity, @NotNull String sound,
            @NotNull org.bukkit.SoundCategory category, float volume, float pitch) {
    }

    @Override
    public void playSound(@NotNull org.bukkit.entity.Entity entity, @NotNull org.bukkit.Sound sound,
            @NotNull org.bukkit.SoundCategory category, float volume, float pitch) {
    }

    @Override
    public void playSound(@NotNull org.bukkit.entity.Entity entity, @NotNull String sound, float volume, float pitch) {
    }

    @Override
    public void playSound(@NotNull org.bukkit.entity.Entity entity, @NotNull org.bukkit.Sound sound, float volume,
            float pitch) {
    }

    @Override
    public void playSound(@NotNull org.bukkit.Location loc, @NotNull String sound,
            @NotNull org.bukkit.SoundCategory category, float volume, float pitch, long seed) {
    }

    @Override
    public void playSound(@NotNull org.bukkit.Location loc, @NotNull org.bukkit.Sound sound,
            @NotNull org.bukkit.SoundCategory category, float volume, float pitch, long seed) {
    }

    @Override
    public void playSound(@NotNull org.bukkit.Location loc, @NotNull String sound,
            @NotNull org.bukkit.SoundCategory category, float volume, float pitch) {
    }

    @Override
    public void playSound(@NotNull org.bukkit.Location loc, @NotNull org.bukkit.Sound sound,
            @NotNull org.bukkit.SoundCategory category, float volume, float pitch) {
    }

    @Override
    public void playSound(@NotNull org.bukkit.Location loc, @NotNull String sound, float volume, float pitch) {
    }

    @Override
    public void playNote(@NotNull org.bukkit.Location loc, @NotNull org.bukkit.Instrument instrument,
            @NotNull org.bukkit.Note note) {
    }

    @Override
    public void setSpawnLimit(@NotNull org.bukkit.entity.SpawnCategory category, int limit) {
    }

    @Override
    public int getSpawnLimit(@NotNull org.bukkit.entity.SpawnCategory category) {
        return -1;
    }

    @Override
    public void setAmbientSpawnLimit(int limit) {
    }

    @Override
    public int getAmbientSpawnLimit() {
        return -1;
    }

    @Override
    public void setWaterAmbientSpawnLimit(int limit) {
    }

    @Override
    public int getWaterAmbientSpawnLimit() {
        return -1;
    }

    @Override
    public void setWaterUndergroundCreatureSpawnLimit(int limit) {
    }

    @Override
    public int getWaterUndergroundCreatureSpawnLimit() {
        return -1;
    }

    @Override
    public void setWaterAnimalSpawnLimit(int limit) {
    }

    @Override
    public int getWaterAnimalSpawnLimit() {
        return -1;
    }

    @Override
    public void setAnimalSpawnLimit(int limit) {
    }

    @Override
    public int getAnimalSpawnLimit() {
        return -1;
    }

    @Override
    public void setMonsterSpawnLimit(int limit) {
    }

    @Override
    public int getMonsterSpawnLimit() {
        return -1;
    }

    @Override
    public void setTicksPerSpawns(@NotNull org.bukkit.entity.SpawnCategory category, int ticks) {
    }

    @Override
    public long getTicksPerSpawns(@NotNull org.bukkit.entity.SpawnCategory category) {
        return -1L;
    }

    @Override
    public void setTicksPerAmbientSpawns(int ticks) {
    }

    @Override
    public long getTicksPerAmbientSpawns() {
        return -1L;
    }

    @Override
    public void setTicksPerWaterUndergroundCreatureSpawns(int ticks) {
    }

    @Override
    public long getTicksPerWaterUndergroundCreatureSpawns() {
        return -1L;
    }

    @Override
    public void setTicksPerWaterAmbientSpawns(int ticks) {
    }

    @Override
    public long getTicksPerWaterAmbientSpawns() {
        return -1L;
    }

    @Override
    public void setTicksPerWaterSpawns(int ticks) {
    }

    @Override
    public long getTicksPerWaterSpawns() {
        return -1L;
    }

    @Override
    public void setTicksPerMonsterSpawns(int ticks) {
    }

    @Override
    public long getTicksPerMonsterSpawns() {
        return -1L;
    }

    @Override
    public void setTicksPerAnimalSpawns(int ticks) {
    }

    @Override
    public long getTicksPerAnimalSpawns() {
        return -1L;
    }

    @Override
    public void setHardcore(boolean hardcore) {
    }

    @Override
    public boolean isHardcore() {
        return false;
    }

    @Override
    public boolean canGenerateStructures() {
        return false;
    }

    @Override
    public org.bukkit.WorldType getWorldType() {
        return org.bukkit.WorldType.NORMAL;
    }

    @Override
    public @NotNull java.io.File getWorldFolder() {
        return new java.io.File(".");
    }

    @Override
    public void setAutoSave(boolean value) {
    }

    @Override
    public boolean isAutoSave() {
        return false;
    }

    @Override
    public void setKeepSpawnInMemory(boolean value) {
    }

    @Override
    public boolean getKeepSpawnInMemory() {
        return false;
    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    @Override
    public boolean isUltraWarm() {
        return false;
    }

    @Override
    public boolean hasRaids() {
        return false;
    }

    @Override
    public boolean isRespawnAnchorWorks() {
        return false;
    }

    @Override
    public boolean isPiglinSafe() {
        return false;
    }

    @Override
    public boolean hasCeiling() {
        return false;
    }

    @Override
    public boolean hasSkyLight() {
        return true;
    }

    @Override
    public boolean isBedWorks() {
        return true;
    }

    @Override
    public boolean isNatural() {
        return true;
    }

    @Override
    public int getLogicalHeight() {
        return 320;
    }

    @Override
    public double getHumidity(int x, int y, int z) {
        return 0.5;
    }

    @Override
    public double getHumidity(int x, int z) {
        return 0.5;
    }

    @Override
    public double getTemperature(int x, int y, int z) {
        return 0.5;
    }

    @Override
    public double getTemperature(int x, int z) {
        return 0.5;
    }

    @Override
    public void setBiome(int x, int z, @NotNull org.bukkit.block.Biome biome) {
    }

    @Override
    public @NotNull org.bukkit.block.Biome getBiome(int x, int z) {
        return org.bukkit.block.Biome.PLAINS;
    }

    @Override
    public boolean getAllowMonsters() {
        return true;
    }

    @Override
    public boolean getAllowAnimals() {
        return true;
    }

    @Override
    public void setSpawnFlags(boolean allowMonsters, boolean allowAnimals) {
    }

    @Override
    public <T> void playEffect(@NotNull org.bukkit.Location location, @NotNull org.bukkit.Effect effect,
            @Nullable T data, int radius) {
    }

    @Override
    public <T> void playEffect(@NotNull org.bukkit.Location location, @NotNull org.bukkit.Effect effect,
            @Nullable T data) {
    }

    @Override
    public void playEffect(@NotNull org.bukkit.Location location, @NotNull org.bukkit.Effect effect, int id, int data) {
    }

    @Override
    public void playEffect(@NotNull org.bukkit.Location location, @NotNull org.bukkit.Effect effect, int data) {
    }

    @Override
    public @NotNull org.bukkit.entity.FallingBlock spawnFallingBlock(@NotNull org.bukkit.Location location,
            @NotNull org.bukkit.Material material, byte data) {
        return null;
    }

    @Override
    public @NotNull org.bukkit.entity.FallingBlock spawnFallingBlock(@NotNull org.bukkit.Location location,
            @NotNull org.bukkit.block.data.BlockData data) {
        return null;
    }

    @Override
    public @NotNull org.bukkit.entity.FallingBlock spawnFallingBlock(@NotNull org.bukkit.Location location,
            @NotNull org.bukkit.material.MaterialData data) {
        return null;
    }

    @Override
    public <T extends Entity> @NotNull T addEntity(@NotNull T entity) {
        return entity;
    }

    @Override
    public <T extends Entity> @NotNull T spawn(@NotNull Location location, @NotNull Class<T> clazz) {
        return null;
    }

    @Override
    public <T extends Entity> @NotNull T spawn(@NotNull Location location, @NotNull Class<T> clazz,
            boolean randomizeData, @Nullable java.util.function.Consumer<? super T> function) {
        return null;
    }

    @Override
    public <T extends Entity> @NotNull T spawn(@NotNull Location location, @NotNull Class<T> clazz,
            @Nullable java.util.function.Consumer<? super T> function,
            @NotNull org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason) {
        return null;
    }

    @Override
    public @NotNull Entity spawnEntity(@NotNull Location location, @NotNull EntityType type) {
        return null;
    }

    @Override
    public @NotNull Entity spawnEntity(@NotNull Location location, @NotNull EntityType type, boolean randomizeData) {
        return null;
    }

    @Override
    public <T extends Entity> @NotNull T createEntity(@NotNull Location location, @NotNull Class<T> clazz) {
        return null;
    }

    @Override
    public <T extends org.bukkit.entity.LivingEntity> @NotNull T spawn(@NotNull org.bukkit.Location location,
            @NotNull Class<T> clazz, @NotNull org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason,
            boolean randomizeData, @Nullable java.util.function.Consumer<? super T> function) {
        return null;
    }

    @Override
    public @NotNull java.util.List<org.bukkit.generator.BlockPopulator> getPopulators() {
        return new java.util.ArrayList<>();
    }

    @Override
    public void save() {
    }

    @Override
    public @Nullable org.bukkit.generator.BiomeProvider getBiomeProvider() {
        return null;
    }

    @Override
    public @Nullable org.bukkit.generator.BiomeProvider vanillaBiomeProvider() {
        return null;
    }

    @Override
    public @Nullable org.bukkit.generator.ChunkGenerator getGenerator() {
        return null;
    }

    @Override
    public int getMaxHeight() {
        return 320;
    }

    @Override
    public int getMinHeight() {
        return -64;
    }

    @Override
    public boolean createExplosion(@NotNull org.bukkit.Location loc, float yield, boolean isFlaming, boolean isSmoking,
            @Nullable org.bukkit.entity.Entity source) {
        return false;
    }

    @Override
    public boolean createExplosion(@NotNull org.bukkit.Location loc, float yield, boolean isFlaming,
            boolean isSmoking) {
        return false;
    }

    @Override
    public boolean createExplosion(@Nullable org.bukkit.entity.Entity source, @NotNull org.bukkit.Location loc,
            float yield, boolean isFlaming, boolean isSmoking, boolean breakBlocks) {
        return false;
    }

    @Override
    public boolean createExplosion(@NotNull org.bukkit.Location loc, float yield, boolean isFlaming) {
        return false;
    }

    @Override
    public boolean createExplosion(@NotNull org.bukkit.Location loc, float yield) {
        return false;
    }

    @Override
    public boolean createExplosion(double x, double y, double z, float yield, boolean isFlaming, boolean isSmoking,
            @Nullable org.bukkit.entity.Entity source) {
        return false;
    }

    @Override
    public boolean createExplosion(double x, double y, double z, float yield, boolean isFlaming, boolean isSmoking) {
        return false;
    }

    @Override
    public boolean createExplosion(double x, double y, double z, float yield, boolean isFlaming) {
        return false;
    }

    @Override
    public boolean createExplosion(double x, double y, double z, float yield) {
        return false;
    }

    @Override
    public int getClearWeatherDuration() {
        return 0;
    }

    @Override
    public void setClearWeatherDuration(int duration) {
    }

    @Override
    public boolean isClearWeather() {
        return false;
    }

    @Override
    public void setThunderDuration(int duration) {
    }

    @Override
    public int getThunderDuration() {
        return 0;
    }

    @Override
    public long getGameTime() {
        return 0;
    }

    @Override
    public boolean isDayTime() {
        return false;
    }

    @Override
    public boolean setSpawnLocation(int x, int y, int z) {
        return false;
    }

    @Override
    public boolean setSpawnLocation(int x, int y, int z, float angle) {
        return false;
    }

    @Override
    public boolean setSpawnLocation(@NotNull org.bukkit.Location location) {
        return false;
    }

    @Override
    public @Nullable org.bukkit.util.RayTraceResult rayTrace(@NotNull io.papermc.paper.math.Position start,
            @NotNull org.bukkit.util.Vector direction, double maxDistance,
            @NotNull org.bukkit.FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks, double raySize,
            @Nullable java.util.function.Predicate<? super org.bukkit.entity.Entity> filter,
            @Nullable java.util.function.Predicate<? super org.bukkit.block.Block> blockFilter) {
        return null;
    }

    @Override
    public @Nullable org.bukkit.util.RayTraceResult rayTrace(@NotNull org.bukkit.Location start,
            @NotNull org.bukkit.util.Vector direction, double maxDistance,
            @NotNull org.bukkit.FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks, double raySize,
            @Nullable java.util.function.Predicate<? super org.bukkit.entity.Entity> filter) {
        return null;
    }

    @Override
    public @Nullable org.bukkit.util.RayTraceResult rayTraceBlocks(@NotNull io.papermc.paper.math.Position start,
            @NotNull org.bukkit.util.Vector direction, double maxDistance,
            @NotNull org.bukkit.FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks,
            @Nullable java.util.function.Predicate<? super org.bukkit.block.Block> filter) {
        return null;
    }

    @Override
    public @Nullable org.bukkit.util.RayTraceResult rayTraceBlocks(@NotNull org.bukkit.Location start,
            @NotNull org.bukkit.util.Vector direction, double maxDistance,
            @NotNull org.bukkit.FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks) {
        return null;
    }

    @Override
    public @Nullable org.bukkit.util.RayTraceResult rayTraceBlocks(@NotNull org.bukkit.Location start,
            @NotNull org.bukkit.util.Vector direction, double maxDistance,
            @NotNull org.bukkit.FluidCollisionMode fluidCollisionMode) {
        return null;
    }

    @Override
    public @Nullable org.bukkit.util.RayTraceResult rayTraceBlocks(@NotNull org.bukkit.Location start,
            @NotNull org.bukkit.util.Vector direction, double maxDistance) {
        return null;
    }

    @Override
    public @Nullable org.bukkit.util.RayTraceResult rayTraceEntities(@NotNull io.papermc.paper.math.Position start,
            @NotNull org.bukkit.util.Vector direction, double maxDistance, double raySize,
            @Nullable java.util.function.Predicate<? super org.bukkit.entity.Entity> filter) {
        return null;
    }

    @Override
    public @Nullable org.bukkit.util.RayTraceResult rayTraceEntities(@NotNull org.bukkit.Location start,
            @NotNull org.bukkit.util.Vector direction, double maxDistance, double raySize,
            @Nullable java.util.function.Predicate<? super org.bukkit.entity.Entity> filter) {
        return null;
    }

    @Override
    public @Nullable org.bukkit.util.RayTraceResult rayTraceEntities(@NotNull org.bukkit.Location start,
            @NotNull org.bukkit.util.Vector direction, double maxDistance,
            @Nullable java.util.function.Predicate<? super org.bukkit.entity.Entity> filter) {
        return null;
    }

    @Override
    public @Nullable org.bukkit.util.RayTraceResult rayTraceEntities(@NotNull org.bukkit.Location start,
            @NotNull org.bukkit.util.Vector direction, double maxDistance) {
        return null;
    }

    @Override
    public @Nullable org.bukkit.util.RayTraceResult rayTraceEntities(@NotNull org.bukkit.Location start,
            @NotNull org.bukkit.util.Vector direction, double maxDistance, double raySize) {
        return null;
    }

    @Override
    public @NotNull java.util.Collection<org.bukkit.entity.Entity> getNearbyEntities(
            @NotNull org.bukkit.util.BoundingBox boundingBox,
            @Nullable java.util.function.Predicate<? super org.bukkit.entity.Entity> filter) {
        return java.util.Collections.emptyList();
    }

    @Override
    public @NotNull java.util.Collection<org.bukkit.entity.Entity> getNearbyEntities(
            @NotNull org.bukkit.util.BoundingBox boundingBox) {
        return java.util.Collections.emptyList();
    }

    @Override
    public @NotNull java.util.Collection<org.bukkit.entity.Entity> getNearbyEntities(
            @NotNull org.bukkit.Location location, double x, double y, double z,
            @Nullable java.util.function.Predicate<? super org.bukkit.entity.Entity> filter) {
        return java.util.Collections.emptyList();
    }

    @Override
    public @NotNull java.util.Collection<org.bukkit.entity.Entity> getNearbyEntities(
            @NotNull org.bukkit.Location location, double x, double y, double z) {
        return java.util.Collections.emptyList();
    }

    @Override
    public @Nullable org.bukkit.entity.Entity getEntity(@NotNull java.util.UUID uuid) {
        return null;
    }

    @Override
    public @NotNull java.util.concurrent.CompletableFuture<org.bukkit.Chunk> getChunkAtAsync(int x, int z, boolean gen,
            boolean urgent) {
        return java.util.concurrent.CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull java.util.concurrent.CompletableFuture<org.bukkit.Chunk> getChunkAtAsync(int x, int z,
            boolean gen) {
        return java.util.concurrent.CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull java.util.concurrent.CompletableFuture<org.bukkit.Chunk> getChunkAtAsync(int x, int z) {
        return java.util.concurrent.CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull java.util.concurrent.CompletableFuture<org.bukkit.Chunk> getChunkAtAsync(
            @NotNull org.bukkit.Location location, boolean gen) {
        return java.util.concurrent.CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull java.util.concurrent.CompletableFuture<org.bukkit.Chunk> getChunkAtAsync(
            @NotNull org.bukkit.Location location) {
        return java.util.concurrent.CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull java.util.Collection<org.bukkit.entity.Entity> getEntitiesByClasses(@NotNull Class<?>... classes) {
        return java.util.Collections.emptyList();
    }

    @Override
    public @NotNull <T extends org.bukkit.entity.Entity> java.util.Collection<T> getEntitiesByClass(
            @NotNull Class<T> cls) {
        return java.util.Collections.emptyList();
    }

    @Override
    public @NotNull <T extends org.bukkit.entity.Entity> java.util.Collection<T> getEntitiesByClass(
            @NotNull Class<T>... classes) {
        return java.util.Collections.emptyList();
    }

    @Override
    public @NotNull java.util.List<org.bukkit.entity.LivingEntity> getLivingEntities() {
        return java.util.Collections.emptyList();
    }

    @Override
    public @Nullable org.bukkit.Location findLightningTarget(@NotNull org.bukkit.Location location) {
        return null;
    }

    @Override
    public @Nullable org.bukkit.Location findLightningRod(@NotNull org.bukkit.Location location) {
        return null;
    }

    @Override
    public @NotNull org.bukkit.entity.LightningStrike strikeLightning(@NotNull org.bukkit.Location location) {
        return null;
    }

    @Override
    public @NotNull org.bukkit.entity.LightningStrike strikeLightningEffect(@NotNull org.bukkit.Location location) {
        return null;
    }

    // RegionAccessor & World missing methods
    @Override
    public @NotNull Collection<org.bukkit.Chunk> getIntersectingChunks(@NotNull org.bukkit.util.BoundingBox box) {
        return Collections.emptyList();
    }

    @Override
    public @NotNull Map<org.bukkit.plugin.Plugin, Collection<org.bukkit.Chunk>> getPluginChunkTickets() {
        return Collections.emptyMap();
    }

    @Override
    public @NotNull Collection<org.bukkit.plugin.Plugin> getPluginChunkTickets(int x, int z) {
        return Collections.emptyList();
    }

    @Override
    public boolean addPluginChunkTicket(int x, int z, @NotNull org.bukkit.plugin.Plugin plugin) {
        return false;
    }

    @Override
    public boolean removePluginChunkTicket(int x, int z, @NotNull org.bukkit.plugin.Plugin plugin) {
        return false;
    }

    @Override
    public void removePluginChunkTickets(@NotNull org.bukkit.plugin.Plugin plugin) {
    }
}
