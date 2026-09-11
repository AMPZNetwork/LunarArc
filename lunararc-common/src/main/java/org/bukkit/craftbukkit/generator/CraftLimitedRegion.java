package org.bukkit.craftbukkit.generator;

import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.CraftBiome;
import org.bukkit.craftbukkit.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.block.fluid.CraftFluidData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class CraftLimitedRegion implements LimitedRegion {
    private final WeakReference<WorldGenLevel> weakAccess;
    private final int centerChunkX;
    private final int centerChunkZ;
    private final int buffer = 16;
    private final BoundingBox region;
    private boolean entitiesLoaded;
    private final List<net.minecraft.world.entity.Entity> entities = new ArrayList<>();
    private final List<net.minecraft.world.entity.Entity> outsideEntities = new ArrayList<>();

    public CraftLimitedRegion(WorldGenLevel access, ChunkPos center) {
        this.weakAccess = new WeakReference<>(Objects.requireNonNull(access, "access"));
        this.centerChunkX = center.x;
        this.centerChunkZ = center.z;
        int x = center.getMinBlockX(), z = center.getMinBlockZ();
        this.region = new BoundingBox(x - buffer, access.getMinBuildHeight(), z - buffer,
                x + 16 + buffer, access.getMaxBuildHeight(), z + 16 + buffer);
    }

    public WorldGenLevel getHandle() {
        WorldGenLevel access = this.weakAccess.get();
        if (access == null) throw new IllegalStateException("LimitedRegion is no longer valid outside its population call");
        return access;
    }

    private net.minecraft.server.level.ServerLevel serverLevel() {
        WorldGenLevel access = getHandle();
        if (access instanceof net.minecraft.server.level.ServerLevel direct) return direct;
        if (access instanceof net.minecraft.server.level.WorldGenRegion region) return region.getLevel();
        throw new IllegalStateException("WorldGenLevel is not backed by a ServerLevel: " + access.getClass().getName());
    }
    private void check(int x, int y, int z) {
        Preconditions.checkArgument(isInRegion(x, y, z), "Coordinates %s, %s, %s are not in the region", x, y, z);
    }

    public void breakLink() { this.weakAccess.clear(); }
    @Override public int getBuffer() { return this.buffer; }
    @Override public int getCenterChunkX() { return this.centerChunkX; }
    @Override public int getCenterChunkZ() { return this.centerChunkZ; }
    @Override public boolean isInRegion(@NotNull Location location) { return isInRegion(location.getX(), location.getY(), location.getZ()); }
    private boolean isInRegion(double x, double y, double z) { return this.region.contains(x, y, z); }
    @Override public boolean isInRegion(int x, int y, int z) { return this.region.contains(x, y, z); }
    @Override public @NotNull World getWorld() { return ((org.bukkit.craftbukkit.CraftServer) Bukkit.getServer()).getCraftWorld(serverLevel()); }
    @Override public @NotNull NamespacedKey getKey() { return getWorld().getKey(); }

    @Override public @NotNull Biome getBiome(@NotNull Location location) { return getBiome(location.getBlockX(), location.getBlockY(), location.getBlockZ()); }
    @Override public @NotNull Biome getBiome(int x, int y, int z) { check(x,y,z); return CraftBiome.minecraftHolderToBukkit(getHandle().getNoiseBiome(x >> 2, y >> 2, z >> 2)); }
    @Override public @NotNull Biome getComputedBiome(int x, int y, int z) { check(x,y,z); return CraftBiome.minecraftHolderToBukkit(getHandle().getBiome(new BlockPos(x,y,z))); }
    @Override public void setBiome(@NotNull Location location, @NotNull Biome biome) { setBiome(location.getBlockX(), location.getBlockY(), location.getBlockZ(), biome); }
    @Override public void setBiome(int x, int y, int z, @NotNull Biome biome) {
        check(x,y,z);
        ChunkAccess chunk = getHandle().getChunk(x >> 4, z >> 4, ChunkStatus.EMPTY);
        int sectionIndex = chunk.getSectionIndex(y);
        net.minecraft.world.level.chunk.PalettedContainerRO<net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>> biomes =
                chunk.getSection(sectionIndex).getBiomes();
        if (!(biomes instanceof net.minecraft.world.level.chunk.PalettedContainer<?> raw)) {
            throw new IllegalStateException("Biome palette is not mutable");
        }
        @SuppressWarnings("unchecked")
        net.minecraft.world.level.chunk.PalettedContainer<net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>> mutable =
                (net.minecraft.world.level.chunk.PalettedContainer<net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>>) raw;
        mutable.set((x >> 2) & 3, (y >> 2) & 3, (z >> 2) & 3, CraftBiome.bukkitToMinecraftHolder(biome));
        chunk.setUnsaved(true);
    }

    @Override public @NotNull BlockData getBlockData(@NotNull Location location) { return getBlockData(location.getBlockX(), location.getBlockY(), location.getBlockZ()); }
    @Override public @NotNull BlockData getBlockData(int x, int y, int z) { check(x,y,z); return CraftBlockData.fromData(getHandle().getBlockState(new BlockPos(x,y,z))); }
    @Override public @NotNull Material getType(@NotNull Location location) { return getType(location.getBlockX(), location.getBlockY(), location.getBlockZ()); }
    @Override public @NotNull Material getType(int x, int y, int z) {
        check(x,y,z); net.minecraft.resources.ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(getHandle().getBlockState(new BlockPos(x,y,z)).getBlock());
        Material material = id == null ? null : Material.matchMaterial(id.toString()); return material == null ? Material.AIR : material;
    }
    @Override public @NotNull io.papermc.paper.block.fluid.FluidData getFluidData(int x, int y, int z) { check(x,y,z); return new CraftFluidData(getHandle().getFluidState(new BlockPos(x,y,z))); }
    @Override public @NotNull BlockState getBlockState(@NotNull Location location) { return getBlockState(location.getBlockX(), location.getBlockY(), location.getBlockZ()); }
    @Override public @NotNull BlockState getBlockState(int x, int y, int z) {
        check(x,y,z); BlockPos pos = new BlockPos(x,y,z); net.minecraft.world.level.block.state.BlockState state = getHandle().getBlockState(pos);
        net.minecraft.world.level.block.entity.BlockEntity be = getHandle().getBlockEntity(pos);
        return be == null ? CraftBlockState.unplacedAt(pos, state) : new CraftBlockEntityState<>(serverLevel(), be, true);
    }
    @Override public void setBlockData(@NotNull Location location, @NotNull BlockData data) { setBlockData(location.getBlockX(), location.getBlockY(), location.getBlockZ(), data); }
    @Override public void setBlockData(int x, int y, int z, @NotNull BlockData data) {
        check(x,y,z); if (!(data instanceof CraftBlockData craft)) throw new IllegalArgumentException("BlockData must be CraftBlockData");
        getHandle().setBlock(new BlockPos(x,y,z), craft.getState(), net.minecraft.world.level.block.Block.UPDATE_ALL, 512);
    }
    @Override public void setType(@NotNull Location location, @NotNull Material material) { setType(location.getBlockX(), location.getBlockY(), location.getBlockZ(), material); }
    @Override public void setType(int x, int y, int z, @NotNull Material material) { setBlockData(x,y,z, material.createBlockData()); }

    @Override public int getHighestBlockYAt(int x, int z) { return getHighestBlockYAt(x,z,HeightMap.MOTION_BLOCKING); }
    @Override public int getHighestBlockYAt(@NotNull Location location) { return getHighestBlockYAt(location.getBlockX(), location.getBlockZ()); }
    @Override public int getHighestBlockYAt(int x, int z, @NotNull HeightMap map) {
        Preconditions.checkArgument(isInRegion(x, (int) region.getCenter().getY(), z), "Coordinates %s, %s are not in region", x,z);
        return getHandle().getHeight(org.bukkit.craftbukkit.CraftHeightMap.toNMS(map), x, z);
    }
    @Override public int getHighestBlockYAt(@NotNull Location location, @NotNull HeightMap map) { return getHighestBlockYAt(location.getBlockX(), location.getBlockZ(), map); }

    @Override public boolean generateTree(@NotNull Location location, @NotNull Random random, @NotNull TreeType type) { return generateTreeInternal(getHandle(), location, random, type); }
    @Override public boolean generateTree(@NotNull Location location, @NotNull Random random, @NotNull TreeType type, @Nullable Consumer<? super BlockState> consumer) {
        return generateTree(location, random, type, consumer == null ? (Predicate<? super BlockState>) null : state -> { consumer.accept(state); return true; });
    }
    @Override public boolean generateTree(@NotNull Location location, @NotNull Random random, @NotNull TreeType type, @Nullable Predicate<? super BlockState> predicate) {
        check(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        org.bukkit.craftbukkit.util.BlockStateListPopulator capture = new org.bukkit.craftbukkit.util.BlockStateListPopulator(getHandle());
        boolean result = generateTreeInternal(capture, location, random, type);
        for (var captured : capture.getCapturedStates()) {
            BlockState state = captured.state();
            if (predicate == null || predicate.test(state)) setBlockData(state.getX(), state.getY(), state.getZ(), state.getBlockData());
        }
        return result;
    }

    private boolean generateTreeInternal(WorldGenLevel level, Location location, Random random, TreeType type) {
        check(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.feature.ConfiguredFeature<?, ?>> feature = switch (type) {
            case BIG_TREE -> net.minecraft.data.worldgen.features.TreeFeatures.FANCY_OAK; case BIRCH -> net.minecraft.data.worldgen.features.TreeFeatures.BIRCH;
            case REDWOOD -> net.minecraft.data.worldgen.features.TreeFeatures.SPRUCE; case TALL_REDWOOD -> net.minecraft.data.worldgen.features.TreeFeatures.PINE;
            case JUNGLE -> net.minecraft.data.worldgen.features.TreeFeatures.MEGA_JUNGLE_TREE; case SMALL_JUNGLE -> net.minecraft.data.worldgen.features.TreeFeatures.JUNGLE_TREE_NO_VINE;
            case COCOA_TREE -> net.minecraft.data.worldgen.features.TreeFeatures.JUNGLE_TREE; case JUNGLE_BUSH -> net.minecraft.data.worldgen.features.TreeFeatures.JUNGLE_BUSH;
            case RED_MUSHROOM -> net.minecraft.data.worldgen.features.TreeFeatures.HUGE_RED_MUSHROOM; case BROWN_MUSHROOM -> net.minecraft.data.worldgen.features.TreeFeatures.HUGE_BROWN_MUSHROOM;
            case SWAMP -> net.minecraft.data.worldgen.features.TreeFeatures.SWAMP_OAK; case ACACIA -> net.minecraft.data.worldgen.features.TreeFeatures.ACACIA;
            case DARK_OAK -> net.minecraft.data.worldgen.features.TreeFeatures.DARK_OAK; case MEGA_REDWOOD -> net.minecraft.data.worldgen.features.TreeFeatures.MEGA_SPRUCE;
            case MEGA_PINE -> net.minecraft.data.worldgen.features.TreeFeatures.MEGA_PINE; case TALL_BIRCH -> net.minecraft.data.worldgen.features.TreeFeatures.SUPER_BIRCH_BEES_0002;
            case CRIMSON_FUNGUS -> net.minecraft.data.worldgen.features.TreeFeatures.CRIMSON_FUNGUS; case WARPED_FUNGUS -> net.minecraft.data.worldgen.features.TreeFeatures.WARPED_FUNGUS;
            case AZALEA -> net.minecraft.data.worldgen.features.TreeFeatures.AZALEA_TREE; case MANGROVE -> net.minecraft.data.worldgen.features.TreeFeatures.MANGROVE;
            case TALL_MANGROVE -> net.minecraft.data.worldgen.features.TreeFeatures.TALL_MANGROVE; case CHERRY -> net.minecraft.data.worldgen.features.TreeFeatures.CHERRY;
            case TREE -> net.minecraft.data.worldgen.features.TreeFeatures.OAK;
            case CHORUS_PLANT -> { net.minecraft.world.level.block.ChorusFlowerBlock.generatePlant(level, new BlockPos(location.getBlockX(),location.getBlockY(),location.getBlockZ()), new org.bukkit.craftbukkit.util.RandomSourceWrapper(random), 8); yield null; }
            default -> net.minecraft.data.worldgen.features.TreeFeatures.OAK;
        };
        if (feature == null) return true;
        var holder = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.CONFIGURED_FEATURE).getHolder(feature).orElse(null);
        return holder != null && holder.value().place(level, serverLevel().getChunkSource().getGenerator(), new org.bukkit.craftbukkit.util.RandomSourceWrapper(random), new BlockPos(location.getBlockX(),location.getBlockY(),location.getBlockZ()));
    }

    private void loadEntities() {
        if (entitiesLoaded) return; entitiesLoaded = true;
        for (int dx=-1; dx<=1; dx++) for (int dz=-1; dz<=1; dz++) {
            ChunkAccess access = getHandle().getChunk(centerChunkX+dx, centerChunkZ+dz, ChunkStatus.EMPTY);
            if (!(access instanceof ProtoChunk proto)) continue;
            for (CompoundTag tag : proto.getEntities()) {
                net.minecraft.world.entity.EntityType.loadEntityRecursive(tag, serverLevel(), entity -> {
                    if (region.contains(entity.getX(), entity.getY(), entity.getZ())) entities.add(entity); else outsideEntities.add(entity); return entity;
                });
            }
        }
    }
    public void saveEntities() {
        if (entitiesLoaded) for (int dx=-1; dx<=1; dx++) for (int dz=-1; dz<=1; dz++) {
            ChunkAccess access=getHandle().getChunk(centerChunkX+dx,centerChunkZ+dz,ChunkStatus.EMPTY); if (access instanceof ProtoChunk proto) proto.getEntities().clear();
        }
        for (var entity : entities) if (entity.isAlive()) getHandle().addFreshEntityWithPassengers(entity);
        for (var entity : outsideEntities) getHandle().addFreshEntityWithPassengers(entity);
    }
    private org.bukkit.entity.Entity bukkit(net.minecraft.world.entity.Entity entity) { return ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) entity).lunararc$getBukkitEntity(); }
    @Override public @NotNull List<Entity> getEntities() { loadEntities(); return entities.stream().map(this::bukkit).filter(Objects::nonNull).toList(); }
    @Override public @NotNull List<LivingEntity> getLivingEntities() { return getEntities().stream().filter(LivingEntity.class::isInstance).map(LivingEntity.class::cast).toList(); }
    @Override public <T extends Entity> @NotNull Collection<T> getEntitiesByClass(@NotNull Class<T> cls) { return getEntities().stream().filter(cls::isInstance).map(cls::cast).toList(); }
    @Override public @NotNull Collection<Entity> getEntitiesByClasses(@NotNull Class<?>... classes) { return getEntities().stream().filter(e -> Arrays.stream(classes).anyMatch(c -> c.isInstance(e))).toList(); }

    @SuppressWarnings("unchecked")
    private <T extends Entity> T createNms(Location location, Class<T> clazz, boolean randomize) {
        for (EntityType type : EntityType.values()) if (type.getEntityClass()!=null && clazz.isAssignableFrom(type.getEntityClass())) {
            var nmsType=net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(net.minecraft.resources.ResourceLocation.parse(type.getKey().toString()));
            if (nmsType==null) continue; var nms=nmsType.create(serverLevel()); if (nms==null) continue;
            nms.moveTo(location.getX(),location.getY(),location.getZ(),location.getYaw(),location.getPitch());
            if (randomize && nms instanceof Mob mob) mob.finalizeSpawn(getHandle(), getHandle().getCurrentDifficultyAt(nms.blockPosition()), MobSpawnType.COMMAND, null);
            Entity bukkit=bukkit(nms); if (bukkit!=null) return (T)bukkit;
        }
        throw new IllegalArgumentException("Cannot create entity of class "+clazz.getName());
    }
    @Override public <T extends Entity> @NotNull T createEntity(@NotNull Location location,@NotNull Class<T> clazz){ check(location.getBlockX(),location.getBlockY(),location.getBlockZ()); return createNms(location,clazz,true); }
    @Override public <T extends Entity> @NotNull T spawn(@NotNull Location location,@NotNull Class<T> clazz){ return spawn(location,clazz,true,null); }
    @Override public <T extends Entity> @NotNull T spawn(@NotNull Location location,@NotNull Class<T> clazz,boolean randomize,@Nullable Consumer<? super T> function){ return spawn0(location,clazz,randomize,function); }
    @Override public <T extends Entity> @NotNull T spawn(@NotNull Location location,@NotNull Class<T> clazz,@Nullable Consumer<? super T> function,CreatureSpawnEvent.@NotNull SpawnReason reason){ return spawn0(location,clazz,true,function); }
    private <T extends Entity> T spawn0(Location location,Class<T> clazz,boolean randomize,Consumer<? super T> function){ T result=createNms(location,clazz,randomize); if(function!=null)function.accept(result); entities.add(((CraftEntity)result).getHandle()); return result; }
    @Override public @NotNull Entity spawnEntity(@NotNull Location location,@NotNull EntityType type){ return spawn(location, Objects.requireNonNull(type.getEntityClass()), true, null); }
    @Override public @NotNull Entity spawnEntity(@NotNull Location location,@NotNull EntityType type,boolean randomize){ return spawn(location, Objects.requireNonNull(type.getEntityClass()), randomize, null); }
    @Override public <T extends Entity> @NotNull T addEntity(@NotNull T entity){ if(!(entity instanceof CraftEntity craft))throw new IllegalArgumentException("Entity must be CraftEntity"); entities.add(craft.getHandle()); return entity; }

    @Override public @NotNull List<BlockState> getTileEntities(){ List<BlockState> out=new ArrayList<>(); for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){ ChunkAccess access=getHandle().getChunk(centerChunkX+dx,centerChunkZ+dz,ChunkStatus.EMPTY); if(access instanceof ProtoChunk proto)for(BlockPos pos:proto.getBlockEntitiesPos())out.add(getBlockState(pos.getX(),pos.getY(),pos.getZ())); } return out; }
    @Override public void setBlockState(int x,int y,int z,@NotNull BlockState state){ check(x,y,z); if(!(state instanceof CraftBlockState craft))throw new IllegalArgumentException("BlockState must be CraftBlockState"); BlockPos pos=new BlockPos(x,y,z); getHandle().setBlock(pos,craft.getHandle(),net.minecraft.world.level.block.Block.UPDATE_ALL,512); if(state instanceof CraftBlockEntityState<?> tile){ var live=getHandle().getBlockEntity(pos); if(live!=null)live.loadWithComponents(tile.getSnapshotNBT(),getHandle().registryAccess()); } }
    @Override public void scheduleBlockUpdate(int x,int y,int z){ check(x,y,z); BlockPos pos=new BlockPos(x,y,z); getHandle().scheduleTick(pos,getHandle().getBlockState(pos).getBlock(),0); }
    @Override public void scheduleFluidUpdate(int x,int y,int z){ check(x,y,z); BlockPos pos=new BlockPos(x,y,z); getHandle().scheduleTick(pos,getHandle().getFluidState(pos).getType(),0); }
    @Override public @NotNull io.papermc.paper.world.MoonPhase getMoonPhase(){ io.papermc.paper.world.MoonPhase[] p=io.papermc.paper.world.MoonPhase.values(); int i=(int)Math.floorMod(serverLevel().getDayTime()/24000L,8L); return p[i%p.length]; }
    @Override public boolean lineOfSightExists(@NotNull Location from,@NotNull Location to){ var start=new net.minecraft.world.phys.Vec3(from.getX(),from.getY(),from.getZ()); var end=new net.minecraft.world.phys.Vec3(to.getX(),to.getY(),to.getZ()); var hit=getHandle().clip(new net.minecraft.world.level.ClipContext(start,end,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,net.minecraft.world.phys.shapes.CollisionContext.empty())); return hit.getType()==net.minecraft.world.phys.HitResult.Type.MISS; }
    @Override public boolean hasCollisionsIn(@NotNull BoundingBox box){ return !getHandle().noCollision(new net.minecraft.world.phys.AABB(box.getMinX(),box.getMinY(),box.getMinZ(),box.getMaxX(),box.getMaxY(),box.getMaxZ())); }
    @Override
    public @org.jetbrains.annotations.NotNull java.util.Set<org.bukkit.FeatureFlag> getFeatureFlags() {
        return ((org.bukkit.craftbukkit.CraftWorld) getWorld()).getFeatureFlags();
    }

}
