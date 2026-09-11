package io.lunararcdevs.lunararc.common.bridge.access;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface SmithingTransformRecipeAccessBridge {
    Ingredient lunararc$template();
    Ingredient lunararc$base();
    Ingredient lunararc$addition();
    ItemStack lunararc$result();
}
