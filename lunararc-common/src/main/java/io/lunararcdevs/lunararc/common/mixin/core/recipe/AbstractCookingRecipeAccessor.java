package io.lunararcdevs.lunararc.common.mixin.core.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Read-only CraftBukkit view of vanilla 1.21.1 cooking-recipe state. */
@Mixin(AbstractCookingRecipe.class)
public interface AbstractCookingRecipeAccessor extends io.lunararcdevs.lunararc.common.bridge.access.AbstractCookingRecipeAccessBridge {
    @Accessor("category") CookingBookCategory lunararc$category();
    @Accessor("group") String lunararc$group();
    @Accessor("ingredient") Ingredient lunararc$ingredient();
    @Accessor("result") ItemStack lunararc$result();
    @Accessor("experience") float lunararc$experience();
    @Accessor("cookingTime") int lunararc$cookingTime();
}
