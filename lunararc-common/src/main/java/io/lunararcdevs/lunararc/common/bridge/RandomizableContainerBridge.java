package io.lunararcdevs.lunararc.common.bridge;

import com.destroystokyo.paper.loottable.LootableInventory;
import com.destroystokyo.paper.loottable.PaperLootableInventoryData;

/**
 * Exposes {@code RandomizableContainerMixin}'s injected {@code lootableData()} and
 * {@code getLootableInventory()} to plain Java source files that hold a real
 * {@code net.minecraft.world.RandomizableContainer} reference. Cast then call.
 */
public interface RandomizableContainerBridge {
    PaperLootableInventoryData lootableData();
    LootableInventory getLootableInventory();
}
