package io.lunararcdevs.lunararc.common.compat.worldedit;

import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;

/**
 * WorldEdit's PaperweightAdapter passes its own PaperweightAdapter$NoOpWorldLoadListener as the
 * ChunkProgressListener for the throwaway //regen level - it was compiled against Paper's version
 * of this interface, where onStatusChange(ChunkPos, ChunkStatus) has a default no-op body, so it
 * never overrides it. Vanilla's interface keeps that method abstract, so running that class here
 * throws AbstractMethodError the instant the chunk system calls it - not on construction, but
 * later from a background generation-task thread, which kills that task with nothing left to
 * unstick its queue and wedges //regen forever until the watchdog kills the server (confirmed:
 * this reproduces identically with Sable completely removed, ruling that mod out). Real Paper's
 * server never hits this because their ChunkProgressListener default-implements the method;
 * LunarArcIntegratedPatcher retargets WorldEdit's construction of the broken listener to this
 * class instead, which just implements the interface properly.
 */
public final class LunarArcNoOpChunkProgressListener implements ChunkProgressListener {
    @Override public void updateSpawnPos(ChunkPos center) {}
    @Override public void onStatusChange(ChunkPos pos, ChunkStatus status) {}
    @Override public void start() {}
    @Override public void stop() {}
}
