package io.lunararcdevs.lunararc.common.bridge;

/**
 * Narrow Bukkit compatibility state attached directly to vanilla SimpleContainer instances.
 * No proxy/fallback dispatcher: unsupported Container implementations remain explicit.
 */
public interface SimpleContainerBridge {
    int lunararc$getMaxStackSize();
    void lunararc$setMaxStackSize(int size);
}
