package org.bukkit.craftbukkit.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Chunk;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.CraftSoundGroup;
import org.bukkit.craftbukkit.metadata.CraftMetadataStore;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class CraftBlock implements Block {
    private final ServerLevel world;
    private final BlockPos position;
    private static final CraftMetadataStore<Block> METADATA = new CraftMetadataStore<>();

    public CraftBlock(ServerLevel world, BlockPos position) {
        this.world = world;
        this.position = position;
    }

    public static CraftBlock at(ServerLevel world, BlockPos position) {
        return new CraftBlock(world, position);
    }


    public static Block create(ServerLevel world, BlockPos position) {
        return new CraftBlock(world, position);
    }

    public static Block create(ServerLevel world, BlockPos position, net.minecraft.world.level.block.state.BlockState pendingState) {
        return new CraftBlock(world, position);
    }

    public ServerLevel getHandle() {
        return world;
    }

    public BlockPos getPosition() {
        return position;
    }


    @Override
    public @NotNull org.bukkit.SoundGroup getBlockSoundGroup() {
        return new CraftSoundGroup(nmsState().getSoundType());
    }

    @SuppressWarnings("deprecation")
    public @NotNull com.destroystokyo.paper.block.BlockSoundGroup getSoundGroup() {
        return new CraftSoundGroup(nmsState().getSoundType());
    }

    @Override
    public @NotNull String getTranslationKey() {
        net.minecraft.world.level.block.state.BlockState state = world.getBlockState(position);
        return state.getBlock().getDescriptionId();
    }

    @Override
    public @NotNull String translationKey() {
        return getTranslationKey();
    }

    @Override
    public @NotNull World getWorld() {
        org.bukkit.Server server = org.bukkit.Bukkit.getServer();
        if (server instanceof org.bukkit.craftbukkit.CraftServer craftServer) {
            return craftServer.getCraftWorld(world);
        }
        return new CraftWorld(world);
    }

    @Override
    public int getX() { return position.getX(); }

    @Override
    public int getY() { return position.getY(); }

    @Override
    public int getZ() { return position.getZ(); }

    @Override
    public @NotNull Location getLocation() {
        return new Location(getWorld(), position.getX(), position.getY(), position.getZ());
    }

    @Override
    public @Nullable Location getLocation(@Nullable Location loc) {
        if (loc != null) {
            loc.setWorld(getWorld());
            loc.setX(position.getX());
            loc.setY(position.getY());
            loc.setZ(position.getZ());
        }
        return loc;
    }


    private net.minecraft.world.level.block.state.BlockState nmsState() {
        return world.getBlockState(position);
    }

    private static Material materialFromState(net.minecraft.world.level.block.state.BlockState state) {
        // Go through CraftMagicNumbers rather than resolving the name here. It is the same
        // lookup CraftBukkit's CraftBlock uses, it is memoised so reading a block's type costs
        // no string building, and it consults the dynamic Material registry - so a block from a
        // Forge/Fabric mod resolves instead of throwing, which the local copy of this did.
        return org.bukkit.craftbukkit.util.CraftMagicNumbers.getMaterial(state.getBlock());
    }

    @Override
    public @NotNull Material getType() {
        return materialFromState(nmsState());
    }

    @Override
    public void setType(@NotNull Material type) {
        setType(type, true);
    }

    @Override
    public void setType(@NotNull Material type, boolean applyPhysics) {
        java.util.Objects.requireNonNull(type, "type");
        net.minecraft.world.level.block.state.BlockState state;
        if (type.isLegacy()) {
            state = org.bukkit.craftbukkit.legacy.CraftLegacy.fromLegacyData(type, (byte) 0);
        } else {
            ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(type.getKey().getNamespace(), type.getKey().getKey());
            if (!BuiltInRegistries.BLOCK.containsKey(rl)) throw new IllegalArgumentException("Material is not a block: " + type);
            state = BuiltInRegistries.BLOCK.get(rl).defaultBlockState();
        }
        world.setBlock(position, state, applyPhysics ? 3 : 2);
    }

    @Override
    public @NotNull BlockData getBlockData() {
        return CraftBlockData.createData(nmsState());
    }

    @Override
    public void setBlockData(@NotNull BlockData data) {
        setBlockData(data, true);
    }

    @Override
    public void setBlockData(@NotNull BlockData data, boolean applyPhysics) {
        net.minecraft.world.level.block.state.BlockState nms;
        if (data instanceof CraftBlockData craftData) {
            nms = craftData.getState();
        } else {
            nms = org.bukkit.craftbukkit.block.data.CraftBlockData.parse(data.getAsString()).getState();
        }
        world.setBlock(position, nms, applyPhysics ? 3 : 2);
    }

    @Override
    public byte getData() {
        return org.bukkit.craftbukkit.legacy.CraftLegacy.toLegacyData(nmsState());
    }

    public void setData(byte data) {
        setData(data, true);
    }

    public void setData(byte data, boolean applyPhysics) {
        Material legacy = org.bukkit.craftbukkit.legacy.CraftLegacy.toLegacyMaterial(nmsState());
        net.minecraft.world.level.block.state.BlockState state =
                org.bukkit.craftbukkit.legacy.CraftLegacy.fromLegacyData(legacy, data);
        world.setBlock(position, state, applyPhysics ? 3 : 2);
    }


    @Override
    public @NotNull BlockState getState() {
        return getState(true);
    }

    @Override
    public @NotNull BlockState getState(boolean useSnapshot) {
        // Route through CraftBukkit's exact 1.21.1 block-state factory surface so
        // chests, signs, skulls, spawners, containers, etc. expose their specialized
        // Bukkit state types. The factory class itself is supplied from the exact
        // NMS-free Paper 1.21.1 implementation donor when LunarArc does not own it.
        return org.bukkit.craftbukkit.block.CraftBlockStates.getBlockState(this, useSnapshot);
    }


    @Override
    public boolean canPlace(@NotNull BlockData data) {
        java.util.Objects.requireNonNull(data, "data");
        net.minecraft.world.level.block.state.BlockState candidate;
        if (data instanceof CraftBlockData craftData) candidate = craftData.getState();
        else candidate = org.bukkit.craftbukkit.block.data.CraftBlockData.parse(data.getAsString()).getState();
        return candidate.canSurvive(world, position);
    }

    @Override
    public float getBreakSpeed(@NotNull org.bukkit.entity.Player player) {
        java.util.Objects.requireNonNull(player, "player");
        if (!(player instanceof org.bukkit.craftbukkit.entity.CraftPlayer craftPlayer)) return 0.0F;
        return nmsState().getDestroyProgress(craftPlayer.getHandle(), world, position);
    }

    @Override
    public void randomTick() {
        net.minecraft.world.level.block.state.BlockState state = nmsState();
        state.randomTick(world, position, world.getRandom());
    }

    @Override
    public void tick() {
        nmsState().tick(world, position, world.getRandom());
    }

    @Override
    public void fluidTick() {
        net.minecraft.world.level.material.FluidState fluid = world.getFluidState(position);
        if (!fluid.isEmpty()) fluid.tick(world, position);
    }

    @Override
    public int getBlockPower() { return world.getBestNeighborSignal(position); }

    @Override
    public int getBlockPower(@NotNull BlockFace face) {
        java.util.Objects.requireNonNull(face, "face");
        net.minecraft.core.Direction direction = toDirection(face);
        return direction == null ? 0 : world.getSignal(position, direction);
    }

    @Override
    public boolean isBlockFaceIndirectlyPowered(@NotNull BlockFace face) {
        java.util.Objects.requireNonNull(face, "face");
        net.minecraft.core.Direction direction = toDirection(face);
        return direction != null && world.getDirectSignal(position, direction) > 0;
    }

    @Override
    public boolean isBlockFacePowered(@NotNull BlockFace face) { return getBlockPower(face) > 0; }

    @Override
    public boolean isBlockIndirectlyPowered() { return world.hasNeighborSignal(position); }

    @Override
    public boolean isBlockPowered() { return getBlockPower() > 0; }

    @Override
    public boolean isBuildable() { return nmsState().isSolid(); }

    @Override
    public boolean isBurnable() { return nmsState().ignitedByLava(); }

    @Override
    public boolean isCollidable() { return nmsState().isSolid(); }

    @Override
    public boolean isReplaceable() { return nmsState().canBeReplaced(); }

    public boolean isValidTool(@NotNull ItemStack tool) {
        java.util.Objects.requireNonNull(tool, "tool");
        return org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(tool).isCorrectToolForDrops(nmsState());
    }

    @Override
    public @NotNull org.bukkit.util.VoxelShape getCollisionShape() {
        net.minecraft.world.phys.shapes.VoxelShape nmsShape = nmsState().getCollisionShape(world, position);
        java.util.List<org.bukkit.util.BoundingBox> boxes = new java.util.ArrayList<>();
        for (net.minecraft.world.phys.AABB box : nmsShape.toAabbs()) {
            boxes.add(new org.bukkit.util.BoundingBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ));
        }
        java.util.List<org.bukkit.util.BoundingBox> immutable = java.util.List.copyOf(boxes);
        return new org.bukkit.util.VoxelShape() {
            @Override public @NotNull java.util.Collection<org.bukkit.util.BoundingBox> getBoundingBoxes() { return immutable; }
            @Override public boolean overlaps(@NotNull org.bukkit.util.BoundingBox other) {
                java.util.Objects.requireNonNull(other, "other");
                for (org.bukkit.util.BoundingBox box : immutable) if (box.overlaps(other)) return true;
                return false;
            }
        };
    }

    @Override
    public boolean isSolid() { return nmsState().isSolid(); }

    @Override
    public boolean isEmpty() { return nmsState().isAir(); }

    @Override
    public boolean isLiquid() { return !world.getFluidState(position).isEmpty(); }

    @Override
    public boolean isPassable() { return !nmsState().blocksMotion(); }

    @Override
    public boolean isPreferredTool(@NotNull ItemStack tool) {
        java.util.Objects.requireNonNull(tool, "tool");
        return org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(tool).isCorrectToolForDrops(nmsState());
    }

    @Override
    public @NotNull PistonMoveReaction getPistonMoveReaction() {
        return switch (nmsState().getPistonPushReaction()) {
            case NORMAL -> PistonMoveReaction.MOVE;
            case DESTROY -> PistonMoveReaction.BREAK;
            case BLOCK -> PistonMoveReaction.BLOCK;
            case PUSH_ONLY -> PistonMoveReaction.PUSH_ONLY;
            case IGNORE -> PistonMoveReaction.IGNORE;
        };
    }

    /**
     * CraftBukkit's own Direction to BlockFace conversion, public because plugins call it and
     * because CraftBukkit's implementation classes use it wherever a vanilla Direction has to be
     * reported to the Bukkit API - the fluid-flow events among them.
     */
    public static BlockFace notchToBlockFace(net.minecraft.core.Direction notch) {
        if (notch == null) return BlockFace.SELF;
        return switch (notch) {
            case DOWN -> BlockFace.DOWN;
            case UP -> BlockFace.UP;
            case NORTH -> BlockFace.NORTH;
            case SOUTH -> BlockFace.SOUTH;
            case WEST -> BlockFace.WEST;
            case EAST -> BlockFace.EAST;
        };
    }

    private static net.minecraft.core.Direction toDirection(BlockFace face) {
        return switch (face) {
            case DOWN -> net.minecraft.core.Direction.DOWN;
            case UP -> net.minecraft.core.Direction.UP;
            case NORTH -> net.minecraft.core.Direction.NORTH;
            case SOUTH -> net.minecraft.core.Direction.SOUTH;
            case WEST -> net.minecraft.core.Direction.WEST;
            case EAST -> net.minecraft.core.Direction.EAST;
            default -> null;
        };
    }

    @Override
    public boolean breakNaturally() { return breakNaturally(new ItemStack(Material.AIR)); }

    @Override
    public boolean breakNaturally(@NotNull ItemStack tool) {
        world.destroyBlock(position, true);
        return true;
    }

    @Override
    public boolean breakNaturally(boolean triggerEffect, boolean dropExperience) {
        world.destroyBlock(position, true);
        return true;
    }

    @Override
    public boolean breakNaturally(@NotNull ItemStack tool, boolean triggerEffect) {
        return breakNaturally(tool);
    }

    @Override
    public boolean breakNaturally(@NotNull ItemStack tool, boolean triggerEffect, boolean dropExperience) {
        return breakNaturally(tool);
    }

    @Override
    public boolean applyBoneMeal(@NotNull BlockFace face) {
        Objects.requireNonNull(face, "face");
        net.minecraft.world.level.block.state.BlockState state = nmsState();
        if (!(state.getBlock() instanceof net.minecraft.world.level.block.BonemealableBlock bonemealable)) return false;
        if (!bonemealable.isValidBonemealTarget(world, position, state)) return false;
        if (!bonemealable.isBonemealSuccess(world, world.getRandom(), position, state)) return false;
        bonemealable.performBonemeal(world, world.getRandom(), position, state);
        return true;
    }

    @Override
    public @NotNull Collection<ItemStack> getDrops() { return getDrops(new ItemStack(Material.AIR), null); }

    @Override
    public @NotNull Collection<ItemStack> getDrops(@NotNull ItemStack tool) { return getDrops(tool, null); }

    @Override
    public @NotNull Collection<ItemStack> getDrops(@NotNull ItemStack tool, @Nullable Entity entity) {
        java.util.Objects.requireNonNull(tool, "tool");
        net.minecraft.world.entity.Entity nmsEntity = entity instanceof org.bukkit.craftbukkit.entity.CraftEntity craft
                ? craft.getHandle() : null;
        net.minecraft.world.item.ItemStack nmsTool = org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(tool);
        net.minecraft.world.level.block.entity.BlockEntity blockEntity = world.getBlockEntity(position);
        java.util.List<net.minecraft.world.item.ItemStack> drops = net.minecraft.world.level.block.Block.getDrops(
                nmsState(), world, position, blockEntity, nmsEntity, nmsTool);
        java.util.List<ItemStack> result = new java.util.ArrayList<>(drops.size());
        for (net.minecraft.world.item.ItemStack drop : drops) {
            if (!drop.isEmpty()) result.add(org.bukkit.craftbukkit.inventory.CraftItemStack.asBukkitCopy(drop));
        }
        return java.util.List.copyOf(result);
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack itemStack, boolean considerEnchants) {
        java.util.Objects.requireNonNull(itemStack, "itemStack");
        net.minecraft.world.item.ItemStack nms = org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(itemStack);
        return nms.isEmpty() ? 1.0F : nms.getDestroySpeed(nmsState());
    }

    public boolean isPreferredTool(@NotNull ItemStack tool, @Nullable BlockFace face) {
        return isPreferredTool(tool);
    }


    @Override
    public @NotNull Block getRelative(int modX, int modY, int modZ) {
        return new CraftBlock(world, position.offset(modX, modY, modZ));
    }

    @Override
    public @NotNull Block getRelative(@NotNull BlockFace face) {
        return getRelative(face.getModX(), face.getModY(), face.getModZ());
    }

    @Override
    public @NotNull Block getRelative(@NotNull BlockFace face, int distance) {
        return getRelative(face.getModX() * distance, face.getModY() * distance, face.getModZ() * distance);
    }

    @Override
    public @NotNull BlockFace getFace(@NotNull Block block) {
        for (BlockFace face : BlockFace.values()) {
            Block rel = getRelative(face);
            if (rel.getX() == block.getX() && rel.getY() == block.getY() && rel.getZ() == block.getZ()) {
                return face;
            }
        }
        return BlockFace.SELF;
    }


    @Override
    public @NotNull Chunk getChunk() {
        return getWorld().getChunkAt(this);
    }

    @Override
    public @NotNull Biome getBiome() {
        net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> holder = world.getBiome(position);
        net.minecraft.resources.ResourceLocation key = holder.unwrapKey().map(net.minecraft.resources.ResourceKey::location).orElse(null);
        if (key != null) {
            Biome biome = org.bukkit.Registry.BIOME.get(new NamespacedKey(key.getNamespace(), key.getPath()));
            if (biome != null) return biome;
        }
        Biome plains = org.bukkit.Registry.BIOME.get(NamespacedKey.minecraft("plains"));
        if (plains == null) throw new IllegalStateException("minecraft:plains biome is unavailable");
        return plains;
    }

    @Override
    public void setBiome(@NotNull Biome bio) {
        java.util.Objects.requireNonNull(bio, "bio");
        getWorld().setBiome(position.getX(), position.getY(), position.getZ(), bio);
    }

    @Override
    public @NotNull Biome getComputedBiome() { return getBiome(); }


    @Override
    public byte getLightLevel() { return (byte) world.getMaxLocalRawBrightness(position); }

    @Override
    public byte getLightFromSky() { return (byte) world.getBrightness(net.minecraft.world.level.LightLayer.SKY, position); }

    @Override
    public byte getLightFromBlocks() { return (byte) world.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, position); }

    @Override
    public double getTemperature() { return world.getBiome(position).value().getBaseTemperature(); }

    @Override
    public double getHumidity() { return world.getBiome(position).value().climateSettings.downfall(); }


    @Override
    public void setMetadata(@NotNull String key, @NotNull MetadataValue val) { METADATA.setMetadata(this, key, val); }

    @Override
    public @NotNull List<MetadataValue> getMetadata(@NotNull String key) { return METADATA.getMetadata(this, key); }

    @Override
    public boolean hasMetadata(@NotNull String key) { return METADATA.hasMetadata(this, key); }

    @Override
    public void removeMetadata(@NotNull String key, @NotNull Plugin plugin) { METADATA.removeMetadata(this, key, plugin); }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CraftBlock other)) return false;
        return position.equals(other.position) && world == other.world;
    }

    @Override
    public int hashCode() {
        return position.hashCode() * 31 + System.identityHashCode(world);
    }

    @Override
    public String toString() {
        return "CraftBlock{world=" + world.dimension().location() + ",pos=" + position + ",type=" + getType() + "}";
    }


    @Override
    public @Nullable RayTraceResult rayTrace(@NotNull Location start, @NotNull Vector dir, double maxDist, @NotNull FluidCollisionMode mode) {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(dir, "dir");
        Objects.requireNonNull(mode, "mode");
        if (start.getWorld() != getWorld()) throw new IllegalArgumentException("Start location must be in the same world");
        if (maxDist < 0.0D || dir.lengthSquared() == 0.0D) return null;
        Vector normalized = dir.clone().normalize();
        net.minecraft.world.phys.Vec3 from = new net.minecraft.world.phys.Vec3(start.getX(), start.getY(), start.getZ());
        net.minecraft.world.phys.Vec3 to = from.add(normalized.getX() * maxDist, normalized.getY() * maxDist, normalized.getZ() * maxDist);
        net.minecraft.world.phys.BlockHitResult best = nmsState().getShape(world, position).clip(from, to, position);

        net.minecraft.world.level.material.FluidState fluid = world.getFluidState(position);
        boolean traceFluid = mode == FluidCollisionMode.ALWAYS || (mode == FluidCollisionMode.SOURCE_ONLY && fluid.isSource());
        if (traceFluid && !fluid.isEmpty()) {
            net.minecraft.world.phys.BlockHitResult fluidHit = fluid.getShape(world, position).clip(from, to, position);
            if (fluidHit != null && (best == null || fluidHit.getLocation().distanceToSqr(from) < best.getLocation().distanceToSqr(from))) best = fluidHit;
        }
        if (best == null) return null;
        net.minecraft.world.phys.Vec3 hit = best.getLocation();
        return new RayTraceResult(new Vector(hit.x, hit.y, hit.z), this, fromDirection(best.getDirection()));
    }

    private static BlockFace fromDirection(net.minecraft.core.Direction direction) {
        return switch (direction) {
            case DOWN -> BlockFace.DOWN;
            case UP -> BlockFace.UP;
            case NORTH -> BlockFace.NORTH;
            case SOUTH -> BlockFace.SOUTH;
            case WEST -> BlockFace.WEST;
            case EAST -> BlockFace.EAST;
        };
    }

    @Override
    public @NotNull BoundingBox getBoundingBox() {
        return BoundingBox.of(getLocation(), getLocation().add(1, 1, 1));
    }

    // CraftBukkit's NMS-facing accessors. Plugins that reach past the Bukkit API - schematic
    // tools, protection plugins, anything that wants the real BlockState - cast to CraftBlock and
    // call these by name, so they carry CraftBukkit's signatures.
    public net.minecraft.world.level.block.state.BlockState getNMS() {
        return this.world.getBlockState(this.position);
    }

    public net.minecraft.world.level.material.FluidState getNMSFluid() {
        return this.world.getFluidState(this.position);
    }

    public CraftWorld getCraftWorld() {
        return (CraftWorld) this.getWorld();
    }

    public org.bukkit.util.BlockVector getVector() {
        return new org.bukkit.util.BlockVector(this.getX(), this.getY(), this.getZ());
    }

    /** The inverse of {@link #notchToBlockFace}; returns null for any face vanilla has no Direction for. */
    public static net.minecraft.core.Direction blockFaceToNotch(BlockFace face) {
        if (face == null) {
            return null;
        }
        return switch (face) {
            case DOWN -> net.minecraft.core.Direction.DOWN;
            case UP -> net.minecraft.core.Direction.UP;
            case NORTH -> net.minecraft.core.Direction.NORTH;
            case SOUTH -> net.minecraft.core.Direction.SOUTH;
            case WEST -> net.minecraft.core.Direction.WEST;
            case EAST -> net.minecraft.core.Direction.EAST;
            default -> null;
        };
    }

    public static boolean setTypeAndData(net.minecraft.world.level.LevelAccessor world, BlockPos position,
            net.minecraft.world.level.block.state.BlockState old,
            net.minecraft.world.level.block.state.BlockState blockData, boolean applyPhysics) {
        // SPIGOT-611: clear the old block entity first, or a block replaced over one leaves a
        // stale tile behind. SPIGOT-4612: clearing beats a full cleanup pass.
        if (old.hasBlockEntity() && blockData.getBlock() != old.getBlock()) {
            if (world instanceof net.minecraft.world.level.Level level) {
                level.removeBlockEntity(position);
            } else {
                world.setBlock(position, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 0);
            }
        }

        if (applyPhysics) {
            return world.setBlock(position, blockData, 3);
        }

        boolean success = world.setBlock(position, blockData, 2 | 16 | 1024); // NOTIFY | NO_OBSERVER | NO_PLACE
        // CraftBukkit reaches the Level through LevelAccessor#getMinecraftWorld, which CraftBukkit
        // patches onto NMS; lunararc-common compiles against vanilla, where it does not exist. The
        // instanceof already narrows to the same object, so bind it and call directly.
        if (success && world instanceof net.minecraft.world.level.Level level) {
            level.sendBlockUpdated(position, old, blockData, 3);
        }
        return success;
    }
}
