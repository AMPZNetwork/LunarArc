package org.bukkit.craftbukkit.inventory;

import io.lunararcdevs.lunararc.common.LunarArcServerAccess;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ComplexRecipe;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/** Concrete Bukkit view of an imperative/custom NMS recipe. */
public final class CraftComplexRecipe implements ComplexRecipe {
    private final RecipeHolder<?> handle;
    private final NamespacedKey key;

    public CraftComplexRecipe(RecipeHolder<?> handle) {
        this.handle = Objects.requireNonNull(handle, "handle");
        this.key = CraftRecipeAdapter.toBukkit(handle.id());
    }

    public RecipeHolder<?> getHandle() {
        return this.handle;
    }

    @Override
    public @NotNull ItemStack getResult() {
        return CraftItemStack.asBukkitCopy(this.handle.value().getResultItem(
                LunarArcServerAccess.getMinecraftServer().registryAccess()));
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return this.key;
    }

    @Override
    public String toString() {
        return "CraftComplexRecipe[" + this.key + ", " + this.handle.value().getClass().getName() + "]";
    }
}
