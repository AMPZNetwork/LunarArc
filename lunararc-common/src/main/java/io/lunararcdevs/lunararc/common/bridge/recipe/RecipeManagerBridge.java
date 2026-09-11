package io.lunararcdevs.lunararc.common.bridge.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Collection;

/**
 * Narrow state hook implemented directly on the loader-owned RecipeManager.
 * This is intentionally not a platform dispatcher: it only exposes the
 * mutable recipe maps CraftBukkit needs to implement its API.
 */
public interface RecipeManagerBridge {
    Collection<RecipeHolder<?>> lunararc$recipes();
    RecipeHolder<?> lunararc$recipe(ResourceLocation id);
    boolean lunararc$addRecipe(RecipeHolder<?> recipe);
    boolean lunararc$removeRecipe(ResourceLocation id);
    void lunararc$clearRecipes();
}
