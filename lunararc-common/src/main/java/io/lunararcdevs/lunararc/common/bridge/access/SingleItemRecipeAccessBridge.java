package io.lunararcdevs.lunararc.common.bridge.access;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface SingleItemRecipeAccessBridge {
    String lunararc$group();
    Ingredient lunararc$ingredient();
    ItemStack lunararc$result();
}
