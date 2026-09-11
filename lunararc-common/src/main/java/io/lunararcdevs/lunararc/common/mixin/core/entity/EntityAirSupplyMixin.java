package io.lunararcdevs.lunararc.common.mixin.core.entity;

import io.lunararcdevs.lunararc.common.bridge.LivingEntityBridge;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Makes Bukkit's mutable maximum-air value authoritative in vanilla breathing logic. */
@Mixin(Entity.class)
public abstract class EntityAirSupplyMixin {
    @Inject(method = "getMaxAirSupply", at = @At("HEAD"), cancellable = true, require = 0)
    private void lunararc$maximumAir(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof LivingEntityBridge bridge) {
            int maximum = bridge.lunararc$getMaximumAirOverride();
            if (maximum >= 0) cir.setReturnValue(maximum);
        }
    }
}
