package org.bukkit.craftbukkit.structure;

import io.lunararcdevs.lunararc.common.bridge.access.StructureTemplateAccessBridge;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.bukkit.Location;
import org.bukkit.RegionAccessor;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.util.CraftStructureTransformer;
import org.bukkit.craftbukkit.util.TransformerGeneratorAccess;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.structure.Palette;
import org.bukkit.structure.Structure;
import org.bukkit.util.BlockTransformer;
import org.bukkit.util.BlockVector;
import org.bukkit.util.EntityTransformer;
import org.jetbrains.annotations.NotNull;


public final class CraftStructure implements Structure {
    static final String PDC_KEY = "LunarArc.BukkitValues";

    private final MinecraftServer server;
    private final StructureTemplate handle;
    private final CraftPersistentDataContainer persistentData = new CraftPersistentDataContainer();

    public CraftStructure(MinecraftServer server, StructureTemplate handle) {
        this.server = Objects.requireNonNull(server, "server");
        this.handle = Objects.requireNonNull(handle, "handle");
    }

    public StructureTemplate getHandle() {
        return handle;
    }

    CompoundTag saveTag() {
        CompoundTag tag = handle.save(new CompoundTag());
        if (!persistentData.isEmpty()) tag.put(PDC_KEY, persistentData.toTagCompound());
        return tag;
    }

