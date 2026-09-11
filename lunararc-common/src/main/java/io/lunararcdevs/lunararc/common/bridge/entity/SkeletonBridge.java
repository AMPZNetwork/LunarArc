package io.lunararcdevs.lunararc.common.bridge.entity;

public interface SkeletonBridge {
    int lunararc$getConversionTime();
    int lunararc$getInPowderSnowTime();
    void lunararc$startFreezeConversion(int ticks);
    void lunararc$stopFreezeConversion();
}
