package org.bukkit.craftbukkit.v1_21_R1.entity;

import net.minecraft.world.entity.Entity;
import io.ampznetwork.lunararc.common.bridge.EntityBridge;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.bukkit.craftbukkit.v1_21_R1.CraftServer;
import org.bukkit.craftbukkit.v1_21_R1.persistence.CraftPersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Base implementation of Bukkit Entity for LunarArc.
 */
public abstract class CraftEntity implements org.bukkit.entity.Entity {
    protected final CraftServer server;
    protected final Entity entity;

    public CraftEntity(CraftServer server, Entity entity) {
        this.server = server;
        this.entity = entity;
        this.bridge().lunararc$setBukkitEntity(this);
    }

    public static org.bukkit.entity.Entity getEntity(CraftServer server, Entity entity) {
        if (entity == null)
            return null;

        if (entity instanceof net.minecraft.server.level.ServerPlayer sp) {
            org.bukkit.entity.Player player = server.getPlayer(sp.getUUID());
            return player != null ? player : new CraftPlayer(server, sp);
        }

        return new CraftUnknownEntity(server, entity);
    }

    protected EntityBridge bridge() {
        return (EntityBridge) entity;
    }

    // --- Identity and Basic Info ---

    @Override
    public @NotNull UUID getUniqueId() {
        return entity.getUUID();
    }

    @Override
    public int getEntityId() {
        return entity.getId();
    }

    @Override
    public @NotNull String getName() {
        return entity.getName().getString();
    }

    @Override
    public @NotNull Server getServer() {
        return server;
    }

    @Override
    public @NotNull org.bukkit.entity.EntityType getType() {
        return org.bukkit.entity.EntityType.UNKNOWN;
    }

    @Override
    public @NotNull String getAsString() {
        return getName();
    }

    // --- Lifecycle ---

    @Override
    public void remove() {
        entity.discard();
    }

    @Override
    public boolean isDead() {
        return !entity.isAlive();
    }

    @Override
    public boolean isValid() {
        return entity.isAlive() && entity.level() != null;
    }

    // --- Location and Movement ---

    @Override
    public @NotNull World getWorld() {
        return server.getWorld(entity.level().dimension().location().toString());
    }

    @Override
    public @NotNull Location getLocation() {
        return new Location(getWorld(), entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(),
                entity.getXRot());
    }

    @Override
    public @Nullable Location getLocation(@Nullable Location loc) {
        if (loc != null) {
            loc.setWorld(getWorld());
            loc.setX(entity.getX());
            loc.setY(entity.getY());
            loc.setZ(entity.getZ());
            loc.setYaw(entity.getYRot());
            loc.setPitch(entity.getXRot());
        }
        return loc;
    }

    @Override
    public void setVelocity(@NotNull Vector velocity) {
        entity.setDeltaMovement(velocity.getX(), velocity.getY(), velocity.getZ());
        entity.hasImpulse = true;
    }

    @Override
    public @NotNull Vector getVelocity() {
        net.minecraft.world.phys.Vec3 vec = entity.getDeltaMovement();
        return new Vector(vec.x, vec.y, vec.z);
    }

    @Override
    public double getHeight() {
        return entity.getBbHeight();
    }

    @Override
    public double getWidth() {
        return entity.getBbWidth();
    }

    @Override
    public @NotNull BoundingBox getBoundingBox() {
        net.minecraft.world.phys.AABB bb = entity.getBoundingBox();
        return new BoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
    }

    @Override
    public boolean isOnGround() {
        return entity.onGround();
    }

    @Override
    public void setRotation(float yaw, float pitch) {
        entity.setYRot(yaw);
        entity.setXRot(pitch);
    }

    // --- Teleportation ---

