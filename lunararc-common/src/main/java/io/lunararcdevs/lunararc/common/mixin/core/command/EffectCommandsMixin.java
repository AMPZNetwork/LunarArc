package io.lunararcdevs.lunararc.common.mixin.core.command;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.lunararcdevs.lunararc.common.bridge.LivingEntityBridge;
import net.minecraft.server.commands.EffectCommands;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Carries COMMAND provenance through vanilla /effect mutations. */
@Mixin(EffectCommands.class)
public abstract class EffectCommandsMixin {
    @WrapOperation(
            method = "giveEffect",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"),
            require = 0)
    private static boolean lunararc$commandAdd(
            LivingEntity entity,
            net.minecraft.world.effect.MobEffectInstance effect,
            net.minecraft.world.entity.Entity source,
            Operation<Boolean> original) {
        ((LivingEntityBridge) entity).lunararc$pushEffectCause(EntityPotionEffectEvent.Cause.COMMAND);
        return original.call(entity, effect, source);
    }

    @WrapOperation(
            method = "clearEffects",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;removeAllEffects()Z"),
            require = 0)
    private static boolean lunararc$commandClear(LivingEntity entity, Operation<Boolean> original) {
        return ((LivingEntityBridge) entity).lunararc$removeAllEffects(EntityPotionEffectEvent.Cause.COMMAND);
    }

    @WrapOperation(
            method = "clearEffect",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;removeEffect(Lnet/minecraft/core/Holder;)Z"),
            require = 0)
    private static boolean lunararc$commandRemove(
            LivingEntity entity,
            net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,
            Operation<Boolean> original) {
        ((LivingEntityBridge) entity).lunararc$pushEffectCause(EntityPotionEffectEvent.Cause.COMMAND);
        return original.call(entity, effect);
    }
}
