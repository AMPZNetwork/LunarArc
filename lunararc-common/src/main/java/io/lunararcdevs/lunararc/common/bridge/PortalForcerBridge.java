package io.lunararcdevs.lunararc.common.bridge;

import net.minecraft.world.entity.Entity;

/** Narrow per-call portal search/create state carried into loader-owned PortalForcer logic. */
public interface PortalForcerBridge {
    void lunararc$pushSearchRadius(int searchRadius);
    void lunararc$pushPortalCreate(Entity entity, int creationRadius, boolean canCreate);
}
