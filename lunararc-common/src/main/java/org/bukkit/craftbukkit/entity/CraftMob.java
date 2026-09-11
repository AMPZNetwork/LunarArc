package org.bukkit.craftbukkit.entity;

import io.lunararcdevs.lunararc.common.bridge.MobBridge;
import java.util.Objects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.storage.loot.LootTable;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.CraftLootTable;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CraftMob extends CraftLivingEntity implements Mob {
    private final com.destroystokyo.paper.entity.PaperPathfinder paperPathfinder;
    public CraftMob(CraftServer server, net.minecraft.world.entity.Mob entity) {
        super(server, entity);
        this.paperPathfinder = new com.destroystokyo.paper.entity.PaperPathfinder(entity);
    }

    @Override
    public @NotNull com.destroystokyo.paper.entity.Pathfinder getPathfinder() {
        return this.paperPathfinder;
    }

    @Override
    public net.minecraft.world.entity.Mob getHandle() {
        return (net.minecraft.world.entity.Mob) this.entity;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        ((MobBridge) getHandle()).lunararc$pushTargetReason(org.bukkit.event.entity.EntityTargetEvent.TargetReason.CUSTOM);
        if (target == null) {
            getHandle().setTarget(null);
            return;
        }
        if (!(target instanceof CraftLivingEntity craftTarget)) {
            throw new IllegalArgumentException("Target must be backed by LunarArc");
        }
        if (craftTarget.getWorld() != getWorld()) {
            throw new IllegalArgumentException("Target must be in the same world");
        }
        getHandle().setTarget(craftTarget.getHandle());
    }

    @Override
    public @Nullable LivingEntity getTarget() {
        net.minecraft.world.entity.LivingEntity target = getHandle().getTarget();
        if (target == null) return null;
        org.bukkit.entity.Entity bukkit = CraftEntity.getEntity(server, target);
        return bukkit instanceof LivingEntity living ? living : null;
    }

    @Override public void setAware(boolean aware) { ((MobBridge) getHandle()).lunararc$setAware(aware); }
    @Override public boolean isAware() { return ((MobBridge) getHandle()).lunararc$isAware(); }

    @Override
    public @Nullable Sound getAmbientSound() {
        SoundEvent event = ((io.lunararcdevs.lunararc.common.bridge.access.MobAccessBridge) getHandle()).lunararc$invokeGetAmbientSound();
        if (event == null) return null;
        ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.getKey(event);
        if (id == null) return null;
        return org.bukkit.Registry.SOUNDS.get(new NamespacedKey(id.getNamespace(), id.getPath()));
    }

    @Override public boolean isInDaylight() { return getHandle().isSunBurnTick(); }

    @Override public void lookAt(@NotNull Location location) { lookAt(location, getHeadRotationSpeed(), getMaxHeadPitch()); }
    @Override public void lookAt(@NotNull Location location, float headRotationSpeed, float maxHeadPitch) {
        Objects.requireNonNull(location, "location");
        if (location.getWorld() != getWorld()) throw new IllegalArgumentException("Location must be in the same world");
        getHandle().getLookControl().setLookAt(location.getX(), location.getY(), location.getZ(), headRotationSpeed, maxHeadPitch);
    }
    @Override public void lookAt(@NotNull org.bukkit.entity.Entity entity) { lookAt(entity, getHeadRotationSpeed(), getMaxHeadPitch()); }
    @Override public void lookAt(@NotNull org.bukkit.entity.Entity target, float headRotationSpeed, float maxHeadPitch) {
        Objects.requireNonNull(target, "target");
        if (!(target instanceof CraftEntity craftTarget)) throw new IllegalArgumentException("Target must be backed by LunarArc");
        if (target.getWorld() != getWorld()) throw new IllegalArgumentException("Target must be in the same world");
        getHandle().getLookControl().setLookAt(craftTarget.getHandle(), headRotationSpeed, maxHeadPitch);
    }
    @Override public void lookAt(double x, double y, double z) { getHandle().getLookControl().setLookAt(x, y, z); }
    @Override public void lookAt(double x, double y, double z, float speed, float maxPitch) { getHandle().getLookControl().setLookAt(x, y, z, speed, maxPitch); }
    @Override public int getHeadRotationSpeed() { return getHandle().getHeadRotSpeed(); }
    @Override public int getMaxHeadPitch() { return getHandle().getMaxHeadXRot(); }

    @Override public boolean isAggressive() { return getHandle().isAggressive(); }
    @Override public void setAggressive(boolean aggressive) { getHandle().setAggressive(aggressive); }
    @Override public boolean isLeftHanded() { return getHandle().isLeftHanded(); }
    @Override public void setLeftHanded(boolean leftHanded) { getHandle().setLeftHanded(leftHanded); }

    @Override
    public void setLootTable(@Nullable org.bukkit.loot.LootTable table) {
        getHandle().lootTable = table == null ? null : CraftLootTable.bukkitKeyToMinecraft(table.getKey());
    }

    @Override
    public @Nullable org.bukkit.loot.LootTable getLootTable() {
        ResourceKey<LootTable> key = getHandle().lootTable;
        if (key == null) return null;
        ResourceLocation id = key.location();
        return org.bukkit.Bukkit.getLootTable(new NamespacedKey(id.getNamespace(), id.getPath()));
    }

    @Override public void setSeed(long seed) { getHandle().lootTableSeed = seed; }
    @Override public long getSeed() { return getHandle().getLootTableSeed(); }

    @Override public boolean isLeashed() { return getHandle().getLeashHolder() != null; }
    @Override public @NotNull org.bukkit.entity.Entity getLeashHolder() throws IllegalStateException {
        net.minecraft.world.entity.Entity holder = getHandle().getLeashHolder();
        if (holder == null) throw new IllegalStateException("Entity not leashed");
        return CraftEntity.getEntity(server, holder);
    }
    @Override public boolean setLeashHolder(@Nullable org.bukkit.entity.Entity holder) {
        if (holder == null) {
            if (!isLeashed()) return false;
            getHandle().dropLeash(true, false);
            return true;
        }
        if (!(holder instanceof CraftEntity craft)) throw new IllegalArgumentException("Leash holder must be backed by LunarArc");
        if (holder.isDead() || holder.getWorld() != getWorld()) return false;
        if (isLeashed()) getHandle().dropLeash(true, false);
        getHandle().setLeashedTo(craft.getHandle(), true);
        return true;
    }

    @Override
    public int getPossibleExperienceReward() {
        if (!(getHandle().level() instanceof ServerLevel level)) return 0;
        return getHandle().getExperienceReward(level, null);
    }

    @Override public String toString() { return "CraftMob{" + getEntityId() + ",type=" + getType() + '}'; }
}
