package io.lunararcdevs.lunararc.common.mixin.core.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Read-only CraftBukkit view of vanilla 1.21.1 shaped-recipe state. */
@Mixin(ShapedRecipe.class)
public interface ShapedRecipeAccessor extends io.lunararcdevs.lunararc.common.bridge.access.ShapedRecipeAccessBridge {
    @Accessor("pattern") ShapedRecipePattern lunararc$pattern();
    @Accessor("result") ItemStack lunararc$result();
    @Accessor("group") String lunararc$group();
    @Accessor("category") CraftingBookCategory lunararc$category();
}
