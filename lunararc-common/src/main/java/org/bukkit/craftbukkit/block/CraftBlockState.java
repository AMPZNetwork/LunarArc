package org.bukkit.craftbukkit.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.metadata.CraftMetadataStore;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Hybrid-safe CraftBukkit 1.21.1 block-state base.
 *
 * <p>The protected field/method/constructor surface intentionally follows the
 * CraftBukkit ABI used by generated Paper 1.21.1 block-state implementations.
 * The actual level and block data remain the loader-owned Minecraft objects.</p>
 */
@SuppressWarnings({"deprecation", "removal"})
public class CraftBlockState implements BlockState {
    // CraftBukkit ABI: generated/specialized Craft states directly access these.
    protected final @Nullable CraftWorld world;
    private final BlockPos position;
    protected net.minecraft.world.level.block.state.BlockState data;
    protected int flag = 3;

    private final boolean placed;
    private @Nullable ServerLevel serverLevel;
    private @Nullable WeakReference<LevelAccessor> weakWorldHandle;
    private byte rawData;
    private final CraftMetadataStore<BlockState> localMetadata = new CraftMetadataStore<>();

    public CraftBlockState(@NotNull ServerLevel world, @NotNull BlockPos position,
            @NotNull net.minecraft.world.level.block.state.BlockState state) {
        this(craftWorld(world), world, position, state, true);
    }

    protected CraftBlockState(@Nullable ServerLevel world, @NotNull BlockPos position,
            @NotNull net.minecraft.world.level.block.state.BlockState state, boolean placed) {
        this(world == null ? null : craftWorld(world), world, position, state, placed);
    }

    /** CraftBukkit 1.21.1 ABI constructor used by donated specialized states. */
    protected CraftBlockState(@Nullable World world, @NotNull BlockPos position,
            @NotNull net.minecraft.world.level.block.state.BlockState state) {
        this(world == null ? null : requireCraftWorld(world),
                world == null ? null : requireCraftWorld(world).getHandle(), position, state, world != null);
    }

    /** CraftBukkit 1.21.1 ABI constructor used by CraftBlockStates. */
    protected CraftBlockState(@NotNull Block block) {
        this(block, 3);
    }

    /** CraftBukkit 1.21.1 ABI constructor used by CraftBlockStates. */
    protected CraftBlockState(@NotNull Block block, int flag) {
        Objects.requireNonNull(block, "block");
        if (!(block instanceof CraftBlock craftBlock)) {
            throw new IllegalArgumentException("Block must be backed by LunarArc CraftBlock");
        }
        this.world = requireCraftWorld(block.getWorld());
        this.serverLevel = craftBlock.getHandle();
        this.position = craftBlock.getPosition().immutable();
        this.data = this.serverLevel.getBlockState(this.position);
        this.placed = true;
        this.flag = flag;
    }

    /** Creates an unplaced copy at the original or supplied location. */
    protected CraftBlockState(@NotNull CraftBlockState state, @Nullable Location location) {
        Objects.requireNonNull(state, "state");
        CraftWorld targetWorld = null;
        ServerLevel targetLevel = null;
        BlockPos targetPosition;
        if (location == null) {
            targetPosition = state.position.immutable();
        } else {
            if (location.getWorld() != null) {
                targetWorld = requireCraftWorld(location.getWorld());
                targetLevel = targetWorld.getHandle();
            }
            targetPosition = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        }
        this.world = targetWorld;
        this.serverLevel = targetLevel;
        this.position = targetPosition;
        this.data = state.data;
        this.placed = false;
        this.flag = state.flag;
        this.rawData = state.rawData;
        LevelAccessor sourceHandle = state.getWorldHandle();
        if (sourceHandle != null && !(sourceHandle instanceof ServerLevel)) {
            this.weakWorldHandle = new WeakReference<>(sourceHandle);
        }
    }

    private CraftBlockState(@Nullable CraftWorld world, @Nullable ServerLevel serverLevel, BlockPos position,
            net.minecraft.world.level.block.state.BlockState state, boolean placed) {
        this.world = world;
        this.serverLevel = serverLevel;
        this.position = Objects.requireNonNull(position, "position").immutable();
        this.data = Objects.requireNonNull(state, "state");
        this.placed = placed;
    }

    private static CraftWorld requireCraftWorld(World world) {
        if (!(world instanceof CraftWorld craftWorld)) {
            throw new IllegalArgumentException("World must be backed by LunarArc CraftWorld");
        }
        return craftWorld;
    }

    private static CraftWorld craftWorld(ServerLevel level) {
        org.bukkit.Server server = org.bukkit.Bukkit.getServer();
        if (server instanceof org.bukkit.craftbukkit.CraftServer craftServer) {
            return craftServer.getCraftWorld(level);
        }
        return new CraftWorld(level);
    }

