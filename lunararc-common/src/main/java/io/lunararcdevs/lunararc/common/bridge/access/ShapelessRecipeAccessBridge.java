package io.lunararcdevs.lunararc.common.bridge.access;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface ShapelessRecipeAccessBridge {
    String lunararc$group();
    CraftingBookCategory lunararc$category();
    ItemStack lunararc$result();
    NonNullList<Ingredient> lunararc$ingredients();
}
