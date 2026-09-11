package io.lunararcdevs.lunararc.common.bridge;

/**
 * Paper/Bukkit compatibility state attached directly to the real 1.21.1
 * player inventory. This is deliberately a narrow mixin bridge, not a
 * platform dispatcher.
 */
public interface PlayerInventoryBridge {
    int lunararc$getMaxStackSize();
    void lunararc$setMaxStackSize(int size);
}
