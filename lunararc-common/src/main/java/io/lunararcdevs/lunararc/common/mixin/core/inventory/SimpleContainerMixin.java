package io.lunararcdevs.lunararc.common.mixin.core.inventory;

import io.lunararcdevs.lunararc.common.bridge.SimpleContainerBridge;
import net.minecraft.world.SimpleContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Stores Bukkit's mutable inventory max-stack value on the real vanilla container.
 * SimpleContainer otherwise inherits Container#getMaxStackSize(), so this concrete
 * method intentionally overrides that inherited default on the mixed class.
 */
@Mixin(SimpleContainer.class)
public abstract class SimpleContainerMixin implements SimpleContainerBridge {
    @Unique private int lunararc$maxStackSize = 99;

    @Override
    public int lunararc$getMaxStackSize() {
        return this.lunararc$maxStackSize;
    }

    @Override
    public void lunararc$setMaxStackSize(int size) {
        if (size < 1 || size > 99) {
            throw new IllegalArgumentException("max stack size must be between 1 and 99");
        }
        this.lunararc$maxStackSize = size;
    }

    public int getMaxStackSize() {
        return this.lunararc$maxStackSize;
    }
}
