package io.lunararcdevs.lunararc.common.mixin.core.entity;

import io.lunararcdevs.lunararc.common.bridge.LivingEntityBridge;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Carries the Bukkit ARROW effect cause into the real tipped-arrow effect additions. */
@Mixin(Arrow.class)
public abstract class ArrowMixin {
    @Inject(
            method = "doPostHurtEffects",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"),
            require = 0)
    private void lunararc$arrowCause1(LivingEntity target, CallbackInfo ci) {
        ((LivingEntityBridge) target).lunararc$pushEffectCause(EntityPotionEffectEvent.Cause.ARROW);
    }

    @Inject(
            method = "doPostHurtEffects",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"),
            require = 0)
    private void lunararc$arrowCause2(LivingEntity target, CallbackInfo ci) {
        ((LivingEntityBridge) target).lunararc$pushEffectCause(EntityPotionEffectEvent.Cause.ARROW);
    }
}
