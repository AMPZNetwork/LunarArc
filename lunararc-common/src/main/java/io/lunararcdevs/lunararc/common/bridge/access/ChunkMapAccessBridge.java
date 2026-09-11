package io.lunararcdevs.lunararc.common.bridge.access;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;

/** Runtime-safe access to loader-owned ChunkMap state. */
public interface ChunkMapAccessBridge {
    Long2ObjectLinkedOpenHashMap<ChunkHolder> lunararc$getVisibleChunkMap();

    // CraftBukkit reads ChunkMap.serverViewDistance and ChunkMap.entityMap directly, which it can
    // only do because it is access-transformed into the server jar. LunarArc has no access
    // transformer, so on a loader runtime those reads are an IllegalAccessError - serverViewDistance
    // is private and entityMap is package-private, and org.bukkit.craftbukkit is neither.
    // ChunkMap.TrackedEntity itself is public, so only the field reads need routing through here.
    int lunararc$getServerViewDistance();

    Int2ObjectMap<ChunkMap.TrackedEntity> lunararc$getEntityMap();
}