    public net.minecraft.world.level.block.state.BlockState getHandle() {
        return this.data;
    }

    /** CraftBukkit ABI setter used by specialized implementation code. */
    public void setData(net.minecraft.world.level.block.state.BlockState data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    public static @NotNull CraftBlockState unplaced(@NotNull net.minecraft.world.level.block.state.BlockState state) {
        return new CraftBlockState((ServerLevel) null, BlockPos.ZERO, state, false);
    }

    public static @NotNull CraftBlockState unplacedAt(@NotNull BlockPos position,
            @NotNull net.minecraft.world.level.block.state.BlockState state) {
        return new CraftBlockState((ServerLevel) null, position, state, false);
    }

    private static Material toMaterial(net.minecraft.world.level.block.state.BlockState state) {
        return org.bukkit.craftbukkit.util.CraftMagicNumbers.getMaterial(state.getBlock());
    }

    private static net.minecraft.world.level.block.state.BlockState stateFor(Material material) {
        Objects.requireNonNull(material, "material");
        if (material.isLegacy()) {
            return org.bukkit.craftbukkit.legacy.CraftLegacy.fromLegacyData(material, (byte) 0);
        }
        ResourceLocation key = ResourceLocation.fromNamespaceAndPath(material.getKey().getNamespace(), material.getKey().getKey());
        if (!BuiltInRegistries.BLOCK.containsKey(key)) throw new IllegalArgumentException("Material is not a block: " + material);
        return BuiltInRegistries.BLOCK.get(key).defaultBlockState();
    }

    protected final void requirePlaced() {
        if (!this.placed || this.serverLevel == null || this.world == null) {
            throw new IllegalStateException("BlockState is not placed");
        }
    }

    /**
     * CraftBukkit-compatible level accessor. World-generation states may expose
     * a non-Level accessor without pretending that the state is placed.
     */
    public LevelAccessor getWorldHandle() {
        WeakReference<LevelAccessor> weak = this.weakWorldHandle;
        if (weak != null) {
            LevelAccessor access = weak.get();
            if (access != null) return access;
            this.weakWorldHandle = null;
        }
        return this.serverLevel;
    }

    public void setWorldHandle(LevelAccessor access) {
        if (access instanceof ServerLevel level) {
            this.serverLevel = level;
            this.weakWorldHandle = null;
        } else {
            this.weakWorldHandle = access == null ? null : new WeakReference<>(access);
        }
    }

    protected final @Nullable ServerLevel getServerLevelHandle() {
        LevelAccessor access = getWorldHandle();
        return access instanceof ServerLevel level ? level : null;
    }

    protected final boolean isWorldGeneration() {
        LevelAccessor access = getWorldHandle();
        return access != null && !(access instanceof net.minecraft.world.level.Level);
    }

    protected final void ensureNoWorldGeneration() {
        if (isWorldGeneration()) {
            throw new IllegalStateException("This operation is not supported during world generation!");
        }
    }

    public final BlockPos getPosition() {
        return this.position;
    }

    @Override
    public @NotNull Block getBlock() {
        requirePlaced();
        return CraftBlock.at(Objects.requireNonNull(this.serverLevel, "serverLevel"), this.position);
    }

    @Override
    public @NotNull MaterialData getData() {
        return org.bukkit.craftbukkit.legacy.CraftLegacy.toLegacy(this.data);
    }

    @Override
    public @NotNull BlockData getBlockData() {
        return CraftBlockData.createData(this.data);
    }

    @Override
    public @NotNull BlockState copy() {
        CraftBlockState copy = new CraftBlockState(this, null);
        copy.rawData = this.rawData;
        return copy;
    }

    @Override
    public @NotNull BlockState copy(@NotNull Location location) {
        Objects.requireNonNull(location, "location");
        CraftBlockState copy = new CraftBlockState(this, location);
        copy.rawData = this.rawData;
        return copy;
    }

    @Override
    public @NotNull Material getType() {
        return toMaterial(this.data);
    }

    @Override
    public byte getLightLevel() {
        requirePlaced();
        return (byte) Objects.requireNonNull(this.serverLevel, "serverLevel").getMaxLocalRawBrightness(this.position);
    }

    @Override
    public @NotNull World getWorld() {
        requirePlaced();
        return Objects.requireNonNull(this.world, "world");
    }

    @Override public int getX() { return this.position.getX(); }
    @Override public int getY() { return this.position.getY(); }
    @Override public int getZ() { return this.position.getZ(); }

    @Override
    public @NotNull Location getLocation() {
        return new Location(this.placed ? getWorld() : null, getX(), getY(), getZ());
    }

    @Override
    public @Nullable Location getLocation(@Nullable Location loc) {
        if (loc == null) return null;
        loc.setWorld(this.placed ? getWorld() : null);
        loc.setX(getX());
        loc.setY(getY());
        loc.setZ(getZ());
        return loc;
    }

    @Override
    public @NotNull Chunk getChunk() {
        requirePlaced();
        return getWorld().getChunkAt(getX() >> 4, getZ() >> 4);
    }

    @Override
    public void setData(@NotNull MaterialData data) {
        Objects.requireNonNull(data, "data");
        Material material = data.getItemType();
        if (material == null || !material.isLegacy()) throw new IllegalArgumentException("MaterialData must contain a legacy Material");
        this.data = org.bukkit.craftbukkit.legacy.CraftLegacy.fromLegacyData(material, data.getData());
        this.rawData = data.getData();
    }

    @Override
    public void setBlockData(@NotNull BlockData data) {
        Objects.requireNonNull(data, "data");
        if (data instanceof CraftBlockData craft) {
            this.data = craft.getState();
        } else {
            this.data = CraftBlockData.parse(data.getAsString()).getState();
        }
    }

    @Override
    public void setType(@NotNull Material type) {
        this.data = stateFor(type);
        this.rawData = 0;
    }

    @Override public boolean update() { return update(false); }
    @Override public boolean update(boolean force) { return update(force, true); }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        if (!this.placed || this.serverLevel == null) return true;
        net.minecraft.world.level.block.state.BlockState live = this.serverLevel.getBlockState(this.position);
        if (!force && live.getBlock() != this.data.getBlock()) return false;
        return this.serverLevel.setBlock(this.position, this.data, applyPhysics ? this.flag : (this.flag & ~1));
    }

