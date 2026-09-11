package io.lunararcdevs.lunararc.common.mixin.core.entity;

import io.lunararcdevs.lunararc.common.bridge.LivingEntityBridge;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Applies Bukkit LivingEntity collidable/exemption state at vanilla's real push-collision selector. */
@Mixin(EntitySelector.class)
public abstract class EntitySelectorCollisionMixin {
    @Inject(method = "pushableBy", at = @At("RETURN"), cancellable = true, require = 0)
    private static void lunararc$bukkitCollidable(Entity source, CallbackInfoReturnable<Predicate<Entity>> cir) {
        Predicate<Entity> vanilla = cir.getReturnValue();
        if (vanilla == null) return;
        cir.setReturnValue(candidate -> vanilla.test(candidate)
                && lunararc$allowsCollision(source, candidate)
                && lunararc$allowsCollision(candidate, source));
    }

    private static boolean lunararc$allowsCollision(Entity entity, Entity other) {
        if (!(entity instanceof LivingEntityBridge bridge)) return true;
        return bridge.lunararc$isCollidable() != bridge.lunararc$getCollidableExemptions().contains(other.getUUID());
    }
}
