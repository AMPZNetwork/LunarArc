package io.lunararcdevs.lunararc.common.bridge.access;

import net.minecraft.world.phys.HitResult;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface ThrownPotionInvokeBridge {
    void lunararc$invokeOnHit(HitResult result);
}
