package io.lunararcdevs.lunararc.common.bridge.alchemy;

/** Narrow access to the real 1.21.1 brewing-stand timing/fuel state. */
public interface BrewingStandBridge {
    int lunararc$getBrewTime();
    void lunararc$setBrewTime(int ticks);
    int lunararc$getFuel();
    void lunararc$setFuel(int fuel);
}
