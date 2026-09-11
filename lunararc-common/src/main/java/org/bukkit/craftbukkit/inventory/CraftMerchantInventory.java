package org.bukkit.craftbukkit.inventory;

import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import io.lunararcdevs.lunararc.common.bridge.MerchantBukkitBridge;
import io.lunararcdevs.lunararc.common.bridge.access.MerchantContainerAccessBridge;
import io.lunararcdevs.lunararc.common.bridge.access.MerchantMenuAccessBridge;
import net.minecraft.world.inventory.MerchantMenu;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CraftMerchantInventory extends CraftInventoryView.MenuSlotInventory implements MerchantInventory {
    private final MerchantMenu handle;

    public CraftMerchantInventory(MerchantMenu handle, HumanEntity owner) {
        super(handle, 3, InventoryType.MERCHANT, (org.bukkit.inventory.InventoryHolder) owner);
        this.handle = handle;
    }

    private MerchantContainerAccessBridge container() {
        return (MerchantContainerAccessBridge) (Object) ((MerchantMenuAccessBridge) (Object) this.handle).lunararc$getTradeContainer();
    }

    @Override public int getSelectedRecipeIndex() { return container().lunararc$getSelectionHint(); }
    @Override public @Nullable MerchantRecipe getSelectedRecipe() {
        var offer = container().lunararc$getActiveOffer();
        return offer == null ? null : new CraftMerchantRecipe(offer);
    }

    @Override
    public @NotNull Merchant getMerchant() {
        net.minecraft.world.item.trading.Merchant trader = ((MerchantMenuAccessBridge) (Object) this.handle).lunararc$getTrader();
        if (trader instanceof net.minecraft.world.entity.Entity entity) {
            Object bukkit = ((EntityBridge) entity).lunararc$getBukkitEntity();
            if (bukkit instanceof Merchant merchant) return merchant;
        }
        if (trader instanceof MerchantBukkitBridge bridge) return bridge.lunararc$getBukkitMerchant();
        throw new IllegalStateException("Merchant has no concrete Bukkit adapter: " + trader.getClass().getName());
    }
}
