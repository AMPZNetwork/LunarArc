package org.bukkit.craftbukkit.entity;

import io.lunararcdevs.lunararc.common.bridge.AbstractHurtingProjectileBridge;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Fireball;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CraftFireball extends CraftProjectile implements Fireball {
    public CraftFireball(CraftServer server, AbstractHurtingProjectile entity) { super(server, entity); }
    protected AbstractHurtingProjectileBridge fireballBridge() { return (AbstractHurtingProjectileBridge) (Object) getHandle(); }
    @Override public float getYield() { return fireballBridge().lunararc$getBukkitYield(); }
    @Override public void setYield(float yield) { fireballBridge().lunararc$setBukkitYield(yield); }
    @Override public boolean isIncendiary() { return fireballBridge().lunararc$isIncendiary(); }
    @Override public void setIsIncendiary(boolean incendiary) { fireballBridge().lunararc$setIncendiary(incendiary); }
    @Override public @NotNull Vector getDirection() { return getAcceleration(); }
    @Override public void setDirection(@NotNull Vector direction) {
        Objects.requireNonNull(direction, "direction");
        if (direction.isZero()) { setVelocity(direction); setAcceleration(direction); return; }
        Vector normalized = direction.clone().normalize();
        setVelocity(normalized.clone().multiply(getVelocity().length()));
        setAcceleration(normalized.multiply(getAcceleration().length()));
    }
    @Override public void setAcceleration(@NotNull Vector acceleration) {
        Objects.requireNonNull(acceleration, "acceleration");
        getHandle().assignDirectionalMovement(new Vec3(acceleration.getX(), acceleration.getY(), acceleration.getZ()), acceleration.length());
    }
    @Override public @NotNull Vector getAcceleration() {
        Vec3 delta = getHandle().getDeltaMovement();
        return new Vector(delta.x, delta.y, delta.z);
    }
    @Override public void setPower(@NotNull Vector power) { setAcceleration(power); }
    @Override public @NotNull Vector getPower() { return getAcceleration(); }
    @Override public AbstractHurtingProjectile getHandle() { return (AbstractHurtingProjectile) this.entity; }
}
