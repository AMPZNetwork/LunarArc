package io.lunararcdevs.lunararc.common.mixin.core.entity;

import io.lunararcdevs.lunararc.common.bridge.LightningBoltBridge;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(LightningBolt.class)
public abstract class LightningBoltMixin implements LightningBoltBridge {
    @Unique private boolean lunararc$effect;

    @Override
    public boolean lunararc$isEffect() {
        return lunararc$effect;
    }

    @Override
    public void lunararc$setEffect(boolean effect) {
        lunararc$effect = effect;
        ((LightningBolt) (Object) this).setVisualOnly(effect);
    }

    @Inject(method = "spawnFire", at = @At("HEAD"), cancellable = true, require = 0)
    private void lunararc$skipEffectFire(int attempts, CallbackInfo ci) {
        if (lunararc$effect) ci.cancel();
    }

    @Inject(method = "powerLightningRod", at = @At("HEAD"), cancellable = true, require = 0)
    private void lunararc$skipEffectRod(CallbackInfo ci) {
        if (lunararc$effect) ci.cancel();
    }
}
