package io.lunararcdevs.lunararc.common.mod.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class LunarArcInventories {

    private LunarArcInventories() {
    }

    /** The Bukkit inventory a container belongs to, or null when none can be named for it. */
    public static Inventory ownerInventory(Container container) {
        InventoryHolder owner = owner(container);
        return owner != null ? owner.getInventory() : null;
    }

    /** The Bukkit block state that owns this container, or null when nothing does. */
    public static InventoryHolder owner(Container container) {
        if (!(container instanceof BlockEntity blockEntity)) return null;
        if (!(blockEntity.getLevel() instanceof ServerLevel level)) return null;
        // A level a mod is simulating has no world a plugin has seen, so there is no owner to name.
        if (!LunarArcLogicWorlds.isLogicWorld(level)) return null;

        BlockPos position = blockEntity.getBlockPos();
        // getState(false) rather than getState(): a plain getState() is a snapshot copy, and an
        // event carrying a copy is an event whose setItem does nothing. Paper asks for the same
        // thing at the same point, as getOwner(false).
        org.bukkit.block.BlockState state = CraftBlock.at(level, position).getState(false);
        return state instanceof InventoryHolder holder ? holder : null;
    }
}
