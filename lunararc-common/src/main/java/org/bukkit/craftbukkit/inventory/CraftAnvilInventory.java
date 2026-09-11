package org.bukkit.craftbukkit.inventory;

import io.lunararcdevs.lunararc.common.bridge.AnvilMenuBridge;
import io.lunararcdevs.lunararc.common.bridge.access.AnvilMenuAccessBridge;
import net.minecraft.world.inventory.AnvilMenu;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.jetbrains.annotations.Nullable;

public final class CraftAnvilInventory extends CraftInventoryView.MenuSlotInventory implements AnvilInventory {
    private final AnvilMenu handle;

    public CraftAnvilInventory(AnvilMenu handle, HumanEntity owner) {
        super(handle, 3, InventoryType.ANVIL, (org.bukkit.inventory.InventoryHolder) owner);
        this.handle = handle;
    }

    private AnvilMenuAccessBridge access() { return (AnvilMenuAccessBridge) (Object) this.handle; }
    private AnvilMenuBridge bridge() { return (AnvilMenuBridge) (Object) this.handle; }

    @Override public @Nullable String getRenameText() { return access().lunararc$getItemName(); }
    @Override public int getRepairCostAmount() { return access().lunararc$getRepairItemCountCost(); }
    @Override public void setRepairCostAmount(int amount) { access().lunararc$setRepairItemCountCost(Math.max(0, amount)); }
    @Override public int getRepairCost() { return access().lunararc$getCost().get(); }
    @Override public void setRepairCost(int levels) { access().lunararc$getCost().set(levels); }
    @Override public int getMaximumRepairCost() { return bridge().lunararc$getMaximumRepairCost(); }
    @Override public void setMaximumRepairCost(int levels) {
        if (levels < 0) throw new IllegalArgumentException("Maximum repair cost must be >= 0");
        bridge().lunararc$setMaximumRepairCost(levels);
    }
}
