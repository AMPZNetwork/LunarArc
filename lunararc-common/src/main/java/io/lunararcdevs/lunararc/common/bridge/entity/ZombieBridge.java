package io.lunararcdevs.lunararc.common.bridge.entity;

/** Narrow Bukkit/Paper access attached directly to the loader-owned NMS Zombie. */
public interface ZombieBridge {
    int lunararc$getConversionTime();
    void lunararc$setConversionTimeDirect(int ticks);
    void lunararc$stopDrowning();
    boolean lunararc$supportsBreakDoorGoal();
    boolean lunararc$shouldBurnInDay();
    void lunararc$setShouldBurnInDay(boolean burn);
}
