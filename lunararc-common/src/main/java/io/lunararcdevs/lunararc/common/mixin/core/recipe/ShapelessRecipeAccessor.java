package io.lunararcdevs.lunararc.common.mixin.core.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Read-only CraftBukkit view of vanilla 1.21.1 shapeless-recipe state. */
@Mixin(ShapelessRecipe.class)
public interface ShapelessRecipeAccessor extends io.lunararcdevs.lunararc.common.bridge.access.ShapelessRecipeAccessBridge {
    @Accessor("group") String lunararc$group();
    @Accessor("category") CraftingBookCategory lunararc$category();
    @Accessor("result") ItemStack lunararc$result();
    @Accessor("ingredients") NonNullList<Ingredient> lunararc$ingredients();
}
