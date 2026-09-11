package io.lunararcdevs.lunararc.common.bridge;

/** Bukkit/Paper fireball state attached directly to the real NMS projectile. */
public interface AbstractHurtingProjectileBridge {
    float lunararc$getBukkitYield();
    void lunararc$setBukkitYield(float yield);
    boolean lunararc$isIncendiary();
    void lunararc$setIncendiary(boolean incendiary);
}
