package io.lunararcdevs.lunararc.common.mixin.core.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.lunararcdevs.lunararc.common.bridge.LivingEntityBridge;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.MilkBucketItem;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Gives milk removals their Bukkit cause while retaining the real LivingEntity effect path. */
@Mixin(MilkBucketItem.class)
public abstract class MilkBucketItemMixin {
    @WrapOperation(
            method = "finishUsingItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;removeAllEffects()Z"),
            require = 0)
    private boolean lunararc$milkCause(LivingEntity entity, Operation<Boolean> original) {
        if (entity instanceof LivingEntityBridge bridge) {
            return bridge.lunararc$removeAllEffects(EntityPotionEffectEvent.Cause.MILK);
        }
        return original.call(entity);
    }
}
