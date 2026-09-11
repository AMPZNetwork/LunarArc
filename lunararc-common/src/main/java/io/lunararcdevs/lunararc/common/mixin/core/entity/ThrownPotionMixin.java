package io.lunararcdevs.lunararc.common.mixin.core.entity;

import com.llamalad7.mixinextras.sugar.Local;
import io.lunararcdevs.lunararc.common.bridge.LivingEntityBridge;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownPotion;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Tags splash-potion effect additions without replacing splash mechanics. */
@Mixin(ThrownPotion.class)
public abstract class ThrownPotionMixin {
    @Inject(
            method = "applySplash",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"),
            require = 0)
    private void lunararc$splashCause(CallbackInfo ci, @Local LivingEntity target) {
        ((LivingEntityBridge) target).lunararc$pushEffectCause(EntityPotionEffectEvent.Cause.POTION_SPLASH);
    }
}
