package io.lunararcdevs.lunararc.common.bridge.access;

import net.minecraft.world.item.crafting.Ingredient;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface SmithingTrimRecipeAccessBridge {
    Ingredient lunararc$template();
    Ingredient lunararc$base();
    Ingredient lunararc$addition();
}
