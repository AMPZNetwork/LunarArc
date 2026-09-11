package io.lunararcdevs.lunararc.common.mixin.core.entity;

import io.lunararcdevs.lunararc.common.bridge.entity.ZombieBridge;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public abstract class ZombieMixin implements ZombieBridge {
    @Shadow private int conversionTime;
    @Shadow @Final private static EntityDataAccessor<Boolean> DATA_DROWNED_CONVERSION_ID;
    @Invoker("supportsBreakDoorGoal") public abstract boolean lunararc$invokeSupportsBreakDoorGoal();

    @Unique private boolean lunararc$shouldBurnInDay = true;

    @Override public int lunararc$getConversionTime() { return this.conversionTime; }
    @Override public void lunararc$setConversionTimeDirect(int ticks) { this.conversionTime = ticks; }
    @Override public void lunararc$stopDrowning() {
        this.conversionTime = -1;
        ((Zombie) (Object) this).getEntityData().set(DATA_DROWNED_CONVERSION_ID, false);
    }
    @Override public boolean lunararc$supportsBreakDoorGoal() { return this.lunararc$invokeSupportsBreakDoorGoal(); }
    @Override public boolean lunararc$shouldBurnInDay() { return this.lunararc$shouldBurnInDay; }
    @Override public void lunararc$setShouldBurnInDay(boolean burn) { this.lunararc$shouldBurnInDay = burn; }

    @Inject(method = "isSunSensitive", at = @At("HEAD"), cancellable = true)
    private void lunararc$paperBurnInDay(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(this.lunararc$shouldBurnInDay);
    }
}
