package io.lunararcdevs.lunararc.common.mixin.core.projectile;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(ShulkerBullet.class)
public interface ShulkerBulletAccessor extends io.lunararcdevs.lunararc.common.bridge.access.ShulkerBulletAccessBridge {
    @Accessor("finalTarget") Entity lunararc$getFinalTarget();
    @Accessor("finalTarget") void lunararc$setFinalTarget(Entity target);
    @Accessor("targetId") void lunararc$setTargetId(UUID targetId);
    @Accessor("currentMoveDirection") Direction lunararc$getCurrentMoveDirection();
    @Accessor("currentMoveDirection") void lunararc$setCurrentMoveDirection(Direction direction);
    @Accessor("flightSteps") int lunararc$getFlightSteps();
    @Accessor("flightSteps") void lunararc$setFlightSteps(int steps);
    @Accessor("targetDeltaX") double lunararc$getTargetDeltaX();
    @Accessor("targetDeltaX") void lunararc$setTargetDeltaX(double value);
    @Accessor("targetDeltaY") double lunararc$getTargetDeltaY();
    @Accessor("targetDeltaY") void lunararc$setTargetDeltaY(double value);
    @Accessor("targetDeltaZ") double lunararc$getTargetDeltaZ();
    @Accessor("targetDeltaZ") void lunararc$setTargetDeltaZ(double value);
}
