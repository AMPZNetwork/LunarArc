package io.lunararcdevs.lunararc.neoforge.mixin.core.effect;

import io.lunararcdevs.lunararc.common.bridge.PlayerExhaustionBridge;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.effect.HungerMobEffect", remap = false)
public abstract class HungerMobEffectMixin_NeoForge {
    @Inject(
            method = "applyEffectTick",
            remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V", remap = false),
            require = 0)
    private void lunararc$hungerExhaustion(LivingEntity entity, int amplifier, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Player player) {
            ((PlayerExhaustionBridge) player).lunararc$pushExhaustionReason(
                    org.bukkit.event.entity.EntityExhaustionEvent.ExhaustionReason.HUNGER_EFFECT);
        }
    }
}
