package org.bukkit.craftbukkit.entity;

import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CraftEntitySnapshot implements EntitySnapshot {
    private final CompoundTag data;
    private final EntityType type;

    private CraftEntitySnapshot(CompoundTag data, EntityType type) {
        this.data = data;
        this.type = type;
    }

    @Override
    public @NotNull Entity createEntity(@NotNull World world) {
        if (!(java.util.Objects.requireNonNull(world, "world") instanceof CraftWorld craftWorld)) {
            throw new IllegalArgumentException("World must be backed by LunarArc CraftWorld");
        }
        net.minecraft.world.entity.Entity entity = net.minecraft.world.entity.EntityType.loadEntityRecursive(
                this.data.copy(), craftWorld.getHandle(), created -> created);
        if (entity == null) {
            throw new IllegalArgumentException("Error creating entity from snapshot");
        }
        return ((EntityBridge) entity).lunararc$getBukkitEntity();
    }

    @Override
    public @NotNull Entity createEntity(@NotNull Location location) {
        java.util.Objects.requireNonNull(location, "location");
        World world = java.util.Objects.requireNonNull(location.getWorld(), "location world");
        Entity entity = this.createEntity(world);
        if (!entity.spawnAt(location, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM)) {
            throw new IllegalStateException("Unable to spawn entity from snapshot");
        }
        return entity;
    }

    @Override
    public @NotNull EntityType getEntityType() {
        return this.type;
    }

    @Override
    public @NotNull String getAsString() {
        return this.data.toString();
    }

    public CompoundTag getData() {
        return this.data.copy();
    }

    public static @Nullable CraftEntitySnapshot create(CraftEntity entity) {
        java.util.Objects.requireNonNull(entity, "entity");
        CompoundTag tag = new CompoundTag();
        if (!((EntityBridge) entity.getHandle()).lunararc$saveAsPassenger(tag)) {
            return null;
        }
        stripIdentityAndPosition(tag);
        return new CraftEntitySnapshot(tag, entity.getType());
    }

    public static @Nullable CraftEntitySnapshot create(CompoundTag tag, EntityType type) {
        if (tag == null || tag.isEmpty() || type == null) {
            return null;
        }
        CompoundTag copy = tag.copy();
        stripIdentityAndPosition(copy);
        return new CraftEntitySnapshot(copy, type);
    }

    private static void stripIdentityAndPosition(CompoundTag tag) {
        tag.remove("UUID");
        tag.remove("UUIDMost");
        tag.remove("UUIDLeast");
        tag.remove("WorldUUIDMost");
        tag.remove("WorldUUIDLeast");
        tag.remove("Pos");
        if (tag.contains("Passengers", 9)) {
            ListTag passengers = tag.getList("Passengers", 10);
            for (int i = 0; i < passengers.size(); i++) {
                stripIdentityAndPosition(passengers.getCompound(i));
            }
        }
    }
}
