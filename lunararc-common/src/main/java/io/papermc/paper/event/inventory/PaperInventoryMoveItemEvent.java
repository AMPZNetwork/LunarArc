package io.papermc.paper.event.inventory;

import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PaperInventoryMoveItemEvent extends InventoryMoveItemEvent {
    public boolean calledSetItem;
    public boolean calledGetItem;

    public PaperInventoryMoveItemEvent(Inventory source, ItemStack item, Inventory destination, boolean sourceInitiated) {
        super(source, item, destination, sourceInitiated);
    }

    @Override
    public ItemStack getItem() {
        calledGetItem = true;
        return super.getItem();
    }

    @Override
    public void setItem(ItemStack item) {
        super.setItem(item);
        calledSetItem = true;
    }
}
