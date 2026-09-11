package io.lunararcdevs.lunararc.common.mixin.core.entity;
import io.lunararcdevs.lunararc.common.bridge.entity.SlimeBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Slime.class)
public abstract class SlimeMixin implements SlimeBridge {
    @Unique private boolean lunararc$canWander = true;
    @Override public boolean lunararc$canWander() { return lunararc$canWander; }
    @Override public void lunararc$setCanWander(boolean value) { lunararc$canWander = value; }
    @Inject(method="addAdditionalSaveData", at=@At("TAIL"))
    private void lunararc$saveWander(CompoundTag tag, CallbackInfo ci) { tag.putBoolean("Paper.canWander", lunararc$canWander); }
    @Inject(method="readAdditionalSaveData", at=@At("TAIL"))
    private void lunararc$loadWander(CompoundTag tag, CallbackInfo ci) { if (tag.contains("Paper.canWander")) lunararc$canWander = tag.getBoolean("Paper.canWander"); }
}
