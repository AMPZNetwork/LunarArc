package io.lunararcdevs.lunararc.common.bridge.access;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface InventoryMenuAccessBridge {
    Player lunararc$getOwner();
    CraftingContainer lunararc$getCraftSlots();
    ResultContainer lunararc$getResultSlots();
}
