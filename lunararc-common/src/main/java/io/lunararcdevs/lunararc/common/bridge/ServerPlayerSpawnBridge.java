package io.lunararcdevs.lunararc.common.bridge;

/**
 * Narrow bridge for carrying Bukkit's spawn-change cause into the loader-owned
 * ServerPlayer#setRespawnPosition call. This is deliberately state-only; NMS
 * remains responsible for storing and validating the actual respawn point.
 */
public interface ServerPlayerSpawnBridge {
    void lunararc$pushSpawnChangeCause(org.bukkit.event.player.PlayerSpawnChangeEvent.Cause cause);
}
