package io.lunararcdevs.lunararc.common.mixin.core.recipe;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Read-only CraftBukkit view of vanilla 1.21.1 smithing-trim state. */
@Mixin(SmithingTrimRecipe.class)
public interface SmithingTrimRecipeAccessor extends io.lunararcdevs.lunararc.common.bridge.access.SmithingTrimRecipeAccessBridge {
    @Accessor("template") Ingredient lunararc$template();
    @Accessor("base") Ingredient lunararc$base();
    @Accessor("addition") Ingredient lunararc$addition();
}
