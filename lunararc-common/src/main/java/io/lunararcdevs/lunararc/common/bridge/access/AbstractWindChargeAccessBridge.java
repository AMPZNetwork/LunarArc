package io.lunararcdevs.lunararc.common.bridge.access;

import net.minecraft.world.phys.Vec3;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface AbstractWindChargeAccessBridge {
    void lunararc$invokeExplode(Vec3 position);
}
