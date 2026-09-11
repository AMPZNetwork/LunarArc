package io.lunararcdevs.lunararc.common.mixin.core.entity;

import io.lunararcdevs.lunararc.common.bridge.AgeableMobBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.AgeableMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Paper/CraftBukkit age-lock state on the real loader-owned AgeableMob.
 * Vanilla age remains authoritative; when locked, automatic age movement is
 * restored after the vanilla tick and explicit ageUp calls are suppressed.
 */
@Mixin(AgeableMob.class)
public abstract class AgeableMobMixin implements AgeableMobBridge {
    @Unique private boolean lunararc$ageLocked;
    @Unique private int lunararc$lockedAge;

    @Override public boolean lunararc$isAgeLocked() { return this.lunararc$ageLocked; }
    @Override public void lunararc$setAgeLocked(boolean locked) { this.lunararc$ageLocked = locked; }

    @Inject(method = "aiStep", at = @At("HEAD"), require = 0)
    private void lunararc$captureLockedAge(CallbackInfo ci) {
        if (this.lunararc$ageLocked) {
            this.lunararc$lockedAge = ((AgeableMob) (Object) this).getAge();
        }
    }

    @Inject(method = "aiStep", at = @At("TAIL"), require = 0)
    private void lunararc$restoreLockedAge(CallbackInfo ci) {
        if (this.lunararc$ageLocked) {
            AgeableMob mob = (AgeableMob) (Object) this;
            if (mob.getAge() != this.lunararc$lockedAge) mob.setAge(this.lunararc$lockedAge);
        }
    }

    @Inject(method = "ageUp", at = @At("HEAD"), cancellable = true, require = 0)
    private void lunararc$honorAgeLock(int amount, boolean forced, CallbackInfo ci) {
        if (this.lunararc$ageLocked) ci.cancel();
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"), require = 0)
    private void lunararc$saveAgeLock(CompoundTag tag, CallbackInfo ci) {
        if (this.lunararc$ageLocked) tag.putBoolean("AgeLocked", true);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"), require = 0)
    private void lunararc$loadAgeLock(CompoundTag tag, CallbackInfo ci) {
        this.lunararc$ageLocked = tag.getBoolean("AgeLocked");
    }
}