    @Override
    public boolean teleport(@NotNull Location location) {
        return teleport(location, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    @Override
    public boolean teleport(@NotNull Location location,
            @NotNull org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause) {
        if (entity.isVehicle() || entity.isRemoved())
            return false;
        entity.teleportTo(((org.bukkit.craftbukkit.v1_21_R1.CraftWorld) location.getWorld()).getHandle(),
                location.getX(), location.getY(), location.getZ(), Collections.emptySet(), location.getYaw(),
                location.getPitch());
        return true;
    }

    @Override
    public boolean teleport(@NotNull Location location,
            @NotNull org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause,
            @NotNull io.papermc.paper.entity.TeleportFlag... flags) {
        return teleport(location, cause);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> teleportAsync(@NotNull Location location,
            @NotNull org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause,
            @NotNull io.papermc.paper.entity.TeleportFlag... flags) {
        return CompletableFuture.completedFuture(teleport(location, cause));
    }

    @Override
    public boolean teleport(@NotNull org.bukkit.entity.Entity destination) {
        return teleport(destination.getLocation());
    }

    @Override
    public boolean teleport(@NotNull org.bukkit.entity.Entity destination,
            @NotNull org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause) {
        return teleport(destination.getLocation(), cause);
    }

    // --- State and Flags ---

    @Override
    public boolean isPersistent() {
        return true;
    }

    @Override
    public void setPersistent(boolean persistent) {
    }

    @Override
    public void setCustomName(@Nullable String name) {
        entity.setCustomName(name != null ? net.minecraft.network.chat.Component.literal(name) : null);
    }

    @Override
    public @Nullable String getCustomName() {
        return entity.getCustomName() != null ? entity.getCustomName().getString() : null;
    }

    @Override
    public void setCustomNameVisible(boolean flag) {
        entity.setCustomNameVisible(flag);
    }

    @Override
    public boolean isCustomNameVisible() {
        return entity.isCustomNameVisible();
    }

    @Override
    public void setGlowing(boolean flag) {
        entity.setGlowingTag(flag);
    }

    @Override
    public boolean isGlowing() {
        return entity.hasGlowingTag();
    }

    @Override
    public void setInvulnerable(boolean flag) {
        entity.setInvulnerable(flag);
    }

    @Override
    public boolean isInvulnerable() {
        return entity.isInvulnerable();
    }

    @Override
    public boolean isSilent() {
        return entity.isSilent();
    }

    @Override
    public void setSilent(boolean flag) {
        entity.setSilent(flag);
    }

    @Override
    public boolean hasGravity() {
        return !entity.isNoGravity();
    }

    @Override
    public void setGravity(boolean gravity) {
        entity.setNoGravity(!gravity);
    }

    @Override
    public int getPortalCooldown() {
        return bridge().lunararc$getPortalCooldown();
    }

    @Override
    public void setPortalCooldown(int cooldown) {
        bridge().lunararc$setPortalCooldown(cooldown);
    }

    // --- Metadata and Persistence ---

    @Override
    public @NotNull org.bukkit.persistence.PersistentDataContainer getPersistentDataContainer() {
        return bridge().lunararc$getPersistentDataContainer();
    }

    @Override
    public void setMetadata(@NotNull String metadataKey, @NotNull org.bukkit.metadata.MetadataValue newMetadataValue) {
        server.getEntityMetadata().setMetadata(this, metadataKey, newMetadataValue);
    }

    @Override
    public @NotNull List<org.bukkit.metadata.MetadataValue> getMetadata(@NotNull String metadataKey) {
        return server.getEntityMetadata().getMetadata(this, metadataKey);
    }

    @Override
    public boolean hasMetadata(@NotNull String metadataKey) {
        return server.getEntityMetadata().hasMetadata(this, metadataKey);
    }

    @Override
    public void removeMetadata(@NotNull String metadataKey, @NotNull org.bukkit.plugin.Plugin owningPlugin) {
        server.getEntityMetadata().removeMetadata(this, metadataKey, owningPlugin);
    }

    // --- Scoreboard ---

    @Override
    public @NotNull Set<String> getScoreboardTags() {
        return entity.getTags();
    }

    @Override
    public boolean addScoreboardTag(@NotNull String tag) {
        return entity.addTag(tag);
    }

    @Override
    public boolean removeScoreboardTag(@NotNull String tag) {
        return entity.removeTag(tag);
    }

    // --- Other methods (Stubs) ---

    @Override
    public @Nullable org.bukkit.entity.Entity getPassenger() {
        return getPassengers().isEmpty() ? null : getPassengers().get(0);
    }

    @Override
    public boolean setPassenger(@NotNull org.bukkit.entity.Entity passenger) {
        return addPassenger(passenger);
    }

    @Override
    public @NotNull List<org.bukkit.entity.Entity> getPassengers() {
        return Collections.emptyList();
    }

    @Override
    public boolean addPassenger(@NotNull org.bukkit.entity.Entity passenger) {
        return false;
    }

    @Override
    public boolean removePassenger(@NotNull org.bukkit.entity.Entity passenger) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return getPassengers().isEmpty();
    }

    @Override
    public boolean eject() {
        return false;
    }

    @Override
    public float getFallDistance() {
        return entity.fallDistance;
    }

    @Override
    public void setFallDistance(float distance) {
        entity.fallDistance = distance;
    }

    @Override
    public void setLastDamageCause(@Nullable org.bukkit.event.entity.EntityDamageEvent cause) {
    }

    @Override
    public @Nullable org.bukkit.event.entity.EntityDamageEvent getLastDamageCause() {
        return null;
    }

    @Override
    public boolean isTicking() {
        return true; // Simplified for LunarArc
    }

    @Override public boolean isInLava() { return entity.isInLava(); }
    @Override public boolean isInWater() { return entity.isInWater(); }
    @Override public boolean isInWaterOrRain() { return entity.isInWaterRainOrBubble(); }
    @Override public boolean isInBubbleColumn() { 
        net.minecraft.world.level.Level level = entity.level();
        return level != null && level.getBlockState(entity.blockPosition()).is(net.minecraft.world.level.block.Blocks.BUBBLE_COLUMN); 
    }
    @Override public boolean isUnderWater() { return entity.isEyeInFluid(net.minecraft.tags.FluidTags.WATER); }
    @Override public boolean isInRain() { 
        net.minecraft.world.level.Level level = entity.level();
        return level != null && level.isRainingAt(entity.blockPosition()); 
    }
    @Override public boolean isInWaterOrBubbleColumn() { return entity.isInWaterOrBubble(); }
    @Override public boolean isInWaterOrRainOrBubbleColumn() { return entity.isInWaterRainOrBubble(); }

    @Override
    public @NotNull Set<org.bukkit.entity.Player> getTrackedPlayers() {
        return Collections.emptySet();
    }

    @Override public @NotNull org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason getEntitySpawnReason() { return org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.DEFAULT; }
    @Override public boolean fromMobSpawner() { return false; }
    @Override public @Nullable Location getOrigin() { return null; }
    @Override public @NotNull net.kyori.adventure.text.Component teamDisplayName() { return net.kyori.adventure.text.Component.text(getName()); }
    @Override public @NotNull org.bukkit.entity.Entity copy(@NotNull Location to) { return this; }
    @Override public @NotNull org.bukkit.entity.Entity copy() { return this; }
    @Override public @Nullable org.bukkit.entity.EntitySnapshot createSnapshot() { return null; }
    @Override public boolean isInWorld() { return entity.level() != null; }
    @Override public @NotNull org.bukkit.entity.SpawnCategory getSpawnCategory() { return org.bukkit.entity.SpawnCategory.MISC; }
    @Override public boolean hasFixedPose() { return false; }
    @Override public void setPose(@NotNull org.bukkit.entity.Pose pose, boolean fixed) {}
    @Override public void setSneaking(boolean sneak) {}
    @Override public boolean isSneaking() { return false; }
    @Override public @NotNull Set<org.bukkit.entity.Player> getTrackedBy() { return Collections.emptySet(); }
    @Override public boolean isVisibleByDefault() { return true; }
    @Override public void setVisibleByDefault(boolean visible) {}
    @Override public @NotNull org.bukkit.Sound getSwimHighSpeedSplashSound() { return org.bukkit.Sound.ENTITY_GENERIC_SPLASH; }
    @Override public @NotNull org.bukkit.Sound getSwimSplashSound() { return org.bukkit.Sound.ENTITY_GENERIC_SPLASH; }
    @Override public @NotNull org.bukkit.Sound getSwimSound() { return org.bukkit.Sound.ENTITY_GENERIC_SWIM; }
    @Override public void lockFreezeTicks(boolean lock) {}
    @Override public boolean isFreezeTickingLocked() { return false; }
    @Override public boolean hasNoPhysics() { return entity.noPhysics; }
    @Override public void setNoPhysics(boolean noPhysics) { entity.noPhysics = noPhysics; }
    @Override public boolean isInvisible() { return entity.isInvisible(); }
    @Override public void setInvisible(boolean invisible) { entity.setInvisible(invisible); }
    @Override public boolean isFrozen() { return entity.isFullyFrozen(); }
    @Override public void setFreezeTicks(int ticks) { entity.setTicksFrozen(ticks); }
    @Override public int getMaxFreezeTicks() { return entity.getTicksRequiredToFreeze(); }
    @Override public int getFreezeTicks() { return entity.getTicksFrozen(); }
    @Override public boolean isVisualFire() { return entity.isOnFire(); }
    @Override public void setVisualFire(boolean fire) {}
    @Override public void setFireTicks(int ticks) { ((EntityBridge) entity).lunararc$setRemainingFireTicks(ticks); }
    @Override public int getMaxFireTicks() { return 1000; }
    @Override public int getFireTicks() { return ((EntityBridge) entity).lunararc$getRemainingFireTicks(); }
    @Override public @NotNull List<org.bukkit.entity.Entity> getNearbyEntities(double x, double y, double z) { return Collections.emptyList(); }
    @Override public void sendMessage(@Nullable UUID sender, @NotNull String... message) {}
    @Override public void sendMessage(@Nullable UUID sender, @NotNull String message) {}
    @Override public void sendMessage(@NotNull String... message) {}
    @Override public void sendMessage(@NotNull String message) {}
    @Override public void customName(net.kyori.adventure.text.Component customName) { entity.setCustomName(customName != null ? net.minecraft.network.chat.Component.literal(net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(customName)) : null); }
    @Override public net.kyori.adventure.text.Component customName() { return entity.getCustomName() != null ? net.kyori.adventure.text.Component.text(entity.getCustomName().getString()) : null; }

    @Override
    public int getTicksLived() {
        return entity.tickCount;
    }

    @Override
    public void setTicksLived(int value) {
        entity.tickCount = value;
    }

    @Override
    public void playEffect(@NotNull org.bukkit.EntityEffect type) {
    }

    @Override
    public boolean isInsideVehicle() {
        return entity.isPassenger();
    }

    @Override
    public boolean leaveVehicle() {
        entity.stopRiding();
        return true;
    }

    @Override
    public @Nullable org.bukkit.entity.Entity getVehicle() {
        return entity.getVehicle() != null ? getEntity(server, entity.getVehicle()) : null;
    }

    @Override
    public @NotNull org.bukkit.block.PistonMoveReaction getPistonMoveReaction() {
        return org.bukkit.block.PistonMoveReaction.MOVE;
    }

    @Override
    public @NotNull org.bukkit.block.BlockFace getFacing() {
        return org.bukkit.block.BlockFace.SOUTH;
    }

    @Override
    public @NotNull org.bukkit.entity.Pose getPose() {
        return org.bukkit.entity.Pose.STANDING;
    }

    @Override
    public boolean isPermissionSet(@NotNull String name) {
        return false;
    }

    @Override
    public boolean isPermissionSet(@NotNull org.bukkit.permissions.Permission perm) {
        return false;
    }

    @Override
    public boolean hasPermission(@NotNull String name) {
        return false;
    }

    @Override
    public boolean hasPermission(@NotNull org.bukkit.permissions.Permission perm) {
        return false;
    }

    @Override
    public @NotNull org.bukkit.permissions.PermissionAttachment addAttachment(@NotNull org.bukkit.plugin.Plugin plugin,
            @NotNull String name, boolean value) {
        return null;
    }

    @Override
    public @NotNull org.bukkit.permissions.PermissionAttachment addAttachment(
            @NotNull org.bukkit.plugin.Plugin plugin) {
        return null;
    }

    @Override
    public @Nullable org.bukkit.permissions.PermissionAttachment addAttachment(@NotNull org.bukkit.plugin.Plugin plugin,
            @NotNull String name, boolean value, int ticks) {
        return null;
    }

    @Override
    public @Nullable org.bukkit.permissions.PermissionAttachment addAttachment(@NotNull org.bukkit.plugin.Plugin plugin,
            int ticks) {
        return null;
    }

    @Override
    public void removeAttachment(@NotNull org.bukkit.permissions.PermissionAttachment attachment) {
    }

    @Override
    public void recalculatePermissions() {
    }

    @Override
    public @NotNull Set<org.bukkit.permissions.PermissionAttachmentInfo> getEffectivePermissions() {
        return Collections.emptySet();
    }

    @Override
    public boolean isOp() {
        return false;
    }

    @Override
    public void setOp(boolean value) {
    }

    // Paper specific
    @Override
    public void broadcastHurtAnimation(@NotNull Collection<org.bukkit.entity.Player> players) {
    }

    @Override
    public @NotNull String getScoreboardEntryName() {
        return getUniqueId().toString();
    }

    @Override
    public @NotNull io.papermc.paper.threadedregions.scheduler.EntityScheduler getScheduler() {
        return null;
    }

    @Override
    public boolean wouldCollideUsing(@NotNull BoundingBox boundingBox) {
        return false;
    }

    @Override
    public boolean collidesAt(@NotNull Location location) {
        return false;
    }

    @Override
    public float getYaw() {
        return entity.getYRot();
    }

    @Override
    public float getPitch() {
        return entity.getXRot();
    }

    @Override
    public double getX() {
        return entity.getX();
    }

    @Override
    public double getY() {
        return entity.getY();
    }

    @Override
    public double getZ() {
        return entity.getZ();
    }

    @Override
    public boolean isInPowderedSnow() {
        return entity.isInPowderSnow;
    }

    @Override
    public boolean spawnAt(@NotNull Location location,
            @NotNull org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason) {
        return false;
    }

    /**
     * Inner class for unknown/generic entities.
     */
    private static class CraftUnknownEntity extends CraftEntity {
        public CraftUnknownEntity(CraftServer server, Entity entity) {
            super(server, entity);
        }

        @Override
        public @NotNull net.kyori.adventure.text.Component name() {
            return net.kyori.adventure.text.Component.text(getName());
        }

        @Override
        public void setLastDamageCause(@Nullable org.bukkit.event.entity.EntityDamageEvent cause) {
        }

        @Override
        public @Nullable org.bukkit.event.entity.EntityDamageEvent getLastDamageCause() {
            return null;
        }

        @Override
        public @NotNull Set<org.bukkit.entity.Player> getTrackedPlayers() {
            return Collections.emptySet();
        }




        @Override
        public org.bukkit.entity.Entity.Spigot spigot() {
            return new org.bukkit.entity.Entity.Spigot();
        }


    }
}
