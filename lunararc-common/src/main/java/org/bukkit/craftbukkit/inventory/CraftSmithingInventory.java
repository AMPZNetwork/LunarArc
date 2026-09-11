package org.bukkit.craftbukkit.inventory;

import io.lunararcdevs.lunararc.common.bridge.access.SmithingMenuAccessBridge;
import net.minecraft.world.inventory.SmithingMenu;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.SmithingInventory;
import org.jetbrains.annotations.Nullable;

public final class CraftSmithingInventory extends CraftInventoryView.MenuSlotInventory implements SmithingInventory {
    private final SmithingMenu handle;

    public CraftSmithingInventory(SmithingMenu handle, HumanEntity owner) {
        super(handle, 4, InventoryType.SMITHING, (org.bukkit.inventory.InventoryHolder) owner);
        this.handle = handle;
    }

    @Override public @Nullable ItemStack getResult() { return getItem(3); }
    @Override public void setResult(@Nullable ItemStack item) { setItem(3, item); }

    @Override
    public @Nullable Recipe getRecipe() {
        var holder = ((SmithingMenuAccessBridge) (Object) this.handle).lunararc$getSelectedRecipe();
        return holder == null ? null : CraftRecipeAdapter.toBukkit(holder);
    }
}
