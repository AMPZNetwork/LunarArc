package io.lunararcdevs.lunararc.common.bridge.world;

import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;

/**
 * Exposes Bukkit persistent data stored directly on the real NMS StructureStart.
 */
public interface StructureStartBridge {
    CraftPersistentDataContainer lunararc$getPersistentDataContainer();
}
