package io.lunararcdevs.lunararc.common.mixin.core.inventory;

import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import org.jetbrains.annotations.Nullable;

@Mixin(SmithingMenu.class)
public interface SmithingMenuAccessor extends io.lunararcdevs.lunararc.common.bridge.access.SmithingMenuAccessBridge {
    @Accessor("selectedRecipe") @Nullable RecipeHolder<SmithingRecipe> lunararc$getSelectedRecipe();
}
