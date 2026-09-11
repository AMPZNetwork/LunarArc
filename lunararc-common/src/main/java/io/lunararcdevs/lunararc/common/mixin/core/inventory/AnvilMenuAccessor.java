package io.lunararcdevs.lunararc.common.mixin.core.inventory;

import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AnvilMenu.class)
public interface AnvilMenuAccessor extends io.lunararcdevs.lunararc.common.bridge.access.AnvilMenuAccessBridge {
    @Accessor("itemName") String lunararc$getItemName();
    @Accessor("repairItemCountCost") int lunararc$getRepairItemCountCost();
    @Accessor("repairItemCountCost") void lunararc$setRepairItemCountCost(int value);
    @Accessor("cost") DataSlot lunararc$getCost();
}