    static CraftStructure fromTag(MinecraftServer server, CompoundTag tag) {
        StructureTemplate template = server.getStructureManager().readStructure(tag);
        CraftStructure structure = new CraftStructure(server, template);
        if (tag.contains(PDC_KEY, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            structure.persistentData.fromTag(tag.getCompound(PDC_KEY));
        }
        return structure;
    }

    @Override
    public @NotNull BlockVector getSize() {
        Vec3i size = handle.getSize();
        return new BlockVector(size.getX(), size.getY(), size.getZ());
    }

    @Override
    public @NotNull List<Palette> getPalettes() {
        List<Palette> out = new ArrayList<>();
        for (StructureTemplate.Palette palette : ((StructureTemplateAccessBridge) (Object) handle).lunararc$getPalettes()) {
            out.add(new CraftPalette(palette));
        }
        return List.copyOf(out);
    }

    @Override
    public int getPaletteCount() {
        return ((StructureTemplateAccessBridge) (Object) handle).lunararc$getPalettes().size();
    }

    @Override
    public @NotNull List<Entity> getEntities() {
        ServerLevel level = server.overworld();
        if (level == null) return List.of();
        CraftServer craftServer = (CraftServer) org.bukkit.Bukkit.getServer();
        List<Entity> result = new ArrayList<>();
        for (StructureTemplate.StructureEntityInfo info : ((StructureTemplateAccessBridge) (Object) handle).lunararc$getEntityInfoList()) {
            try {
                net.minecraft.world.entity.Entity entity = net.minecraft.world.entity.EntityType.loadEntityRecursive(
                        info.nbt.copy(), level, created -> created);
                if (entity == null) continue;
                entity.moveTo(info.pos.x, info.pos.y, info.pos.z, entity.getYRot(), entity.getXRot());
                Entity bukkit = CraftEntity.getEntity(craftServer, entity);
                if (bukkit != null) result.add(bukkit);
            } catch (Throwable ignored) {

            }
        }
        return List.copyOf(result);
    }

    @Override
    public int getEntityCount() {
        return ((StructureTemplateAccessBridge) (Object) handle).lunararc$getEntityInfoList().size();
    }

    private static CraftWorld requireWorld(RegionAccessor region) {
        if (region instanceof CraftWorld world) return world;
        throw new IllegalArgumentException("LunarArc structures require a CraftWorld-backed RegionAccessor");
    }

    private static Rotation rotation(StructureRotation rotation) {
        return switch (rotation) {
            case NONE -> Rotation.NONE;
            case CLOCKWISE_90 -> Rotation.CLOCKWISE_90;
            case CLOCKWISE_180 -> Rotation.CLOCKWISE_180;
            case COUNTERCLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
        };
    }

    private static Mirror mirror(org.bukkit.block.structure.Mirror mirror) {
        return switch (mirror) {
            case NONE -> Mirror.NONE;
            case LEFT_RIGHT -> Mirror.LEFT_RIGHT;
            case FRONT_BACK -> Mirror.FRONT_BACK;
        };
    }

    private boolean place0(CraftWorld world, BlockVector position, boolean includeEntities,
            StructureRotation rotation, org.bukkit.block.structure.Mirror mirror, int palette,
            float integrity, Random random) {
        return place0(world, position, includeEntities, rotation, mirror, palette, integrity, random, List.of(), List.of());
    }

    private boolean place0(CraftWorld world, BlockVector position, boolean includeEntities,
            StructureRotation rotation, org.bukkit.block.structure.Mirror mirror, int palette,
            float integrity, Random random, Collection<BlockTransformer> blockTransformers,
            Collection<EntityTransformer> entityTransformers) {
        if (integrity < 0.0F || integrity > 1.0F) throw new IllegalArgumentException("integrity must be between 0 and 1");
        if (palette < -1 || palette >= getPaletteCount()) throw new IllegalArgumentException("palette out of range: " + palette);
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(rotation(rotation))
                .setMirror(mirror(mirror))
                .setIgnoreEntities(!includeEntities);
        if (integrity < 1.0F) settings.addProcessor(new BlockRotProcessor(integrity));
        if (palette >= 0) {
            try {
                StructurePlaceSettings.class.getMethod("setPalette", int.class).invoke(settings, palette);
            } catch (NoSuchMethodException ignored) {

            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException("Unable to select structure palette", ex);
            }
        }
        RandomSource source = RandomSource.create(random.nextLong());
        BlockPos origin = new BlockPos(position.getBlockX(), position.getBlockY(), position.getBlockZ());
        WorldGenLevel target = world.getHandle();
        if (blockTransformers.isEmpty() && entityTransformers.isEmpty()) {
            return handle.placeInWorld(target, origin, origin, settings, source, 2);
        }

        CraftStructureTransformer transformer = new CraftStructureTransformer(
                target, new ChunkPos(origin), blockTransformers, entityTransformers);
        TransformerGeneratorAccess access = new TransformerGeneratorAccess(target, transformer);
        try {
            return handle.placeInWorld(access, origin, origin, settings, source, 2);
        } finally {
            transformer.discard();
        }
    }

    @Override
    public void place(@NotNull Location location, boolean includeEntities, @NotNull StructureRotation rotation,
            @NotNull org.bukkit.block.structure.Mirror mirror, int palette, float integrity, @NotNull Random random) {
        Objects.requireNonNull(location, "location");
        if (!(location.getWorld() instanceof CraftWorld world)) throw new IllegalArgumentException("Location must use a CraftWorld");
        place0(world, new BlockVector(location.getBlockX(), location.getBlockY(), location.getBlockZ()), includeEntities,
                rotation, mirror, palette, integrity, random);
    }

    @Override
    public void place(@NotNull Location location, boolean includeEntities, @NotNull StructureRotation rotation,
            @NotNull org.bukkit.block.structure.Mirror mirror, int palette, float integrity, @NotNull Random random,
            @NotNull Collection<BlockTransformer> blockTransformers,
            @NotNull Collection<EntityTransformer> entityTransformers) {
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(blockTransformers, "blockTransformers");
        Objects.requireNonNull(entityTransformers, "entityTransformers");
        if (!(location.getWorld() instanceof CraftWorld world)) throw new IllegalArgumentException("Location must use a CraftWorld");
        place0(world, new BlockVector(location.getBlockX(), location.getBlockY(), location.getBlockZ()), includeEntities,
                rotation, mirror, palette, integrity, random, blockTransformers, entityTransformers);
    }

    @Override
    public void place(@NotNull RegionAccessor region, @NotNull BlockVector position, boolean includeEntities,
            @NotNull StructureRotation rotation, @NotNull org.bukkit.block.structure.Mirror mirror, int palette,
            float integrity, @NotNull Random random) {
        place0(requireWorld(region), position, includeEntities, rotation, mirror, palette, integrity, random);
    }

    @Override
    public void place(@NotNull RegionAccessor region, @NotNull BlockVector position, boolean includeEntities,
            @NotNull StructureRotation rotation, @NotNull org.bukkit.block.structure.Mirror mirror, int palette,
            float integrity, @NotNull Random random, @NotNull Collection<BlockTransformer> blockTransformers,
            @NotNull Collection<EntityTransformer> entityTransformers) {
        Objects.requireNonNull(blockTransformers, "blockTransformers");
        Objects.requireNonNull(entityTransformers, "entityTransformers");
        place0(requireWorld(region), position, includeEntities, rotation, mirror, palette, integrity, random,
                blockTransformers, entityTransformers);
    }

    @Override
    public void fill(@NotNull Location corner1, @NotNull Location corner2, boolean includeEntities) {
        Objects.requireNonNull(corner1, "corner1");
        Objects.requireNonNull(corner2, "corner2");
        if (!(corner1.getWorld() instanceof CraftWorld world) || corner2.getWorld() != world) {
            throw new IllegalArgumentException("Both corners must belong to the same CraftWorld");
        }
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        handle.fillFromWorld(world.getHandle(), new BlockPos(minX, minY, minZ),
                new Vec3i(maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1), includeEntities, null);
    }

    @Override
    public void fill(@NotNull Location origin, @NotNull BlockVector size, boolean includeEntities) {
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(size, "size");
        if (!(origin.getWorld() instanceof CraftWorld world)) throw new IllegalArgumentException("Origin must use a CraftWorld");
        if (size.getBlockX() < 1 || size.getBlockY() < 1 || size.getBlockZ() < 1) {
            throw new IllegalArgumentException("Structure size must be positive");
        }
        handle.fillFromWorld(world.getHandle(), new BlockPos(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ()),
                new Vec3i(size.getBlockX(), size.getBlockY(), size.getBlockZ()), includeEntities, null);
    }

    @Override
    public @NotNull PersistentDataContainer getPersistentDataContainer() {
        return persistentData;
    }
}
