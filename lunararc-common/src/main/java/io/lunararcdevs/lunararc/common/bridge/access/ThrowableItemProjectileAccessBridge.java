package io.lunararcdevs.lunararc.common.bridge.access;

import net.minecraft.world.item.Item;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface ThrowableItemProjectileAccessBridge {
    Item lunararc$getDefaultItem();
}
