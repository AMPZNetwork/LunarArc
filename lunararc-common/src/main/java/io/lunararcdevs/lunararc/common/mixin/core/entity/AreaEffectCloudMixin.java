package io.lunararcdevs.lunararc.common.mixin.core.entity;

import com.llamalad7.mixinextras.sugar.Local;
import io.lunararcdevs.lunararc.common.bridge.LivingEntityBridge;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Supplies AREA_EFFECT_CLOUD provenance at the vanilla cloud application boundary. */
@Mixin(AreaEffectCloud.class)
public abstract class AreaEffectCloudMixin {
    @Inject(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"),
            require = 0)
    private void lunararc$cloudCause(CallbackInfo ci, @Local LivingEntity target) {
        ((LivingEntityBridge) target).lunararc$pushEffectCause(EntityPotionEffectEvent.Cause.AREA_EFFECT_CLOUD);
    }
}
