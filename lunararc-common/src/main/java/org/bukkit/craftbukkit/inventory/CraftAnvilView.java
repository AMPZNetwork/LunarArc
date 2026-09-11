package org.bukkit.craftbukkit.inventory;

import io.lunararcdevs.lunararc.common.bridge.AnvilMenuBridge;
import io.lunararcdevs.lunararc.common.bridge.access.AnvilMenuAccessBridge;
import net.minecraft.world.inventory.AnvilMenu;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.view.AnvilView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CraftAnvilView extends CraftInventoryView implements AnvilView {
    private final AnvilMenu handle;
    private final CraftAnvilInventory top;

    public CraftAnvilView(org.bukkit.craftbukkit.entity.CraftPlayer player, AnvilMenu handle,
                          net.kyori.adventure.text.Component title) {
        this(player, handle, new CraftAnvilInventory(handle, player), title);
    }

    private CraftAnvilView(org.bukkit.craftbukkit.entity.CraftPlayer player, AnvilMenu handle,
                           CraftAnvilInventory top, net.kyori.adventure.text.Component title) {
        super(player, handle, top, player.getInventory(), org.bukkit.event.inventory.InventoryType.ANVIL, title);
        this.handle = handle;
        this.top = top;
    }

    private AnvilMenuAccessBridge access() { return (AnvilMenuAccessBridge) (Object) this.handle; }
    private AnvilMenuBridge bridge() { return (AnvilMenuBridge) (Object) this.handle; }

    @Override public @NotNull AnvilInventory getTopInventory() { return this.top; }
    @Override public @Nullable String getRenameText() { return access().lunararc$getItemName(); }
    @Override public int getRepairItemCountCost() { return access().lunararc$getRepairItemCountCost(); }
    @Override public int getRepairCost() { return access().lunararc$getCost().get(); }
    @Override public int getMaximumRepairCost() { return bridge().lunararc$getMaximumRepairCost(); }
    @Override public void setRepairItemCountCost(int amount) { access().lunararc$setRepairItemCountCost(Math.max(0, amount)); }
    @Override public void setRepairCost(int cost) { access().lunararc$getCost().set(cost); }
    @Override public void setMaximumRepairCost(int levels) {
        if (levels < 0) throw new IllegalArgumentException("Maximum repair cost must be >= 0");
        bridge().lunararc$setMaximumRepairCost(levels);
    }
    @Override public boolean bypassesEnchantmentLevelRestriction() { return bridge().lunararc$bypassesEnchantmentLevelRestriction(); }
    @Override public void bypassEnchantmentLevelRestriction(boolean value) { bridge().lunararc$setBypassEnchantmentLevelRestriction(value); }
}
