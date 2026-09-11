package io.lunararcdevs.lunararc.common.bridge;

import com.destroystokyo.paper.loottable.LootableInventory;
import com.destroystokyo.paper.loottable.PaperLootableInventoryData;

/**
 * Exposes {@code ContainerEntityMixin}'s injected methods to plain Java source files.
 */
public interface ContainerEntityBridge {
    PaperLootableInventoryData lootableData();
    LootableInventory getLootableInventory();
}
