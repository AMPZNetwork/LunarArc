package io.lunararcdevs.lunararc.common.bridge.access;

import net.minecraft.core.BlockPos;
import net.minecraft.world.SimpleContainer;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface AllayAccessBridge {
    SimpleContainer lunararc$getInventory();
    BlockPos lunararc$getJukeboxPos();
    void lunararc$setJukeboxPos(BlockPos value);
    long lunararc$getDuplicationCooldown();
    void lunararc$setDuplicationCooldown(long value);
}
