package com.destroystokyo.paper.loottable;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public interface PaperLootableBlockInventory extends LootableBlockInventory, PaperLootableInventory, PaperLootableBlock {

    /* PaperLootableInventory */
    @Override
    default PaperLootableInventoryData lootableDataForAPI() {
        return Objects.requireNonNull(((io.lunararcdevs.lunararc.common.bridge.RandomizableContainerBridge) this.getRandomizableContainer()).lootableData(), "Can only manage loot tables on tile entities with lootableData");
    }

    /* LootableBlockInventory */
    @Override
    default Block getBlock() {
        final BlockPos position = this.getRandomizableContainer().getBlockPos();
        // Real Paper's CraftBlock.at takes a LevelAccessor and doesn't need this cast; LunarArc's
        // takes a ServerLevel. Safe in practice — loot containers in a running world are always in
        // a ServerLevel — but checked explicitly so a broken assumption gives a clear message
        // rather than a bare ClassCastException.
        if (!(this.getNMSWorld() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            throw new IllegalStateException("Lootable block inventory is not in a server level");
        }
        return CraftBlock.at(serverLevel, position);
    }

}
