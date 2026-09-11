package io.lunararcdevs.lunararc.common.bridge.entity;

/** Narrow mutable access to the real NMS ItemBasedSteering state used by Bukkit rideable APIs. */
public interface ItemBasedSteeringBridge {
    boolean lunararc$isBoosting();
    int lunararc$getBoostTime();
    void lunararc$setBoostTime(int ticks);
    int lunararc$getBoostTimeTotal();
    void lunararc$setBoostTicks(int ticks);
}
