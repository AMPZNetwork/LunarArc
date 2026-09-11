package org.bukkit.craftbukkit.entity;

import io.lunararcdevs.lunararc.common.bridge.access.ShulkerBulletAccessBridge;
import net.minecraft.core.Direction;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class CraftShulkerBullet extends CraftProjectile implements ShulkerBullet {
    public CraftShulkerBullet(CraftServer server, net.minecraft.world.entity.projectile.ShulkerBullet entity) { super(server, entity); }
    private ShulkerBulletAccessBridge access() { return (ShulkerBulletAccessBridge) (Object) getHandle(); }
    @Override public @Nullable org.bukkit.entity.Entity getTarget() {
        net.minecraft.world.entity.Entity target = access().lunararc$getFinalTarget();
        return target == null ? null : CraftEntity.getEntity(this.server, target);
    }
    @Override public void setTarget(@Nullable org.bukkit.entity.Entity target) {
        net.minecraft.world.entity.Entity nms = null;
        if (target != null) {
            if (!(target instanceof CraftEntity craft)) throw new IllegalArgumentException("Target is not backed by LunarArc CraftEntity");
            nms = craft.getHandle();
        }
        access().lunararc$setFinalTarget(nms);
        access().lunararc$setTargetId(nms == null ? null : nms.getUUID());
    }
    @Override public Vector getTargetDelta() { return new Vector(access().lunararc$getTargetDeltaX(), access().lunararc$getTargetDeltaY(), access().lunararc$getTargetDeltaZ()); }
    @Override public void setTargetDelta(Vector vector) {
        Objects.requireNonNull(vector, "vector");
        access().lunararc$setTargetDeltaX(vector.getX()); access().lunararc$setTargetDeltaY(vector.getY()); access().lunararc$setTargetDeltaZ(vector.getZ());
    }
    @Override public @Nullable BlockFace getCurrentMovementDirection() { return toBlockFace(access().lunararc$getCurrentMoveDirection()); }
    @Override public void setCurrentMovementDirection(@Nullable BlockFace direction) { access().lunararc$setCurrentMoveDirection(toDirection(direction)); }
    @Override public int getFlightSteps() { return access().lunararc$getFlightSteps(); }
    @Override public void setFlightSteps(int steps) { access().lunararc$setFlightSteps(steps); }
    @Override public net.minecraft.world.entity.projectile.ShulkerBullet getHandle() { return (net.minecraft.world.entity.projectile.ShulkerBullet) this.entity; }
    private static BlockFace toBlockFace(Direction d) { if (d == null) return null; return switch (d) { case DOWN -> BlockFace.DOWN; case UP -> BlockFace.UP; case NORTH -> BlockFace.NORTH; case SOUTH -> BlockFace.SOUTH; case WEST -> BlockFace.WEST; case EAST -> BlockFace.EAST; }; }
    private static Direction toDirection(BlockFace f) { if (f == null) return null; return switch (f) { case DOWN -> Direction.DOWN; case UP -> Direction.UP; case NORTH -> Direction.NORTH; case SOUTH -> Direction.SOUTH; case WEST -> Direction.WEST; case EAST -> Direction.EAST; default -> throw new IllegalArgumentException("Movement direction must be a cardinal block face: " + f); }; }
}
