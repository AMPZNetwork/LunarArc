package io.lunararcdevs.lunararc.common.bridge;

public interface ServerPlayerBukkitDataBridge {
    long lunararc$getFirstPlayed();
    void lunararc$setFirstPlayed(long firstPlayed);
    long lunararc$getLastPlayed();
    void lunararc$setLastPlayed(long lastPlayed);
    long lunararc$getLoginTime();
    void lunararc$setLoginTime(long loginTime);
    long lunararc$getLastSaveTime();
    boolean lunararc$hasPlayedBefore();
}
