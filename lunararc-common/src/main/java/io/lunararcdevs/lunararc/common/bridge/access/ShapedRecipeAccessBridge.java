package io.lunararcdevs.lunararc.common.bridge.access;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface ShapedRecipeAccessBridge {
    ShapedRecipePattern lunararc$pattern();
    ItemStack lunararc$result();
    String lunararc$group();
    CraftingBookCategory lunararc$category();
}
