package io.lunararcdevs.lunararc.common.bridge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface EntityBridge {
    int lunararc$getPortalCooldown();
    void lunararc$setPortalCooldown(int cooldown);
    int lunararc$getRemainingFireTicks();
    void lunararc$setRemainingFireTicks(int ticks);
    org.bukkit.persistence.PersistentDataContainer lunararc$getPersistentDataContainer();
    org.bukkit.entity.Entity lunararc$getBukkitEntity();
    org.bukkit.entity.Entity lunararc$peekBukkitEntity();
    void lunararc$setBukkitEntity(org.bukkit.entity.Entity entity);
    CreatureSpawnEvent.@Nullable SpawnReason lunararc$getSpawnReason();
    void lunararc$setSpawnReason(CreatureSpawnEvent.@Nullable SpawnReason reason);
    boolean lunararc$fromMobSpawner();
    void lunararc$setFromMobSpawner(boolean fromMobSpawner);
    @Nullable Vec3 lunararc$getOrigin();
    @Nullable UUID lunararc$getOriginWorld();
    void lunararc$setOrigin(@Nullable Vec3 origin, @Nullable UUID worldId);
    boolean lunararc$isInWorld();
    void lunararc$setInWorld(boolean inWorld);
    boolean lunararc$isVisualFire();
    void lunararc$setVisualFire(boolean visualFire);
    boolean lunararc$isPersistent();
    void lunararc$setPersistent(boolean persistent);
    void lunararc$setLevel(Level level);
    boolean lunararc$saveAsPassenger(CompoundTag tag);
}
