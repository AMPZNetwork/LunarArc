package io.lunararcdevs.lunararc.common.bridge;

import net.kyori.adventure.util.TriState;


public interface ItemEntityBridge {
    boolean lunararc$canMobPickup();
    void lunararc$setCanMobPickup(boolean value);
    TriState lunararc$getFrictionState();
    void lunararc$setFrictionState(TriState state);
}
