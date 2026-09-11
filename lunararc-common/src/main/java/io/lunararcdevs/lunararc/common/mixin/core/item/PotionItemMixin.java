package io.lunararcdevs.lunararc.common.mixin.core.item;

import io.lunararcdevs.lunararc.common.bridge.LivingEntityBridge;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Tags non-instant potion-drink effects before vanilla applies them. */
@Mixin(PotionItem.class)
public abstract class PotionItemMixin {
    @Inject(
            method = "finishUsingItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"),
            require = 0)
    private void lunararc$potionDrinkCause(
            ItemStack stack, Level level, LivingEntity entity,
            CallbackInfoReturnable<ItemStack> cir) {
        ((LivingEntityBridge) entity).lunararc$pushEffectCause(EntityPotionEffectEvent.Cause.POTION_DRINK);
    }
}
