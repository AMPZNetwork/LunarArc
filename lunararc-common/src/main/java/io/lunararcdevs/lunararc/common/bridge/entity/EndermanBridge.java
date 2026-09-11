package io.lunararcdevs.lunararc.common.bridge.entity;

import net.minecraft.world.entity.Entity;

/** Narrow access to non-public vanilla Enderman operations/state. */
public interface EndermanBridge {
    boolean lunararc$teleportRandomly();
    boolean lunararc$teleportTowards(Entity entity);
    boolean lunararc$isCreepy();
    void lunararc$setCreepy(boolean creepy);
    boolean lunararc$hasBeenStaredAt();
    void lunararc$setHasBeenStaredAt(boolean staredAt);
}
