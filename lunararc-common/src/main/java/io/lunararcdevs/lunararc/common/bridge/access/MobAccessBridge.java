package io.lunararcdevs.lunararc.common.bridge.access;

import net.minecraft.sounds.SoundEvent;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface MobAccessBridge {
    void lunararc$setPersistenceRequired(boolean value);
    float lunararc$getBodyArmorDropChance();
    void lunararc$setBodyArmorDropChance(float value);
    SoundEvent lunararc$invokeGetAmbientSound();
}
