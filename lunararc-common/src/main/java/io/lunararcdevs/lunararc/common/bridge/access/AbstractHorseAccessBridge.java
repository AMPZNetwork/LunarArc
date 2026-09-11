package io.lunararcdevs.lunararc.common.bridge.access;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface AbstractHorseAccessBridge {
    SimpleContainer lunararc$getInventory();
    Container lunararc$getBodyArmorAccess();
    void lunararc$recreateInventory();
}
