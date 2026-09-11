package io.lunararcdevs.lunararc.common.bridge;

import org.bukkit.event.inventory.InventoryCloseEvent;

/** Carries the Bukkit close reason into the loader-owned ServerPlayer close path. */
public interface ServerPlayerInventoryBridge {
    void lunararc$setNextInventoryCloseReason(InventoryCloseEvent.Reason reason);
    int lunararc$nextContainerCounter();
    void lunararc$initMenu(net.minecraft.world.inventory.AbstractContainerMenu menu);
}
