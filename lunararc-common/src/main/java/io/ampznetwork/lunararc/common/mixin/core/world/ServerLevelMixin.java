package io.ampznetwork.lunararc.common.mixin.core.world;

import io.ampznetwork.lunararc.common.LunarArcServerAccess;
import io.ampznetwork.lunararc.common.bridge.EntityBridge;
import io.ampznetwork.lunararc.common.bridge.ServerLevelBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements ServerLevelBridge {

    @Shadow
    public abstract boolean addFreshEntity(Entity entity);

    @org.spongepowered.asm.mixin.Unique
    private java.nio.file.Path lunararc$dimensionFolder;

    /**
     * Remembers where this level's data is written, since vanilla does not.
     *
     * <p>CraftWorld needs it to put {@code uid.dat} in the world it identifies. It used to derive a
     * folder from the Bukkit world name instead, resolved against the process working directory,
     * which created empty {@code world_nether} and {@code world_the_end} directories beside a save
     * whose nether and end actually live in {@code world/DIM-1} and {@code world/DIM1} - so the
     * identity of a world did not travel with the world, and deleting the stray directory silently
     * gave it a new UUID.</p>
     *
     * <p>{@code require = 0} because this is a twelve-argument constructor a loader could extend:
     * failing to apply leaves the folder null and CraftWorld falls back to the old path rather than
     * failing to boot.</p>
     */
    @Inject(method = "<init>", at = @At("RETURN"), require = 0)
    private void lunararc$captureDimensionFolder(
            net.minecraft.server.MinecraftServer server,
            java.util.concurrent.Executor dispatcher,
            net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess access,
            net.minecraft.world.level.storage.ServerLevelData levelData,
            net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension,
            net.minecraft.world.level.dimension.LevelStem levelStem,
            net.minecraft.server.level.progress.ChunkProgressListener progressListener,
            boolean isDebug,
            long biomeZoomSeed,
            java.util.List<net.minecraft.world.level.CustomSpawner> customSpawners,
            boolean tickTime,
            net.minecraft.world.RandomSequences randomSequences,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        try {
            this.lunararc$dimensionFolder = access == null ? null : access.getDimensionPath(dimension);
        } catch (Throwable unavailable) {
            this.lunararc$dimensionFolder = null;
        }
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "tickNonPassenger", at = @org.spongepowered.asm.mixin.injection.At("HEAD"), cancellable = true)
    private void lunararc$markerTickConfiguration(net.minecraft.world.entity.Entity entity,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfo callback) {
        if (entity instanceof net.minecraft.world.entity.Marker
                && !io.papermc.paper.configuration.WorldConfiguration.forLevel((net.minecraft.world.level.Level) (Object) this).entities.markers.tick) {
            callback.cancel();
        }
    }

    @Override
    public java.nio.file.Path lunararc$getDimensionFolder() {
        return this.lunararc$dimensionFolder;
    }

    @Override
    public boolean lunararc$addFreshEntity(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        java.util.Objects.requireNonNull(entity, "entity");
        java.util.Objects.requireNonNull(reason, "reason");
        EntityBridge bridge = (EntityBridge) entity;
        if (bridge.lunararc$getSpawnReason() == null) {
            bridge.lunararc$setSpawnReason(reason);
        }
        if (reason == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            bridge.lunararc$setFromMobSpawner(true);
        }
        org.bukkit.event.Cancellable bukkitEvent = lunararc$callSpawnEvent(entity, reason);
        if (bukkitEvent != null) {
            io.ampznetwork.lunararc.common.mod.util.LunarArcEntityJoinCapture.capture(entity, bukkitEvent);
        }
        boolean loaderOwnsCancellation = ((io.ampznetwork.lunararc.common.bridge.MinecraftServerBridge) (Object) ((ServerLevel) (Object) this).getServer())
                .lunararc$loaderHandlesEntityJoinEvent();
        if (bukkitEvent != null && bukkitEvent.isCancelled() && !loaderOwnsCancellation) {
            io.ampznetwork.lunararc.common.mod.util.LunarArcEntityJoinCapture.clear();
            return false;
        }
        try {
            boolean added = this.addFreshEntity(entity);
            if (added) {
                bridge.lunararc$setInWorld(true);
                this.lunararc$ensureOrigin(entity);
            }
            return added;
        } finally {
            io.ampznetwork.lunararc.common.mod.util.LunarArcEntityJoinCapture.clear();
        }
    }

    @Override
    public void lunararc$addFreshEntityWithPassengers(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        if (!this.lunararc$addFreshEntity(entity, reason)) {
            return;
        }
        entity.getIndirectPassengers().forEach(passenger -> this.lunararc$addFreshEntity(passenger, reason));
    }

    public boolean addFreshEntity(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        return this.lunararc$addFreshEntity(entity, reason);
    }

    public void addFreshEntityWithPassengers(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        this.lunararc$addFreshEntityWithPassengers(entity, reason);
    }


    @Inject(method = "addFreshEntity", at = @At("HEAD"), cancellable = true, require = 0)
    private void lunararc$nativeFreshEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof ServerPlayer) return;
        // addFreshEntity is the core method structure population (dungeons, villages,
        // outposts — anything that spawns entities) uses to add entities to the world, and
        // that generation genuinely happens on worker threads even in vanilla. Same class of
        // risk as a real, confirmed crash in LivingEntity.addEffect() during structure
        // population, but this method is even more centrally on that path. Skip firing the
        // Bukkit spawn event off-thread rather than let PaperEventManager's safety check throw
        // and abort the underlying vanilla/modded entity placement.
        if (!org.bukkit.Bukkit.isPrimaryThread()) return;
        ServerLevel level = (ServerLevel) (Object) this;
        boolean loaderOwnsCancellation = ((io.ampznetwork.lunararc.common.bridge.MinecraftServerBridge) (Object) level.getServer())
                .lunararc$loaderHandlesEntityJoinEvent();
        if (loaderOwnsCancellation) {
            // Forge/NeoForge deliver their own cancellable EntityJoinLevelEvent. Their adapters
            // translate that loader event to the same Bukkit event/capture instead.
            return;
        }

        org.bukkit.event.Cancellable captured =
                io.ampznetwork.lunararc.common.mod.util.LunarArcEntityJoinCapture.matching(entity);
        if (captured != null) {
            if (captured.isCancelled()) cir.setReturnValue(false);
            return;
        }

        EntityBridge bridge = (EntityBridge) entity;
        CreatureSpawnEvent.SpawnReason reason = bridge.lunararc$getSpawnReason();
        if (reason == null) reason = CreatureSpawnEvent.SpawnReason.DEFAULT;
        org.bukkit.event.Cancellable event = lunararc$callSpawnEvent(entity, reason);
        if (event != null && event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "addEntity", at = @At("RETURN"), require = 0)
    private void lunararc$afterNativeEntityAdd(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (Boolean.TRUE.equals(cir.getReturnValue())) {
            ((EntityBridge) entity).lunararc$setInWorld(true);
            this.lunararc$ensureOrigin(entity);
        }
    }

    private void lunararc$ensureOrigin(Entity entity) {
        EntityBridge bridge = (EntityBridge) entity;
        if (bridge.lunararc$getOrigin() != null) {
            return;
        }
        ServerLevel level = (ServerLevel) (Object) this;
        org.bukkit.craftbukkit.CraftWorld craftWorld =
                LunarArcServerAccess.getCraftServer(level.getServer()).getCraftWorldIfPresent(level);
        if (craftWorld == null) {
            // Synthetic/mod-owned worlds (for example Create contraption worlds) do not
            // have a Bukkit world identity. Preserve the mod operation without inventing one.
            return;
        }
        bridge.lunararc$setOrigin(new Vec3(entity.getX(), entity.getY(), entity.getZ()), craftWorld.getUID());
    }

    private static org.bukkit.event.Cancellable lunararc$callSpawnEvent(Entity entity, CreatureSpawnEvent.SpawnReason reason) {
        if (entity instanceof ServerPlayer) return null;
        if (!(entity.level() instanceof ServerLevel level)) return null;

        org.bukkit.craftbukkit.CraftServer craftServer = LunarArcServerAccess.getCraftServer(level.getServer());
        if (craftServer.getCraftWorldIfPresent(level) == null) {
            // Do not force Bukkit event translation onto synthetic/mod-owned worlds.
            return null;
        }

        if (entity instanceof net.minecraft.world.entity.projectile.Projectile projectile) {
            Object bukkit = ((EntityBridge) projectile).lunararc$getBukkitEntity();
            if (bukkit instanceof org.bukkit.entity.Projectile bukkitProjectile) {
                org.bukkit.event.entity.ProjectileLaunchEvent event =
                        new org.bukkit.event.entity.ProjectileLaunchEvent(bukkitProjectile);
                craftServer.getPluginManager().callEvent(event);
                return event;
            }
        }
        if (entity instanceof LivingEntity livingEntity) {
            return org.bukkit.craftbukkit.event.CraftEventFactory.callCreatureSpawnEvent(livingEntity, reason);
        }
        return org.bukkit.craftbukkit.event.CraftEventFactory.callEntitySpawnEvent(entity);
    }
}
