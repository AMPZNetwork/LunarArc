package io.lunararcdevs.lunararc.common.mixin.core.entity;

import io.lunararcdevs.lunararc.common.bridge.entity.SkeletonBridge;
import net.minecraft.world.entity.monster.Skeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Skeleton.class)
public abstract class SkeletonMixin implements SkeletonBridge {
    @Shadow private int inPowderSnowTime;
    @Shadow private int conversionTime;
    @Invoker("startFreezeConversion") public abstract void lunararc$invokeStartFreezeConversion(int ticks);

    @Override public int lunararc$getConversionTime() { return this.conversionTime; }
    @Override public int lunararc$getInPowderSnowTime() { return this.inPowderSnowTime; }
    @Override public void lunararc$startFreezeConversion(int ticks) { this.lunararc$invokeStartFreezeConversion(ticks); }
    @Override public void lunararc$stopFreezeConversion() {
        this.conversionTime = -1;
        ((Skeleton) (Object) this).setFreezeConverting(false);
    }
}
