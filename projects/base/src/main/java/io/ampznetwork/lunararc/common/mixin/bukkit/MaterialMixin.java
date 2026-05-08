package io.ampznetwork.lunararc.common.mixin.bukkit;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Material.class)
public abstract class MaterialMixin {
    
    @Shadow(remap = false)
    public abstract boolean isLegacy();

    /**
     * @author Antigravity
     * @reason Fixes IllegalArgumentException when getting key of legacy materials.
     */
    @Inject(method = "getKey", at = @At("HEAD"), cancellable = true, remap = false)
    private void lunararc$onGetKey(CallbackInfoReturnable<NamespacedKey> cir) {
        if (isLegacy()) {
            // Standard Bukkit behavior throws an exception for legacy materials.
            // We return a fallback key to prevent plugins like CMILib from crashing.
            cir.setReturnValue(NamespacedKey.minecraft("air"));
        }
    }
}
