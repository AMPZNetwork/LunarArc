package io.lunararcdevs.lunararc.common.mixin.core.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Projectile.class)
public interface ProjectileAccessor extends io.lunararcdevs.lunararc.common.bridge.access.ProjectileAccessBridge {
    @Invoker("canHitEntity")
    boolean lunararc$invokeCanHitEntity(Entity entity);

    @Invoker("onHitEntity")
    void lunararc$invokeOnHitEntity(EntityHitResult result);
}
