package io.lunararcdevs.lunararc.common.mixin.core.inventory;

import io.lunararcdevs.lunararc.common.bridge.PlayerInventoryBridge;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Stores Bukkit's mutable inventory stack limit on the real Minecraft player
 * inventory. Paper 1.21.1 uses 99 as the default container stack ceiling.
 */
@Mixin(Inventory.class)
public abstract class PlayerInventoryMixin implements PlayerInventoryBridge {
    @Unique private int lunararc$maxStackSize = 99;

    @Override
    public int lunararc$getMaxStackSize() {
        return Math.max(((Container) (Object) this).getMaxStackSize(), this.lunararc$maxStackSize);
    }

    @Override
    public void lunararc$setMaxStackSize(int size) {
        this.lunararc$maxStackSize = size;
    }
}
