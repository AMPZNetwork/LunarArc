package io.lunararcdevs.lunararc.common.bridge;

/** Narrow bridge for Bukkit PlayerBedLeaveEvent spawn-location semantics. */
public interface ServerPlayerBedBridge {
    void lunararc$setNextBedLeaveShouldSetSpawn(Boolean value);
}
