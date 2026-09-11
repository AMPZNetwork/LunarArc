package io.lunararcdevs.lunararc.common.mixin.core.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Read-only CraftBukkit view of vanilla 1.21.1 smithing-transform state. */
@Mixin(SmithingTransformRecipe.class)
public interface SmithingTransformRecipeAccessor extends io.lunararcdevs.lunararc.common.bridge.access.SmithingTransformRecipeAccessBridge {
    @Accessor("template") Ingredient lunararc$template();
    @Accessor("base") Ingredient lunararc$base();
    @Accessor("addition") Ingredient lunararc$addition();
    @Accessor("result") ItemStack lunararc$result();
}
