package io.lunararcdevs.lunararc.common.bridge;

/** Targeted Paper/CraftBukkit age-lock state attached to the real NMS AgeableMob. */
public interface AgeableMobBridge {
    boolean lunararc$isAgeLocked();
    void lunararc$setAgeLocked(boolean locked);
}
