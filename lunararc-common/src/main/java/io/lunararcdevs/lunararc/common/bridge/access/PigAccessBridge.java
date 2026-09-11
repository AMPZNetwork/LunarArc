package io.lunararcdevs.lunararc.common.bridge.access;

import net.minecraft.world.entity.ItemBasedSteering;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface PigAccessBridge {
    ItemBasedSteering lunararc$getSteering();
}
