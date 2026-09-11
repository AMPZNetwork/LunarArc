package io.lunararcdevs.lunararc.common.bridge;

import net.minecraft.world.item.ItemStack;

/** Per-animal breeding context carried on the real NMS Animal. */
public interface AnimalBridge {
    ItemStack lunararc$getBreedItem();
    void lunararc$setBreedItem(ItemStack item);
}
