package io.lunararcdevs.lunararc.common.bridge;

import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;

/**
 * Normal runtime surface implemented by the LevelChunk mixin. Chunk-owned Bukkit
 * state must live with the real NMS chunk, not with short-lived CraftChunk wrappers.
 */
public interface LevelChunkBridge {
    CraftPersistentDataContainer lunararc$getPersistentDataContainer();
}
