package io.ampznetwork.lunararc.common.stubs.org.bukkit.craftbukkit.v1_21_R1;

import net.minecraft.server.level.ServerLevel;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.*;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.bukkit.util.RayTraceResult;
import org.bukkit.FluidCollisionMode;
import io.papermc.paper.math.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CraftWorld implements World {
    private final ServerLevel world;

    public CraftWorld(ServerLevel world) {
        this.world = world;
    }

    public ServerLevel getHandle() {
        return world;
    }

    @Override public @NotNull String getName() { return world.dimension().location().toString(); }
    @Override public @NotNull UUID getUID() { return UUID.nameUUIDFromBytes(getName().getBytes()); }
    @Override public @NotNull Environment getEnvironment() { return Environment.NORMAL; }
    @Override public long getSeed() { return world.getSeed(); }
    @Override public long getTime() { return world.getDayTime(); }
    @Override public void setTime(long time) { world.setDayTime(time); }
    @Override public long getFullTime() { return world.getGameTime(); }
    @Override public void setFullTime(long time) { world.setDayTime(time); }
    @Override public boolean isThundering() { return world.isThundering(); }
    @Override public void setThundering(boolean thundering) { world.setWeatherParameters(0, 0, thundering, false); }
    @Override public int getThunderDuration() { return 0; }
    @Override public void setThunderDuration(int duration) {}
    @Override public boolean hasStorm() { return world.isRaining(); }
    @Override public void setStorm(boolean hasStorm) { world.setWeatherParameters(0, 0, false, hasStorm); }
    @Override public int getWeatherDuration() { return 0; }
    @Override public void setWeatherDuration(int duration) {}
    @Override public boolean isClearWeather() { return !world.isRaining(); }
    @Override public void setClearWeatherDuration(int duration) {}
    @Override public int getClearWeatherDuration() { return 0; }
    @Override public boolean isDayTime() { return world.isDay(); }
    @Override public long getGameTime() { return world.getGameTime(); }
    
    @Override public @NotNull Block getBlockAt(int x, int y, int z) { return null; }
    @Override public @NotNull Block getBlockAt(@NotNull Location location) { return null; }
    @Override public int getHighestBlockYAt(int x, int z) { return 64; }
    @Override public int getHighestBlockYAt(@NotNull Location location) { return 64; }
    @Override public @NotNull Block getHighestBlockAt(int x, int z) { return null; }
    @Override public @NotNull Block getHighestBlockAt(@NotNull Location location) { return null; }
    @Override public int getHighestBlockYAt(int x, int z, @NotNull HeightMap heightMap) { return 64; }
    @Override public int getHighestBlockYAt(@NotNull Location location, @NotNull HeightMap heightMap) { return 64; }
    @Override public @NotNull Block getHighestBlockAt(int x, int z, @NotNull HeightMap heightMap) { return null; }
    @Override public @NotNull Block getHighestBlockAt(@NotNull Location location, @NotNull HeightMap heightMap) { return null; }
    @Override public @NotNull Location getSpawnLocation() { return new Location(this, 0, 64, 0); }
    @Override public boolean setSpawnLocation(@NotNull Location location) { return false; }
    @Override public boolean setSpawnLocation(int x, int y, int z, float angle) { return false; }
    @Override public boolean setSpawnLocation(int x, int y, int z) { return false; }
    @Override public @NotNull Chunk getChunkAt(int x, int z) { return null; }
    @Override public @NotNull Chunk getChunkAt(@NotNull Block block) { return null; }
    @Override public @NotNull Chunk getChunkAt(@NotNull Location location) { return null; }
    @Override public @NotNull Chunk[] getLoadedChunks() { return new Chunk[0]; }
    @Override public void loadChunk(@NotNull Chunk chunk) {}
    @Override public boolean isChunkLoaded(@NotNull Chunk chunk) { return false; }
    @Override public boolean isChunkLoaded(int x, int z) { return false; }
    @Override public void loadChunk(int x, int z) {}
    @Override public @NotNull Chunk getChunkAt(int x, int z, boolean generate) { return null; }
    @Override public boolean loadChunk(int x, int z, boolean generate) { return false; }
    @Override public boolean unloadChunk(@NotNull Chunk chunk) { return false; }
    @Override public boolean unloadChunk(int x, int z) { return false; }
    @Override public boolean unloadChunk(int x, int z, boolean save) { return false; }
    @Override public boolean unloadChunkRequest(int x, int z) { return false; }
    @Override public boolean regenerateChunk(int x, int z) { return false; }
    @Override public @NotNull Collection<Player> getPlayersSeeingChunk(@NotNull Chunk chunk) { return Collections.emptyList(); }
    @Override public @NotNull Collection<Player> getPlayersSeeingChunk(int x, int z) { return Collections.emptyList(); }
    @Override public boolean isChunkForceLoaded(int x, int z) { return false; }
    @Override public void setChunkForceLoaded(int x, int z, boolean forced) {}
    @Override public @NotNull Collection<Chunk> getForceLoadedChunks() { return Collections.emptyList(); }
    @Override public boolean addPluginChunkTicket(int x, int z, @NotNull org.bukkit.plugin.Plugin plugin) { return false; }
    @Override public boolean removePluginChunkTicket(int x, int z, @NotNull org.bukkit.plugin.Plugin plugin) { return false; }
    @Override public void removePluginChunkTickets(@NotNull org.bukkit.plugin.Plugin plugin) {}
    @Override public @NotNull Collection<org.bukkit.plugin.Plugin> getPluginChunkTickets(int x, int z) { return Collections.emptyList(); }
    @Override public @NotNull Map<org.bukkit.plugin.Plugin, Collection<Chunk>> getPluginChunkTickets() { return Collections.emptyMap(); }
    @Override public @NotNull Collection<Chunk> getIntersectingChunks(@NotNull BoundingBox boundingBox) { return Collections.emptyList(); }
    @Override public @NotNull CompletableFuture<Chunk> getChunkAtAsync(int x, int z, boolean generate, boolean urgent) { return CompletableFuture.completedFuture(null); }
    @Override public int getTileEntityCount() { return 0; }
    @Override public int getTickableTileEntityCount() { return 0; }
    @Override public boolean isChunkGenerated(int x, int z) { return true; }
    @Override public boolean isChunkInUse(int x, int z) { return false; }
    @Override public boolean refreshChunk(int x, int z) { return false; }

    @Override public @NotNull List<Player> getPlayers() { return Collections.emptyList(); }
    @Override public @NotNull List<Entity> getEntities() { return Collections.emptyList(); }
    @Override public @NotNull List<LivingEntity> getLivingEntities() { return Collections.emptyList(); }
    @Override public @NotNull Collection<Entity> getNearbyEntities(@NotNull Location location, double x, double y, double z) { return Collections.emptyList(); }
    @Override public @NotNull Collection<Entity> getNearbyEntities(@NotNull Location location, double x, double y, double z, @Nullable Predicate<? super Entity> filter) { return Collections.emptyList(); }
    @Override public @NotNull Collection<Entity> getNearbyEntities(@NotNull BoundingBox boundingBox) { return Collections.emptyList(); }
    @Override public @NotNull Collection<Entity> getNearbyEntities(@NotNull BoundingBox boundingBox, @Nullable Predicate<? super Entity> filter) { return Collections.emptyList(); }

    @Override public @NotNull String[] getGameRules() { return new String[0]; }
    @Override public @Nullable String getGameRuleValue(@Nullable String rule) { return null; }
    @Override public boolean setGameRuleValue(@NotNull String rule, @NotNull String value) { return false; }
    @Override public boolean isGameRule(@NotNull String rule) { return false; }
    @Override public @Nullable <T> T getGameRuleValue(@NotNull GameRule<T> rule) { return null; }
    @Override public @Nullable <T> T getGameRuleDefault(@NotNull GameRule<T> rule) { return null; }
    @Override public <T> boolean setGameRule(@NotNull GameRule<T> rule, @NotNull T newValue) { return false; }

    @Override public @NotNull WorldBorder getWorldBorder() { return null; }
    @Override public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count) {}
    @Override public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, @Nullable T data) {}
    @Override public void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count) {}
    @Override public <T> void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, @Nullable T data) {}
    @Override public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX, double offsetY, double offsetZ) {}
    @Override public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX, double offsetY, double offsetZ, @Nullable T data) {}
    @Override public void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ) {}
    @Override public <T> void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, @Nullable T data) {}
    @Override public void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX, double offsetY, double offsetZ, double extra) {}
    @Override public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, @Nullable T data) {}
    @Override public void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra) {}
    @Override public <T> void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, @Nullable T data) {}
    @Override public <T> void spawnParticle(@NotNull Particle particle, @NotNull Location location, int count, double offsetX, double offsetY, double offsetZ, double extra, @Nullable T data, boolean force) {}
    @Override public <T> void spawnParticle(@NotNull Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, @Nullable T data, boolean force) {}
    @Override public <T> void spawnParticle(@NotNull Particle particle, @NotNull List<Player> players, @Nullable Player player, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, @Nullable T data, boolean force) {}

    @Override public @NotNull Spigot spigot() { return null; }
    @Override public void sendPluginMessage(@NotNull org.bukkit.plugin.Plugin source, @NotNull String channel, byte[] message) {}
    @Override public @NotNull Set<String> getListeningPluginChannels() { return Collections.emptySet(); }

    @Override public @NotNull Difficulty getDifficulty() { return Difficulty.NORMAL; }
    @Override public void setDifficulty(@NotNull Difficulty difficulty) {}
    @Override public boolean isVoidDamageEnabled() { return true; }
    @Override public void setVoidDamageEnabled(boolean enabled) {}
    @Override public float getVoidDamageAmount() { return 4.0f; }
    @Override public void setVoidDamageAmount(float amount) {}
    @Override public double getVoidDamageMinBuildHeightOffset() { return -64.0; }
    @Override public void setVoidDamageMinBuildHeightOffset(double offset) {}
    @Override public int getMinHeight() { return -64; }
    @Override public int getMaxHeight() { return 320; }
    @Override public boolean hasCollisionsIn(@NotNull BoundingBox boundingBox) { return false; }
    @Override public boolean lineOfSightExists(@NotNull Location from, @NotNull Location to) { return true; }
    @Override public @NotNull org.bukkit.block.Biome getBiome(@NotNull Location location) { return org.bukkit.block.Biome.PLAINS; }
    @Override public @NotNull org.bukkit.block.Biome getBiome(int x, int y, int z) { return org.bukkit.block.Biome.PLAINS; }
    @Override public void setBiome(@NotNull Location location, @NotNull org.bukkit.block.Biome biome) {}
    @Override public void setBiome(int x, int y, int z, @NotNull org.bukkit.block.Biome biome) {}
    @Override public @NotNull BlockData getBlockData(@NotNull Location location) { return null; }
    @Override public @NotNull BlockData getBlockData(int x, int y, int z) { return null; }
    @Override public @NotNull Material getType(@NotNull Location location) { return Material.AIR; }
    @Override public @NotNull Material getType(int x, int y, int z) { return Material.AIR; }
    @Override public void setBlockData(@NotNull Location location, @NotNull BlockData data) {}
    @Override public void setBlockData(int x, int y, int z, @NotNull BlockData data) {}
    @Override public void setType(@NotNull Location location, @NotNull Material type) {}
    @Override public void setType(int x, int y, int z, @NotNull Material type) {}
    @Override public @NotNull BlockState getBlockState(int x, int y, int z) { return null; }
    @Override public @NotNull BlockState getBlockState(@NotNull Location location) { return null; }
    @Override public int getLogicalHeight() { return 256; }
    @Override public @Nullable org.bukkit.block.Biome getBiome(int x, int z) { return null; }
    @Override public void setBiome(int x, int z, @NotNull org.bukkit.block.Biome biome) {}
    @Override public @Nullable org.bukkit.generator.BiomeProvider vanillaBiomeProvider() { return null; }
    @Override public double getTemperature(int x, int z) { return 0.5; }
    @Override public double getTemperature(int x, int y, int z) { return 0.5; }
    @Override public double getHumidity(int x, int z) { return 0.5; }
    @Override public double getHumidity(int x, int y, int z) { return 0.5; }
    @Override public boolean isNatural() { return true; }
    @Override public boolean getPVP() { return true; }
    @Override public void setPVP(boolean pvp) {}
    @Override public boolean isBedWorks() { return true; }
    @Override public boolean hasSkyLight() { return true; }
    @Override public boolean hasCeiling() { return false; }
    @Override public boolean isPiglinSafe() { return false; }
    @Override public boolean isRespawnAnchorWorks() { return false; }
    @Override public boolean hasRaids() { return true; }
    @Override public boolean isUltraWarm() { return false; }
    @Override public int getSeaLevel() { return 63; }
    @Override public boolean getKeepSpawnInMemory() { return true; }
    @Override public void setKeepSpawnInMemory(boolean value) {}
    @Override public int getViewDistance() { return 10; }
    @Override public int getSimulationDistance() { return 10; }
    @Override public @NotNull File getWorldFolder() { return new File("."); }
    @Override public @NotNull WorldType getWorldType() { return WorldType.NORMAL; }
    @Override public boolean canGenerateStructures() { return true; }
    @Override public boolean isHardcore() { return false; }
    @Override public void setHardcore(boolean hardcore) {}
    
    @Override public long getTicksPerAnimalSpawns() { return 400; }
    @Override public void setTicksPerAnimalSpawns(int ticks) {}
    @Override public long getTicksPerMonsterSpawns() { return 1; }
    @Override public void setTicksPerMonsterSpawns(int ticks) {}
    @Override public long getTicksPerWaterSpawns() { return 1; }
    @Override public void setTicksPerWaterSpawns(int ticks) {}
    @Override public long getTicksPerWaterAmbientSpawns() { return 1; }
    @Override public void setTicksPerWaterAmbientSpawns(int ticks) {}
    @Override public long getTicksPerWaterUndergroundCreatureSpawns() { return 1; }
    @Override public void setTicksPerWaterUndergroundCreatureSpawns(int ticks) {}
    @Override public long getTicksPerAmbientSpawns() { return 1; }
    @Override public void setTicksPerAmbientSpawns(int ticks) {}
    @Override public long getTicksPerSpawns(@NotNull SpawnCategory category) { return 1; }
    @Override public void setTicksPerSpawns(@NotNull SpawnCategory category, int ticks) {}

    @Override public int getMonsterSpawnLimit() { return 70; }
    @Override public void setMonsterSpawnLimit(int limit) {}
    @Override public int getAnimalSpawnLimit() { return 10; }
    @Override public void setAnimalSpawnLimit(int limit) {}
    @Override public int getWaterAnimalSpawnLimit() { return 5; }
    @Override public void setWaterAnimalSpawnLimit(int limit) {}
    @Override public int getWaterUndergroundCreatureSpawnLimit() { return 5; }
    @Override public void setWaterUndergroundCreatureSpawnLimit(int limit) {}
    @Override public int getWaterAmbientSpawnLimit() { return 20; }
    @Override public void setWaterAmbientSpawnLimit(int limit) {}
    @Override public int getAmbientSpawnLimit() { return 15; }
    @Override public void setAmbientSpawnLimit(int limit) {}
    @Override public int getSpawnLimit(@NotNull SpawnCategory category) { return 70; }
    @Override public void setSpawnLimit(@NotNull SpawnCategory category, int limit) {}

    @Override public void playNote(@NotNull Location location, @NotNull Instrument instrument, @NotNull Note note) {}
    @Override public void playSound(@NotNull Location location, @NotNull Sound sound, float volume, float pitch) {}
    @Override public void playSound(@NotNull Location location, @NotNull String sound, float volume, float pitch) {}
    @Override public void playSound(@NotNull Location location, @NotNull Sound sound, @NotNull SoundCategory category, float volume, float pitch) {}
    @Override public void playSound(@NotNull Location location, @NotNull String sound, @NotNull SoundCategory category, float volume, float pitch) {}
    @Override public void playSound(@NotNull Location location, @NotNull Sound sound, @NotNull SoundCategory category, float volume, float pitch, long seed) {}
    @Override public void playSound(@NotNull Location location, @NotNull String sound, @NotNull SoundCategory category, float volume, float pitch, long seed) {}
    @Override public void playSound(@NotNull Entity entity, @NotNull Sound sound, float volume, float pitch) {}
    @Override public void playSound(@NotNull Entity entity, @NotNull String sound, float volume, float pitch) {}
    @Override public void playSound(@NotNull Entity entity, @NotNull Sound sound, @NotNull SoundCategory category, float volume, float pitch) {}
    @Override public void playSound(@NotNull Entity entity, @NotNull String sound, @NotNull SoundCategory category, float volume, float pitch) {}
    @Override public void playSound(@NotNull Entity entity, @NotNull Sound sound, @NotNull SoundCategory category, float volume, float pitch, long seed) {}
    @Override public void playSound(@NotNull Entity entity, @NotNull String sound, @NotNull SoundCategory category, float volume, float pitch, long seed) {}

    @Override public @Nullable Location locateNearestStructure(@NotNull Location origin, @NotNull StructureType structureType, int radius, boolean findUnexplored) { return null; }
    @Override public @Nullable org.bukkit.util.StructureSearchResult locateNearestStructure(@NotNull Location origin, @NotNull org.bukkit.generator.structure.StructureType structureType, int radius, boolean findUnexplored) { return null; }
    @Override public @Nullable org.bukkit.util.StructureSearchResult locateNearestStructure(@NotNull Location origin, @NotNull org.bukkit.generator.structure.Structure structure, int radius, boolean findUnexplored) { return null; }

    @Override public double getCoordinateScale() { return 1.0; }
    @Override public boolean isFixedTime() { return false; }
    @Override public @NotNull Collection<Material> getInfiniburn() { return Collections.emptyList(); }
    @Override public @Nullable RayTraceResult rayTraceEntities(@NotNull Location start, @NotNull Vector direction, double maxDistance) { return null; }
    @Override public @Nullable RayTraceResult rayTraceEntities(@NotNull Location start, @NotNull Vector direction, double maxDistance, double raySize) { return null; }
    @Override public @Nullable RayTraceResult rayTraceEntities(@NotNull Location start, @NotNull Vector direction, double maxDistance, @Nullable Predicate<? super Entity> filter) { return null; }
    @Override public @Nullable RayTraceResult rayTraceEntities(@NotNull Location start, @NotNull Vector direction, double maxDistance, double raySize, @Nullable Predicate<? super Entity> filter) { return null; }
    @Override public @Nullable RayTraceResult rayTraceEntities(@NotNull Position start, @NotNull Vector direction, double maxDistance, double raySize, @Nullable Predicate<? super Entity> filter) { return null; }
    @Override public @Nullable RayTraceResult rayTraceBlocks(@NotNull Location start, @NotNull Vector direction, double maxDistance) { return null; }
    @Override public @Nullable RayTraceResult rayTraceBlocks(@NotNull Location start, @NotNull Vector direction, double maxDistance, @NotNull FluidCollisionMode fluidCollisionMode) { return null; }
    @Override public @Nullable RayTraceResult rayTraceBlocks(@NotNull Location start, @NotNull Vector direction, double maxDistance, @NotNull FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks) { return null; }
    @Override public @Nullable RayTraceResult rayTraceBlocks(@NotNull Position start, @NotNull Vector direction, double maxDistance, @NotNull FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks, @Nullable Predicate<? super Block> filter) { return null; }
    @Override public @Nullable RayTraceResult rayTrace(@NotNull Location start, @NotNull Vector direction, double maxDistance, @NotNull FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks, double raySize, @Nullable Predicate<? super Entity> filter) { return null; }
    @Override public @Nullable RayTraceResult rayTrace(@NotNull Position start, @NotNull Vector direction, double maxDistance, @NotNull FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks, double raySize, @Nullable Predicate<? super Entity> filter, @Nullable Predicate<? super Block> filterBlock) { return null; }
    @Override public @Nullable Entity getEntity(@NotNull UUID uuid) { return null; }
    @Override public void sendGameEvent(@Nullable Entity entity, @NotNull GameEvent gameEvent, @NotNull Vector position) {}
    
    @Override public @Nullable org.bukkit.util.BiomeSearchResult locateNearestBiome(@NotNull Location origin, int radius, @NotNull org.bukkit.block.Biome... biomes) { return null; }
    @Override public @Nullable org.bukkit.util.BiomeSearchResult locateNearestBiome(@NotNull Location origin, int radius, int horizontalRes, int verticalRes, @NotNull org.bukkit.block.Biome... biomes) { return null; }
    @Override public @Nullable Raid locateNearestRaid(@NotNull Location location, int radius) { return null; }
    @Override public @Nullable Raid getRaid(int id) { return null; }
    @Override public @NotNull List<Raid> getRaids() { return Collections.emptyList(); }
    @Override public @Nullable DragonBattle getEnderDragonBattle() { return null; }
    @Override public @NotNull Set<FeatureFlag> getFeatureFlags() { return Collections.emptySet(); }
    
    @Override public @NotNull NamespacedKey getKey() { return NamespacedKey.minecraft(getName()); }
    @Override public @NotNull io.papermc.paper.world.MoonPhase getMoonPhase() { return io.papermc.paper.world.MoonPhase.FULL_MOON; }
    @Override public @NotNull org.bukkit.block.Biome getComputedBiome(int x, int y, int z) { return org.bukkit.block.Biome.PLAINS; }
    @Override public @NotNull <T extends Entity> T addEntity(@NotNull T entity) { return entity; }
    @Override public @NotNull <T extends Entity> T createEntity(@NotNull Location location, @NotNull Class<T> clazz) { return null; }
    @Override public @NotNull io.papermc.paper.block.fluid.FluidData getFluidData(int x, int y, int z) { return null; }
    @Override public void setViewDistance(int distance) {}
    @Override public void setSimulationDistance(int distance) {}
    
    @Override public int getSendViewDistance() { return 10; }
    @Override public void setSendViewDistance(int distance) {}
    
    @Override public int getChunkCount() { return 0; }
    @Override public int getEntityCount() { return 0; }
    @Override public int getPlayerCount() { return 0; }
    @Override public @NotNull Chunk getChunkAt(long key) { return null; }
    @Override public @NotNull Chunk getChunkAt(long key, boolean generate) { return null; }
    @Override public boolean isChunkGenerated(long key) { return true; }
    @Override public boolean hasStructureAt(@NotNull Position position, @NotNull org.bukkit.generator.structure.Structure structure) { return false; }
    @Override public @NotNull Collection<org.bukkit.generator.structure.GeneratedStructure> getStructures(int x, int z) { return Collections.emptyList(); }
    @Override public @NotNull Collection<org.bukkit.generator.structure.GeneratedStructure> getStructures(int x, int z, @NotNull org.bukkit.generator.structure.Structure structure) { return Collections.emptyList(); }
    @Override public boolean getAllowAnimals() { return true; }
    @Override public boolean getAllowMonsters() { return true; }
    @Override public void setSpawnFlags(boolean allowMonsters, boolean allowAnimals) {}
    @Override public @NotNull FallingBlock spawnFallingBlock(@NotNull Location location, @NotNull org.bukkit.material.MaterialData data) throws IllegalArgumentException { return null; }
    @Override public @NotNull FallingBlock spawnFallingBlock(@NotNull Location location, @NotNull BlockData data) throws IllegalArgumentException { return null; }
    @Override public @NotNull FallingBlock spawnFallingBlock(@NotNull Location location, @NotNull Material material, byte data) throws IllegalArgumentException { return null; }
    @Override public @NotNull LightningStrike strikeLightning(@NotNull Location loc) { return null; }
    @Override public @NotNull LightningStrike strikeLightningEffect(@NotNull Location loc) { return null; }
    @Override public @Nullable Location findLightningRod(@NotNull Location location) { return null; }
    @Override public @Nullable Location findLightningTarget(@NotNull Location location) { return null; }
    @Override public boolean generateTree(@NotNull Location location, @NotNull TreeType type) { return false; }
    @Override public boolean generateTree(@NotNull Location location, @NotNull TreeType type, @NotNull BlockChangeDelegate delegate) { return false; }
    @Override public boolean generateTree(@NotNull Location location, @NotNull Random random, @NotNull TreeType type) { return false; }
    @Override public boolean generateTree(@NotNull Location location, @NotNull Random random, @NotNull TreeType type, @Nullable Consumer<? super BlockState> consumer) { return false; }
    @Override public boolean generateTree(@NotNull Location location, @NotNull Random random, @NotNull TreeType type, @Nullable Predicate<? super BlockState> filter) { return false; }
    @Override public @NotNull Arrow spawnArrow(@NotNull Location location, @NotNull Vector direction, float speed, float spread) { return null; }
    @Override public @NotNull <T extends AbstractArrow> T spawnArrow(@NotNull Location location, @NotNull Vector direction, float speed, float spread, @NotNull Class<T> clazz) { return null; }
    @Override public @NotNull Item dropItem(@NotNull Location location, @NotNull ItemStack item) { return null; }
    @Override public @NotNull Item dropItem(@NotNull Location location, @NotNull ItemStack item, @Nullable Consumer<? super Item> function) { return null; }
    @Override public @NotNull Item dropItemNaturally(@NotNull Location location, @NotNull ItemStack item) { return null; }
    @Override public @NotNull Item dropItemNaturally(@NotNull Location location, @NotNull ItemStack item, @Nullable Consumer<? super Item> function) { return null; }
    @Override public boolean createExplosion(double x, double y, double z, float power) { return false; }
    @Override public boolean createExplosion(double x, double y, double z, float power, boolean setFire) { return false; }
    @Override public boolean createExplosion(double x, double y, double z, float power, boolean setFire, boolean breakBlocks) { return false; }
    @Override public boolean createExplosion(double x, double y, double z, float power, boolean setFire, boolean breakBlocks, @Nullable Entity source) { return false; }
    @Override public boolean createExplosion(@NotNull Location loc, float power) { return false; }
    @Override public boolean createExplosion(@NotNull Location loc, float power, boolean setFire) { return false; }
    @Override public boolean createExplosion(@NotNull Location loc, float power, boolean setFire, boolean breakBlocks) { return false; }
    @Override public boolean createExplosion(@NotNull Location loc, float power, boolean setFire, boolean breakBlocks, @Nullable Entity source) { return false; }
    @Override public boolean createExplosion(@Nullable Entity source, @NotNull Location loc, float power, boolean setFire, boolean breakBlocks, boolean breakBlocksOverride) { return false; }
    @Override public void save() {}
    @Override public @NotNull List<org.bukkit.generator.BlockPopulator> getPopulators() { return Collections.emptyList(); }
    @Override public @Nullable org.bukkit.generator.ChunkGenerator getGenerator() { return null; }
    @Override public @Nullable org.bukkit.generator.BiomeProvider getBiomeProvider() { return null; }
    @Override public void playEffect(@NotNull Location location, @NotNull Effect effect, int data) {}
    @Override public void playEffect(@NotNull Location location, @NotNull Effect effect, int data, int radius) {}
    @Override public <T> void playEffect(@NotNull Location location, @NotNull Effect effect, @Nullable T data) {}
    @Override public <T> void playEffect(@NotNull Location location, @NotNull Effect effect, @Nullable T data, int radius) {}
    @Override public @NotNull ChunkSnapshot getEmptyChunkSnapshot(int x, int z, boolean includeBiome, boolean includeBiomeTempRain) { return null; }

    @Override public @NotNull <T extends Entity> T spawn(@NotNull Location location, @NotNull Class<T> clazz) throws IllegalArgumentException { return null; }
    @Override public @NotNull <T extends Entity> T spawn(@NotNull Location location, @NotNull Class<T> clazz, @Nullable Consumer<? super T> function) throws IllegalArgumentException { return null; }
    @Override public @NotNull <T extends Entity> T spawn(@NotNull Location location, @NotNull Class<T> clazz, @Nullable Consumer<? super T> function, @NotNull org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason) throws IllegalArgumentException { return null; }
    @Override public @NotNull <T extends LivingEntity> T spawn(@NotNull Location location, @NotNull Class<T> clazz, @NotNull org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason, boolean randomizeData, @Nullable Consumer<? super T> function) throws IllegalArgumentException { return null; }
    @Override public @NotNull <T extends Entity> T spawn(@NotNull Location location, @NotNull Class<T> clazz, boolean randomizeData, @Nullable Consumer<? super T> function) throws IllegalArgumentException { return null; }
    @Override public @NotNull Entity spawnEntity(@NotNull Location loc, @NotNull EntityType type) { return null; }
    @Override public @NotNull Entity spawnEntity(@NotNull Location loc, @NotNull EntityType type, boolean randomizeData) { return null; }
    
    @Override public @NotNull org.bukkit.persistence.PersistentDataContainer getPersistentDataContainer() { return null; }
    @Override public void setMetadata(@NotNull String metadataKey, @NotNull org.bukkit.metadata.MetadataValue newMetadataValue) {}
    @Override public @NotNull List<org.bukkit.metadata.MetadataValue> getMetadata(@NotNull String metadataKey) { return Collections.emptyList(); }
    @Override public boolean hasMetadata(@NotNull String metadataKey) { return false; }
    @Override public void removeMetadata(@NotNull String metadataKey, @NotNull org.bukkit.plugin.Plugin owningPlugin) {}
    
    @Override public void setAutoSave(boolean value) {}
    @Override public boolean isAutoSave() { return true; }
    @Override public @NotNull <T extends Entity> Collection<T> getEntitiesByClass(@NotNull Class<T> clazz) { return Collections.emptyList(); }
    @Override public @NotNull <T extends Entity> Collection<T> getEntitiesByClass(@NotNull Class<T>... classes) { return Collections.emptyList(); }
    @Override public @NotNull Collection<Entity> getEntitiesByClasses(@NotNull Class<?>... classes) { return Collections.emptyList(); }
}
