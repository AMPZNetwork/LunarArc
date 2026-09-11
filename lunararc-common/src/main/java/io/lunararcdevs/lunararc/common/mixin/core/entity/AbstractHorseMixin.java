package io.lunararcdevs.lunararc.common.mixin.core.entity;

import io.lunararcdevs.lunararc.common.bridge.entity.AbstractHorseBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseMixin implements AbstractHorseBridge {
    @Unique private static final int LUNARARC_FLAG_OPEN_MOUTH = 64;
    @Unique private int lunararc$maxDomestication = 100;

    @Shadow protected abstract boolean getFlag(int flag);
    @Shadow protected abstract void setFlag(int flag, boolean value);

    @Override public int lunararc$getMaxDomestication() { return lunararc$maxDomestication; }
    @Override public void lunararc$setMaxDomestication(int value) { lunararc$maxDomestication = value; }
    @Override public boolean lunararc$isMouthOpen() { return getFlag(LUNARARC_FLAG_OPEN_MOUTH); }
    @Override public void lunararc$setMouthOpen(boolean value) { setFlag(LUNARARC_FLAG_OPEN_MOUTH, value); }

    @Inject(method = "getMaxTemper", at = @At("HEAD"), cancellable = true)
    private void lunararc$paperMaxDomestication(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(lunararc$maxDomestication);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void lunararc$saveMaxDomestication(CompoundTag tag, CallbackInfo ci) {
        tag.putInt("Bukkit.MaxDomestication", lunararc$maxDomestication);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void lunararc$loadMaxDomestication(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("Bukkit.MaxDomestication")) {
            int value = tag.getInt("Bukkit.MaxDomestication");
            if (value > 0) lunararc$maxDomestication = value;
        }
    }
}
