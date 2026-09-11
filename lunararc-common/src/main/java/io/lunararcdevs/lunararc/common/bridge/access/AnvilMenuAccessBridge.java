package io.lunararcdevs.lunararc.common.bridge.access;

import net.minecraft.world.inventory.DataSlot;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface AnvilMenuAccessBridge {
    String lunararc$getItemName();
    int lunararc$getRepairItemCountCost();
    void lunararc$setRepairItemCountCost(int value);
    DataSlot lunararc$getCost();
}
