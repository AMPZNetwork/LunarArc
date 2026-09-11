package org.bukkit.craftbukkit.entity;

import net.minecraft.world.entity.Entity;
import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import io.lunararcdevs.lunararc.common.bridge.ServerLevelBridge;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public abstract class CraftEntity implements org.bukkit.entity.Entity {
    protected final CraftServer server;
    protected final Entity entity;
    private org.bukkit.permissions.PermissibleBase permissible;
    private @Nullable org.bukkit.event.entity.EntityDamageEvent lastDamageCause;
    private boolean visibleByDefault = true;
    private boolean freezeTicksLocked;
    private boolean fixedPose;
    private final org.bukkit.entity.Entity.Spigot spigot = new org.bukkit.entity.Entity.Spigot();

    public CraftEntity(CraftServer server, Entity entity) {
        this.server = server;
        this.entity = entity;
        this.permissible = new org.bukkit.permissions.PermissibleBase(this);
        this.bridge().lunararc$setBukkitEntity(this);
    }

    public static org.bukkit.entity.Entity getEntity(CraftServer server, Entity entity) {
        if (entity == null) return null;
        if (entity instanceof EntityBridge bridge) {
            org.bukkit.entity.Entity existing = bridge.lunararc$peekBukkitEntity();
            if (existing != null) return existing;
        }
        if (entity instanceof net.minecraft.server.level.ServerPlayer sp) {
            return new CraftPlayer(server, sp);
        }
        if (entity instanceof net.minecraft.world.entity.item.ItemEntity item) {
            return new CraftItem(server, item);
        }
        if (entity instanceof net.minecraft.world.entity.LightningBolt lightning) {
            return new CraftLightningStrike(server, lightning);
        }
        if (entity instanceof net.minecraft.world.entity.projectile.ThrownPotion potion) {
            return new CraftThrownPotion(server, potion);
        }
        if (entity instanceof net.minecraft.world.entity.projectile.ThrownExperienceBottle bottle) {
            return new CraftThrownExpBottle(server, bottle);
        }
        if (entity instanceof net.minecraft.world.entity.projectile.Snowball snowball) {
            return new CraftSnowball(server, snowball);
        }
        if (entity instanceof net.minecraft.world.entity.projectile.ThrownEgg egg) {
            return new CraftEgg(server, egg);
        }
        if (entity instanceof net.minecraft.world.entity.projectile.ThrownEnderpearl pearl) {
            return new CraftEnderPearl(server, pearl);
        }
        if (entity instanceof net.minecraft.world.entity.projectile.FireworkRocketEntity firework) {
            return new CraftFirework(server, firework);
        }
        if (entity instanceof net.minecraft.world.entity.projectile.FishingHook hook) {
            return new CraftFishHook(server, hook);
        }
        if (entity instanceof net.minecraft.world.entity.projectile.ShulkerBullet bullet) {
            return new CraftShulkerBullet(server, bullet);
        }
        if (entity instanceof net.minecraft.world.entity.projectile.LlamaSpit spit) {
            return new CraftLlamaSpit(server, spit);
        }
        if (entity instanceof net.minecraft.world.entity.projectile.windcharge.BreezeWindCharge charge) {
            return new CraftBreezeWindCharge(server, charge);
        }
        if (entity instanceof net.minecraft.world.entity.projectile.windcharge.WindCharge charge) {
            return new CraftWindCharge(server, charge);
        }
        if (entity instanceof net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge charge) {
            return new CraftAbstractWindCharge(server, charge);
        }
        if (entity instanceof net.minecraft.world.entity.projectile.LargeFireball fireball) {
            return new CraftLargeFireball(server, fireball);
        }
        if (entity instanceof net.minecraft.world.entity.projectile.SmallFireball fireball) {
            return new CraftSmallFireball(server, fireball);
        }
        if (entity instanceof net.minecraft.world.entity.projectile.WitherSkull skull) {
            return new CraftWitherSkull(server, skull);
        }
        if (entity instanceof net.minecraft.world.entity.projectile.DragonFireball fireball) {
            return new CraftDragonFireball(server, fireball);
        }
        if (entity instanceof net.minecraft.world.entity.projectile.AbstractHurtingProjectile fireball) {
            return new CraftFireball(server, fireball);
        }
        if (entity instanceof net.minecraft.world.entity.projectile.SpectralArrow spectralArrow) {
            return new CraftSpectralArrow(server, spectralArrow);
        }
        if (entity instanceof net.minecraft.world.entity.projectile.ThrownTrident trident) {
            return new CraftTrident(server, trident);
        }
        if (entity instanceof net.minecraft.world.entity.projectile.Arrow arrow) {
            return new CraftArrow(server, arrow);
        }
        if (entity instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow) {
            return new CraftAbstractArrow(server, arrow);
        }
        if (entity instanceof net.minecraft.world.entity.item.FallingBlockEntity fallingBlock) {
            return new CraftFallingBlock(server, fallingBlock);
        }
        if (entity instanceof net.minecraft.world.entity.monster.MagmaCube magmaCube) {
            return new CraftMagmaCube(server, magmaCube);
        }
        if (entity instanceof net.minecraft.world.entity.monster.Slime slime) {
            return new CraftSlime(server, slime);
        }
        if (entity instanceof net.minecraft.world.entity.monster.Creeper creeper) {
            return new CraftCreeper(server, creeper);
        }
        if (entity instanceof net.minecraft.world.entity.monster.Zombie zombie) {
            return new CraftZombie(server, zombie);
        }
        if (entity instanceof net.minecraft.world.entity.monster.EnderMan enderman) {
            return new CraftEnderman(server, enderman);
        }
        if (entity instanceof net.minecraft.world.entity.monster.Skeleton skeleton) {
            return new CraftSkeleton(server, skeleton);
        }
        if (entity instanceof net.minecraft.world.entity.monster.Stray stray) {
            return new CraftStray(server, stray);
        }
        if (entity instanceof net.minecraft.world.entity.monster.WitherSkeleton witherSkeleton) {
            return new CraftWitherSkeleton(server, witherSkeleton);
        }
        if (entity instanceof net.minecraft.world.entity.monster.Spider spider) {
            return new CraftSpider(server, spider);
        }
        if (entity instanceof net.minecraft.world.entity.monster.Monster monster) {
            return new CraftMonster(server, monster);
        }
        if (entity instanceof net.minecraft.world.entity.npc.Villager villager) {
            return new CraftVillager(server, villager);
        }
        if (entity instanceof net.minecraft.world.entity.npc.AbstractVillager villager) {
            return new CraftAbstractVillager(server, villager);
        }
        if (entity instanceof net.minecraft.world.entity.animal.horse.Horse horse) {
            return new CraftHorse(server, horse);
        }
        if (entity instanceof net.minecraft.world.entity.animal.horse.Donkey donkey) {
            return new CraftDonkey(server, donkey);
        }
        if (entity instanceof net.minecraft.world.entity.animal.horse.Mule mule) {
            return new CraftMule(server, mule);
        }
        if (entity instanceof net.minecraft.world.entity.animal.horse.SkeletonHorse skeletonHorse) {
            return new CraftSkeletonHorse(server, skeletonHorse);
        }
        if (entity instanceof net.minecraft.world.entity.animal.horse.ZombieHorse zombieHorse) {
            return new CraftZombieHorse(server, zombieHorse);
        }
        if (entity instanceof net.minecraft.world.entity.animal.AbstractGolem golem) {
            return new CraftGolem(server, golem);
        }
        if (entity instanceof net.minecraft.world.entity.animal.WaterAnimal waterAnimal) {
            return new CraftWaterMob(server, waterAnimal);
        }
        if (entity instanceof net.minecraft.world.entity.animal.allay.Allay allay) {
            return new CraftAllay(server, allay);
        }
        if (entity instanceof net.minecraft.world.entity.animal.goat.Goat goat) {
            return new CraftGoat(server, goat);
        }
        if (entity instanceof net.minecraft.world.entity.animal.frog.Frog frog) {
            return new CraftFrog(server, frog);
        }
        if (entity instanceof net.minecraft.world.entity.animal.Bee bee) {
            return new CraftBee(server, bee);
        }
        if (entity instanceof net.minecraft.world.entity.animal.Cow cow) {
            return new CraftCow(server, cow);
        }
        if (entity instanceof net.minecraft.world.entity.animal.Sheep sheep) {
            return new CraftSheep(server, sheep);
        }
        if (entity instanceof net.minecraft.world.entity.animal.Chicken chicken) {
            return new CraftChicken(server, chicken);
        }
        if (entity instanceof net.minecraft.world.entity.animal.Wolf wolf) {
            return new CraftWolf(server, wolf);
        }
        if (entity instanceof net.minecraft.world.entity.animal.Cat cat) {
            return new CraftCat(server, cat);
        }
        if (entity instanceof net.minecraft.world.entity.animal.Pig pig) {
            return new CraftPig(server, pig);
        }
        if (entity instanceof net.minecraft.world.entity.TamableAnimal tameable) {
            return new CraftTameableAnimal(server, tameable);
        }
        if (entity instanceof net.minecraft.world.entity.animal.Animal animal) {
            return new CraftAnimals(server, animal);
        }
        if (entity instanceof net.minecraft.world.entity.AgeableMob ageable) {
            return new CraftAgeable(server, ageable);
        }
        if (entity instanceof net.minecraft.world.entity.PathfinderMob creature) {
            return new CraftCreature(server, creature);
        }
        if (entity instanceof net.minecraft.world.entity.Mob mob) {
            return new CraftMob(server, mob);
        }
        if (entity instanceof net.minecraft.world.entity.decoration.ArmorStand armorStand) {
            return new CraftArmorStand(server, armorStand);
        }
        if (entity instanceof net.minecraft.world.entity.LivingEntity living) {
            return new CraftLivingEntity(server, living);
        }
        // BlockAttachedEntity (ItemFrame/GlowItemFrame/Painting) extends Entity directly, not
        // LivingEntity, so without these branches it fell through to CraftUnknownEntity - a real
        // confirmed bug in Arclight (IzzelAliz/Arclight#2168: "modded BlockAttachedEntity cannot
        // be cast to org.bukkit.entity.Hanging"), and the same gap existed here for the vanilla
        // case too. GlowItemFrame extends ItemFrame, so it must be checked first.
        if (entity instanceof net.minecraft.world.entity.decoration.GlowItemFrame glowItemFrame) {
            return new CraftGlowItemFrame(server, glowItemFrame);
        }
        if (entity instanceof net.minecraft.world.entity.decoration.ItemFrame itemFrame) {
            return new CraftItemFrame(server, itemFrame);
        }
        if (entity instanceof net.minecraft.world.entity.decoration.Painting painting) {
            return new CraftPainting(server, painting);
        }
        if (entity instanceof net.minecraft.world.entity.decoration.LeashFenceKnotEntity leash) {
            return new CraftLeash(server, leash);
        }
        return new CraftUnknownEntity(server, entity);
    }

    protected EntityBridge bridge() {
        return (EntityBridge) entity;
    }


    public Entity getHandle() {
        return entity;
    }


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
    public @NotNull net.kyori.adventure.text.Component name() {
        return io.lunararcdevs.lunararc.common.messaging.LunarArcComponentPipeline.toAdventure(entity.getName());
    }

    @Override
    public org.bukkit.entity.Entity.@NotNull Spigot spigot() {
        return this.spigot;
    }

    @Override
    public @NotNull Server getServer() {
        return server;
    }

    @Override
    public @NotNull org.bukkit.entity.EntityType getType() {
        try {
            net.minecraft.resources.ResourceLocation id = entity.getType().builtInRegistryHolder().key().location();
            org.bukkit.entity.EntityType type = io.lunararcdevs.lunararc.common.server.LunarArcDynamicBukkitEnums.entityType(id);
            return type != null ? type : org.bukkit.entity.EntityType.UNKNOWN;
        } catch (Throwable t) {
            return org.bukkit.entity.EntityType.UNKNOWN;
        }
    }

    @Override
    public @Nullable String getAsString() {
        CraftEntitySnapshot snapshot = CraftEntitySnapshot.create(this);
        return snapshot == null ? null : snapshot.getAsString();
    }


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
        return entity.isAlive() && this.bridge().lunararc$isInWorld();
    }


    @Override
    public @NotNull World getWorld() {
        if (!(entity.level() instanceof net.minecraft.server.level.ServerLevel level)) {
            throw new IllegalStateException("Entity is not attached to a server world");
        }
        return server.getCraftWorld(level);
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


    @Override
    public boolean teleport(@NotNull Location location) {
        return teleport(location, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    @Override
    public boolean teleport(@NotNull Location location,
            org.bukkit.event.player.PlayerTeleportEvent.@NotNull TeleportCause cause) {
        java.util.Objects.requireNonNull(location, "location");
        java.util.Objects.requireNonNull(cause, "cause");
        if (entity.isVehicle() || entity.isRemoved()) return false;
        if (!(location.getWorld() instanceof org.bukkit.craftbukkit.CraftWorld targetWorld)) {
            throw new IllegalArgumentException("Teleport destination must be a LunarArc CraftWorld");
        }

        Location from = this.getLocation();
        Location requested = location.clone();
        final Location destination;
        if (this instanceof org.bukkit.entity.Player player) {
            org.bukkit.event.player.PlayerTeleportEvent event = new org.bukkit.event.player.PlayerTeleportEvent(
                    player, from, requested, cause);
            this.server.getPluginManager().callEvent(event);
            if (event.isCancelled() || event.getTo() == null) return false;
            destination = event.getTo();
        } else {
            org.bukkit.event.entity.EntityTeleportEvent event = new org.bukkit.event.entity.EntityTeleportEvent(
                    this, from, requested);
            this.server.getPluginManager().callEvent(event);
            if (event.isCancelled() || event.getTo() == null) return false;
            destination = event.getTo();
        }

        if (!(destination.getWorld() instanceof org.bukkit.craftbukkit.CraftWorld craftWorld)) {
            return false;
        }
        net.minecraft.server.level.ServerLevel level = craftWorld.getHandle();
        double x = destination.getX(), y = destination.getY(), z = destination.getZ();
        float yaw = destination.getYaw(), pitch = destination.getPitch();
        // PlayerChangedWorldEvent is deliberately not fired here. A cross-world teleport of a
        // player reaches ServerPlayer.changeDimension through teleportTo, and ServerPlayerMixin
        // fires the event there - the same place CraftBukkit does, so it also covers portals, an
        // end-return, and a mod's own transition, none of which come through this method. Firing
        // it here as well would deliver the event twice for every plugin teleport.
        entity.teleportTo(level, x, y, z, Collections.emptySet(), yaw, pitch);
        return true;
    }

    @Override
    public boolean teleport(@NotNull Location location,
            org.bukkit.event.player.PlayerTeleportEvent.@NotNull TeleportCause cause,
            @NotNull io.papermc.paper.entity.TeleportFlag... flags) {
        return teleport(location, cause);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> teleportAsync(@NotNull Location location,
            org.bukkit.event.player.PlayerTeleportEvent.@NotNull TeleportCause cause,
            @NotNull io.papermc.paper.entity.TeleportFlag... flags) {
        java.util.Objects.requireNonNull(location, "location");
        java.util.Objects.requireNonNull(cause, "cause");
        java.util.Objects.requireNonNull(flags, "flags");

        final Location requested = location.clone();
        if (!(requested.getWorld() instanceof org.bukkit.craftbukkit.CraftWorld craftWorld)) {
            return CompletableFuture.completedFuture(false);
        }

        // Paper's async teleport contract is primarily an asynchronous chunk-load
        // boundary. The actual entity mutation and Bukkit events must still run on
        // the owning Minecraft server thread. Do not run teleport() from an arbitrary
        // plugin completion/executor thread.
        final CompletableFuture<Boolean> result = new CompletableFuture<>();
        craftWorld.getChunkAtAsync(requested.getBlockX() >> 4, requested.getBlockZ() >> 4, true, true)
                .whenComplete((chunk, loadFailure) -> {
                    if (loadFailure != null) {
                        result.completeExceptionally(loadFailure);
                        return;
                    }
                    if (chunk == null) {
                        result.complete(false);
                        return;
                    }

                    final net.minecraft.server.MinecraftServer minecraftServer = craftWorld.getHandle().getServer();
                    Runnable finish = () -> {
                        try {
                            result.complete(teleport(requested, cause, flags));
                        } catch (Throwable throwable) {
                            result.completeExceptionally(throwable);
                        }
                    };
                    if (minecraftServer.isSameThread()) {
                        finish.run();
                    } else {
                        minecraftServer.execute(finish);
                    }
                });
        return result;
    }

    @Override
    public boolean teleport(@NotNull org.bukkit.entity.Entity destination) {
        return teleport(destination.getLocation());
    }

    @Override
    public boolean teleport(@NotNull org.bukkit.entity.Entity destination,
            org.bukkit.event.player.PlayerTeleportEvent.@NotNull TeleportCause cause) {
        return teleport(destination.getLocation(), cause);
    }


    @Override
    public boolean isPersistent() {
        return ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) entity).lunararc$isPersistent();
    }

    @Override
    public void setPersistent(boolean persistent) {
        ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) entity).lunararc$setPersistent(persistent);
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
        java.util.List<org.bukkit.entity.Entity> out = new java.util.ArrayList<>();
        for (Entity p : entity.getPassengers()) {
            org.bukkit.entity.Entity bukkit = getEntity(server, p);
            if (bukkit != null) out.add(bukkit);
        }
        return out;
    }

    @Override
    public boolean addPassenger(@NotNull org.bukkit.entity.Entity passenger) {
        if (!(passenger instanceof CraftEntity ce)) return false;
        return ce.entity.startRiding(entity, true);
    }

    @Override
    public boolean removePassenger(@NotNull org.bukkit.entity.Entity passenger) {
        if (!(passenger instanceof CraftEntity ce)) return false;
        if (!entity.getPassengers().contains(ce.entity)) return false;
        ce.entity.stopRiding();
        return true;
    }

    @Override
    public boolean isEmpty() {
        return entity.getPassengers().isEmpty();
    }

    @Override
    public boolean eject() {
        if (entity.getPassengers().isEmpty()) return false;
        entity.ejectPassengers();
        return true;
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
        this.lastDamageCause = cause;
    }

    @Override
    public @Nullable org.bukkit.event.entity.EntityDamageEvent getLastDamageCause() {
        return this.lastDamageCause;
    }

    @Override
    public boolean isTicking() {
        if (!(entity.level() instanceof net.minecraft.server.level.ServerLevel level)) return false;
        return level.getChunkSource().getChunkNow(entity.chunkPosition().x, entity.chunkPosition().z) != null;
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
        if (!(entity.level() instanceof net.minecraft.server.level.ServerLevel level)) return java.util.Set.of();
        net.minecraft.world.level.ChunkPos chunk = entity.chunkPosition();
        java.util.LinkedHashSet<org.bukkit.entity.Player> result = new java.util.LinkedHashSet<>();
        for (net.minecraft.server.level.ServerPlayer player : level.players()) {
            if (!player.getChunkTrackingView().contains(chunk)) continue;
            org.bukkit.entity.Entity bukkit = getEntity(server, player);
            if (bukkit instanceof org.bukkit.entity.Player tracked) result.add(tracked);
        }
        return java.util.Collections.unmodifiableSet(result);
    }

    @Override public org.bukkit.event.entity.CreatureSpawnEvent.@NotNull SpawnReason getEntitySpawnReason() {
        org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason = this.bridge().lunararc$getSpawnReason();
        return reason == null ? org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.DEFAULT : reason;
    }
    @Override public boolean fromMobSpawner() { return this.bridge().lunararc$fromMobSpawner(); }
    @Override public @Nullable Location getOrigin() {
        net.minecraft.world.phys.Vec3 origin = this.bridge().lunararc$getOrigin();
        if (origin == null) return null;
        java.util.UUID originWorld = this.bridge().lunararc$getOriginWorld();
        org.bukkit.World world = originWorld == null ? this.getWorld() : org.bukkit.Bukkit.getWorld(originWorld);
        return new Location(world, origin.x, origin.y, origin.z);
    }
    @Override public @NotNull net.kyori.adventure.text.Component teamDisplayName() {
        return io.papermc.paper.adventure.PaperAdventure.asAdventure(entity.getDisplayName());
    }
    @Override public @NotNull org.bukkit.entity.Entity copy(@NotNull Location to) {
        CraftEntitySnapshot snapshot = CraftEntitySnapshot.create(this);
        if (snapshot == null) throw new IllegalArgumentException("Error creating entity snapshot");
        return snapshot.createEntity(java.util.Objects.requireNonNull(to, "to"));
    }
    @Override public @NotNull org.bukkit.entity.Entity copy() {
        CraftEntitySnapshot snapshot = CraftEntitySnapshot.create(this);
        if (snapshot == null) throw new IllegalArgumentException("Error creating entity snapshot");
        return snapshot.createEntity(this.getWorld());
    }
    @Override public @Nullable org.bukkit.entity.EntitySnapshot createSnapshot() { return CraftEntitySnapshot.create(this); }
    @Override public boolean isInWorld() { return this.bridge().lunararc$isInWorld(); }
    @Override public @NotNull org.bukkit.entity.SpawnCategory getSpawnCategory() {
        return org.bukkit.craftbukkit.util.CraftSpawnCategory.toBukkit(entity.getType().getCategory());
    }
    @Override public boolean hasFixedPose() { return fixedPose; }
    @Override public void setPose(@NotNull org.bukkit.entity.Pose pose, boolean fixed) {
        if (pose == null) throw new IllegalArgumentException("pose cannot be null");
        try {
            entity.setPose(net.minecraft.world.entity.Pose.valueOf(pose.name()));
            fixedPose = fixed;
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported pose for Minecraft 1.21.1: " + pose, ex);
        }
    }
    @Override public void setSneaking(boolean sneak) { entity.setShiftKeyDown(sneak); }
    @Override public boolean isSneaking() { return entity.isShiftKeyDown(); }
    @Override public @NotNull Set<org.bukkit.entity.Player> getTrackedBy() { return getTrackedPlayers(); }
    @Override public boolean isVisibleByDefault() { return visibleByDefault; }
    @Override public void setVisibleByDefault(boolean visible) { this.visibleByDefault = visible; }
    @Override public @NotNull org.bukkit.Sound getSwimHighSpeedSplashSound() { return org.bukkit.Sound.ENTITY_GENERIC_SPLASH; }
    @Override public @NotNull org.bukkit.Sound getSwimSplashSound() { return org.bukkit.Sound.ENTITY_GENERIC_SPLASH; }
    @Override public @NotNull org.bukkit.Sound getSwimSound() { return org.bukkit.Sound.ENTITY_GENERIC_SWIM; }
    @Override public void lockFreezeTicks(boolean lock) { this.freezeTicksLocked = lock; }
    @Override public boolean isFreezeTickingLocked() { return freezeTicksLocked; }
    @Override public boolean hasNoPhysics() { return entity.noPhysics; }
    @Override public void setNoPhysics(boolean noPhysics) { entity.noPhysics = noPhysics; }
    @Override public boolean isInvisible() { return entity.isInvisible(); }
    @Override public void setInvisible(boolean invisible) { entity.setInvisible(invisible); }
    @Override public boolean isFrozen() { return entity.isFullyFrozen(); }
    @Override public void setFreezeTicks(int ticks) { entity.setTicksFrozen(ticks); }
    @Override public int getMaxFreezeTicks() { return entity.getTicksRequiredToFreeze(); }
    @Override public int getFreezeTicks() { return entity.getTicksFrozen(); }
    @Override public boolean isVisualFire() { return this.bridge().lunararc$isVisualFire(); }
    @Override public void setVisualFire(boolean fire) {
        this.bridge().lunararc$setVisualFire(fire);
        entity.setSharedFlagOnFire(this.bridge().lunararc$getRemainingFireTicks() > 0);
    }
    @Override public void setFireTicks(int ticks) { ((EntityBridge) entity).lunararc$setRemainingFireTicks(ticks); }
    @Override public int getMaxFireTicks() { return entity.getFireImmuneTicks(); }
    @Override public int getFireTicks() { return ((EntityBridge) entity).lunararc$getRemainingFireTicks(); }
    @Override public @NotNull List<org.bukkit.entity.Entity> getNearbyEntities(double x, double y, double z) {
        java.util.List<org.bukkit.entity.Entity> result = new java.util.ArrayList<>();
        net.minecraft.world.phys.AABB box = entity.getBoundingBox().inflate(x, y, z);
        for (net.minecraft.world.entity.Entity candidate : entity.level().getEntities(entity, box)) {
            org.bukkit.entity.Entity bukkit = getEntity(server, candidate);
            if (bukkit != null) result.add(bukkit);
        }
        return result;
    }
    @Override public void sendMessage(@Nullable UUID sender, @NotNull String... message) {
        java.util.Objects.requireNonNull(message, "message");
        for (String line : message) sendMessage(sender, line);
    }
    @Override public void sendMessage(@Nullable UUID sender, @NotNull String message) {
        entity.sendSystemMessage(io.lunararcdevs.lunararc.common.messaging.LunarArcComponentPipeline.fromLegacy(
                java.util.Objects.requireNonNull(message, "message")));
    }
    @Override public void sendMessage(@NotNull String... message) { sendMessage((UUID) null, message); }
    @Override public void sendMessage(@NotNull String message) { sendMessage((UUID) null, message); }
    @Override public void customName(net.kyori.adventure.text.Component customName) {
        entity.setCustomName(customName == null ? null
                : io.lunararcdevs.lunararc.common.messaging.LunarArcComponentPipeline.fromAdventure(customName));
    }
    @Override public net.kyori.adventure.text.Component customName() {
        return entity.getCustomName() == null ? null : io.papermc.paper.adventure.PaperAdventure.asAdventure(entity.getCustomName());
    }

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
        java.util.Objects.requireNonNull(type, "type");
        if (entity.level() instanceof net.minecraft.server.level.ServerLevel level) {
            level.broadcastEntityEvent(entity, type.getData());
        }
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
        float yaw = entity.getYRot() % 360.0F;
        if (yaw < 0.0F) yaw += 360.0F;
        if (yaw >= 315.0F || yaw < 45.0F) return org.bukkit.block.BlockFace.SOUTH;
        if (yaw < 135.0F) return org.bukkit.block.BlockFace.WEST;
        if (yaw < 225.0F) return org.bukkit.block.BlockFace.NORTH;
        return org.bukkit.block.BlockFace.EAST;
    }

    @Override
    public @NotNull org.bukkit.entity.Pose getPose() {
        try { return org.bukkit.entity.Pose.valueOf(entity.getPose().name()); }
        catch (IllegalArgumentException ignored) { return org.bukkit.entity.Pose.STANDING; }
    }

    @Override
    public boolean isPermissionSet(@NotNull String name) { return permissible.isPermissionSet(name); }

    @Override
    public boolean isPermissionSet(@NotNull org.bukkit.permissions.Permission perm) { return permissible.isPermissionSet(perm); }

    @Override
    public boolean hasPermission(@NotNull String name) { return permissible.hasPermission(name); }

    @Override
    public boolean hasPermission(@NotNull org.bukkit.permissions.Permission perm) { return permissible.hasPermission(perm); }

    @Override
    public @NotNull org.bukkit.permissions.PermissionAttachment addAttachment(@NotNull org.bukkit.plugin.Plugin plugin,
            @NotNull String name, boolean value) { return permissible.addAttachment(plugin, name, value); }

    @Override
    public @NotNull org.bukkit.permissions.PermissionAttachment addAttachment(
            @NotNull org.bukkit.plugin.Plugin plugin) { return permissible.addAttachment(plugin); }

    @Override
    public @Nullable org.bukkit.permissions.PermissionAttachment addAttachment(@NotNull org.bukkit.plugin.Plugin plugin,
            @NotNull String name, boolean value, int ticks) { return permissible.addAttachment(plugin, name, value, ticks); }

    @Override
    public @Nullable org.bukkit.permissions.PermissionAttachment addAttachment(@NotNull org.bukkit.plugin.Plugin plugin,
            int ticks) { return permissible.addAttachment(plugin, ticks); }

    @Override
    public void removeAttachment(@NotNull org.bukkit.permissions.PermissionAttachment attachment) { permissible.removeAttachment(attachment); }

    @Override
    public void recalculatePermissions() { permissible.recalculatePermissions(); }

    @Override
    public @NotNull Set<org.bukkit.permissions.PermissionAttachmentInfo> getEffectivePermissions() { return permissible.getEffectivePermissions(); }

    @Override
    public boolean isOp() {
        return false;
    }

    @Override
    public void setOp(boolean value) {
    }


    @Override
    public void broadcastHurtAnimation(@NotNull Collection<org.bukkit.entity.Player> players) {
        java.util.Objects.requireNonNull(players, "players");
        if (players.contains(this)) {
            throw new IllegalArgumentException("Cannot broadcast hurt animation to self without a yaw");
        }
        for (org.bukkit.entity.Player player : players) {
            if (!(player instanceof CraftPlayer craftPlayer)) {
                throw new IllegalArgumentException("Player is not backed by LunarArc CraftPlayer: " + player);
            }
            craftPlayer.sendHurtAnimation(0.0F, this);
        }
    }

    @Override
    public @NotNull String getScoreboardEntryName() {
        return getUniqueId().toString();
    }

    @Override
    public @NotNull io.papermc.paper.threadedregions.scheduler.EntityScheduler getScheduler() {
        return server.getEntityScheduler(entity);
    }

    @Override
    public boolean wouldCollideUsing(@NotNull BoundingBox boundingBox) {
        net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(
                boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(),
                boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
        return !entity.level().noCollision(entity, box);
    }

    @Override
    public boolean collidesAt(@NotNull Location location) {
        if (location.getWorld() != getWorld()) return false;
        net.minecraft.world.phys.AABB box = entity.getBoundingBox().move(
                location.getX() - entity.getX(), location.getY() - entity.getY(), location.getZ() - entity.getZ());
        return !entity.level().noCollision(entity, box);
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
            org.bukkit.event.entity.CreatureSpawnEvent.@NotNull SpawnReason reason) {
        java.util.Objects.requireNonNull(location, "location");
        java.util.Objects.requireNonNull(reason, "reason");
        if (!(location.getWorld() instanceof org.bukkit.craftbukkit.CraftWorld world)) {
            throw new IllegalArgumentException("Location must reference a LunarArc CraftWorld");
        }
        if (this.bridge().lunararc$isInWorld()) {
            return false;
        }
        this.bridge().lunararc$setLevel(world.getHandle());
        entity.setPos(location.getX(), location.getY(), location.getZ());
        entity.setRot(location.getYaw(), location.getPitch());
        ServerLevelBridge levelBridge = (ServerLevelBridge) world.getHandle();
        if (!levelBridge.lunararc$addFreshEntity(entity, reason)) {
            return false;
        }
        entity.getIndirectPassengers().forEach(passenger -> {
            ((EntityBridge) passenger).lunararc$setLevel(world.getHandle());
            levelBridge.lunararc$addFreshEntity(passenger, reason);
        });
        return true;
    }


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
            super.setLastDamageCause(cause);
        }

        @Override
        public @Nullable org.bukkit.event.entity.EntityDamageEvent getLastDamageCause() {
            return super.getLastDamageCause();
        }

        @Override
        public @NotNull Set<org.bukkit.entity.Player> getTrackedPlayers() {
            return super.getTrackedPlayers();
        }


        @Override
        public org.bukkit.entity.Entity.Spigot spigot() {
            return new org.bukkit.entity.Entity.Spigot();
        }


    }

    // CraftBukkit's non-API surface on CraftEntity. getHandleRaw and the momentum pair are how
    // plugins written against older CraftBukkit still reach the entity and its velocity; the
    // Bukkit-values pair is the persistent-data round trip CraftBukkit performs on save and load.
    public Entity getHandleRaw() {
        return this.entity;
    }

    public org.bukkit.util.Vector getMomentum() {
        return this.getVelocity();
    }

    public void setMomentum(org.bukkit.util.Vector value) {
        this.setVelocity(value);
    }

    public void storeBukkitValues(net.minecraft.nbt.CompoundTag c) {
        if (this.getPersistentDataContainer() instanceof CraftPersistentDataContainer container
                && !container.isEmpty()) {
            c.put("BukkitValues", container.toTagCompound());
        }
    }

    public void readBukkitValues(net.minecraft.nbt.CompoundTag c) {
        if (c.get("BukkitValues") instanceof net.minecraft.nbt.CompoundTag base
                && this.getPersistentDataContainer() instanceof CraftPersistentDataContainer container) {
            container.putAll(base);
        }
    }

    public void update(net.minecraft.server.level.ServerPlayer player) {
        if (!this.getHandle().isAlive()) {
            return;
        }

        net.minecraft.server.level.ChunkMap chunkMap =
                ((org.bukkit.craftbukkit.CraftWorld) this.getWorld()).getHandle().getChunkSource().chunkMap;
        net.minecraft.server.level.ChunkMap.TrackedEntity tracked =
                ((io.lunararcdevs.lunararc.common.bridge.access.ChunkMapAccessBridge) (Object) chunkMap)
                        .lunararc$getEntityMap().get(this.getEntityId());
        if (tracked == null) {
            return;
        }

        tracked.removePlayer(player);
        tracked.updatePlayer(player);
    }
}