    @Override public byte getRawData() {
        return org.bukkit.craftbukkit.legacy.CraftLegacy.toLegacyData(this.data);
    }

    @Override public void setRawData(byte data) {
        Material legacy = org.bukkit.craftbukkit.legacy.CraftLegacy.toLegacyMaterial(this.data);
        this.data = org.bukkit.craftbukkit.legacy.CraftLegacy.fromLegacyData(legacy, data);
        this.rawData = data;
    }

    @Override public boolean isPlaced() { return this.placed; }

    @Override
    public boolean isCollidable() {
        return !this.data.getCollisionShape(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty();
    }

    @Override
    public @NotNull Collection<ItemStack> getDrops(@Nullable ItemStack tool, @Nullable Entity entity) {
        requirePlaced();
        ServerLevel level = Objects.requireNonNull(this.serverLevel, "serverLevel");
        net.minecraft.world.item.ItemStack nmsTool = tool == null
                ? net.minecraft.world.item.ItemStack.EMPTY : CraftItemStack.asNMSCopy(tool);
        net.minecraft.world.entity.Entity nmsEntity = entity instanceof CraftEntity craft ? craft.getHandle() : null;
        net.minecraft.world.level.block.entity.BlockEntity tile = level.getBlockEntity(this.position);
        List<net.minecraft.world.item.ItemStack> nmsDrops = net.minecraft.world.level.block.Block.getDrops(
                this.data, level, this.position, tile, nmsEntity, nmsTool);
        List<ItemStack> drops = new ArrayList<>(nmsDrops.size());
        for (net.minecraft.world.item.ItemStack drop : nmsDrops) {
            if (!drop.isEmpty()) drops.add(CraftItemStack.asBukkitCopy(drop));
        }
        return List.copyOf(drops);
    }

    @Override
    public void setMetadata(@NotNull String metadataKey, @NotNull MetadataValue newMetadataValue) {
        if (this.placed) getBlock().setMetadata(metadataKey, newMetadataValue);
        else this.localMetadata.setMetadata(this, metadataKey, newMetadataValue);
    }

    @Override
    public @NotNull List<MetadataValue> getMetadata(@NotNull String metadataKey) {
        return this.placed ? getBlock().getMetadata(metadataKey) : this.localMetadata.getMetadata(this, metadataKey);
    }

    @Override
    public boolean hasMetadata(@NotNull String metadataKey) {
        return this.placed ? getBlock().hasMetadata(metadataKey) : this.localMetadata.hasMetadata(this, metadataKey);
    }

    @Override
    public void removeMetadata(@NotNull String metadataKey, @NotNull Plugin owningPlugin) {
        if (this.placed) getBlock().removeMetadata(metadataKey, owningPlugin);
        else this.localMetadata.removeMetadata(this, metadataKey, owningPlugin);
    }

    @Override
    public String toString() {
        return "CraftBlockState{" + getType() + " @ " + this.position + ", placed=" + this.placed + '}';
    }
}
