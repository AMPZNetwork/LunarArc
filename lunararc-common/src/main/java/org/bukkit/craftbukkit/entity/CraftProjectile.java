package org.bukkit.craftbukkit.entity;

import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import io.lunararcdevs.lunararc.common.bridge.ProjectileBridge;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Projectile;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public abstract class CraftProjectile extends CraftEntity implements Projectile {
    protected CraftProjectile(CraftServer server, net.minecraft.world.entity.projectile.Projectile entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.projectile.Projectile getHandle() {
        return (net.minecraft.world.entity.projectile.Projectile) this.entity;
    }

    private ProjectileBridge projectileBridge() {
        return (ProjectileBridge) this.entity;
    }

    @Override
    @Deprecated
    public boolean doesBounce() {
        return projectileBridge().lunararc$doesBounce();
    }

    @Override
    @Deprecated
    public void setBounce(boolean doesBounce) {
        projectileBridge().lunararc$setBounce(doesBounce);
    }

    @Override
    public boolean hasLeftShooter() {
        return projectileBridge().lunararc$hasLeftShooter();
    }

    @Override
    public void setHasLeftShooter(boolean leftShooter) {
        projectileBridge().lunararc$setHasLeftShooter(leftShooter);
    }

    @Override
    public boolean hasBeenShot() {
        return projectileBridge().lunararc$hasBeenShot();
    }

    @Override
    public void setHasBeenShot(boolean beenShot) {
        projectileBridge().lunararc$setHasBeenShot(beenShot);
    }

    @Override
    public boolean canHitEntity(org.bukkit.entity.Entity entity) {
        Objects.requireNonNull(entity, "entity");
        if (!(entity instanceof CraftEntity craftEntity)) throw new IllegalArgumentException("Entity is not backed by LunarArc");
        return projectileBridge().lunararc$canHitEntity(craftEntity.getHandle());
    }

    @Override
    public void hitEntity(org.bukkit.entity.Entity entity) {
        Objects.requireNonNull(entity, "entity");
        if (!(entity instanceof CraftEntity craftEntity)) throw new IllegalArgumentException("Entity is not backed by LunarArc");
        projectileBridge().lunararc$hitEntity(craftEntity.getHandle(), null);
    }

    @Override
    public void hitEntity(org.bukkit.entity.Entity entity, Vector vector) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(vector, "vector");
        if (!(entity instanceof CraftEntity craftEntity)) throw new IllegalArgumentException("Entity is not backed by LunarArc");
        projectileBridge().lunararc$hitEntity(craftEntity.getHandle(), new Vec3(vector.getX(), vector.getY(), vector.getZ()));
    }

    @Override
    public @Nullable ProjectileSource getShooter() {
        ProjectileSource source = projectileBridge().lunararc$getProjectileSource();
        if (source != null) return source;
        net.minecraft.world.entity.Entity owner = getHandle().getOwner();
        if (owner == null) return null;
        org.bukkit.entity.Entity bukkit = ((EntityBridge) owner).lunararc$getBukkitEntity();
        if (bukkit instanceof ProjectileSource projectileSource) {
            projectileBridge().lunararc$setProjectileSource(projectileSource);
            return projectileSource;
        }
        return null;
    }

    @Override
    public void setShooter(@Nullable ProjectileSource source) {
        if (source instanceof CraftEntity craftEntity) {
            getHandle().setOwner(craftEntity.getHandle());
        } else {
            getHandle().setOwner(null);
        }
        projectileBridge().lunararc$setProjectileSource(source);
    }

    @Override
    public @Nullable UUID getOwnerUniqueId() {
        return projectileBridge().lunararc$getOwnerUUID();
    }
}
