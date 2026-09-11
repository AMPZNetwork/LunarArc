package io.lunararcdevs.lunararc.common.bridge.access;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingRecipe;
import org.jetbrains.annotations.Nullable;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface SmithingMenuAccessBridge {
    @Nullable RecipeHolder<SmithingRecipe> lunararc$getSelectedRecipe();
}
