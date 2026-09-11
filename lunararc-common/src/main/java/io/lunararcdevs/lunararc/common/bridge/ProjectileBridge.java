package io.lunararcdevs.lunararc.common.bridge;

import java.util.UUID;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.Nullable;

/**
 * Small state/access bridge mixed directly into the loader-owned NMS Projectile.
 * This is deliberately not a runtime dispatcher or proxy layer.
 */
public interface ProjectileBridge {
    boolean lunararc$hasLeftShooter();
    void lunararc$setHasLeftShooter(boolean value);
    boolean lunararc$hasBeenShot();
    void lunararc$setHasBeenShot(boolean value);
    boolean lunararc$doesBounce();
    void lunararc$setBounce(boolean value);
    boolean lunararc$canHitEntity(Entity entity);
    void lunararc$hitEntity(Entity entity, @Nullable Vec3 hitPosition);
    @Nullable ProjectileSource lunararc$getProjectileSource();
    void lunararc$setProjectileSource(@Nullable ProjectileSource source);
    @Nullable UUID lunararc$getOwnerUUID();
}
