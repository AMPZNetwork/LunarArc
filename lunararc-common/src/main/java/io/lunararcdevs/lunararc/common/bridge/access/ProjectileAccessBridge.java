package io.lunararcdevs.lunararc.common.bridge.access;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface ProjectileAccessBridge {
    boolean lunararc$invokeCanHitEntity(Entity entity);
    void lunararc$invokeOnHitEntity(EntityHitResult result);
}
