package io.lunararcdevs.lunararc.common.mixin.core.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin implements io.lunararcdevs.lunararc.common.bridge.LevelChunkBridge {
    @Shadow @Final private Level level;
    @Shadow private boolean loaded;

    @Shadow
    public abstract BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving);

    @Unique
    private boolean lunararc$needsDecoration;

    @Unique
    private final org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer lunararc$persistentData =
            new org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer();

    @Override
    public org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer lunararc$getPersistentDataContainer() {
        return this.lunararc$persistentData;
    }

    public BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving, boolean doPlace) {
        return this.setBlockState(pos, state, isMoving);
    }

    @Inject(method = "<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ProtoChunk;Lnet/minecraft/world/level/chunk/LevelChunk$PostLoadProcessor;)V",
            at = @At("RETURN"), require = 0)
    private void lunararc$markGeneratedChunk(net.minecraft.server.level.ServerLevel level,
                                             net.minecraft.world.level.chunk.ProtoChunk protoChunk,
                                             LevelChunk.PostLoadProcessor postLoadProcessor,
                                             CallbackInfo ci) {
        this.lunararc$needsDecoration = true;
    }

    @Unique
    private org.bukkit.craftbukkit.CraftWorld lunararc$craftWorld() {
        // Read the Bukkit world straight off the Level, the way CraftBukkit's own chunk code
        // does. This used to scan Bukkit.getWorlds() for the one whose handle matched, which
        // allocated a fresh List.copyOf of every world on each call - and this runs once per
        // chunk load and once per unload, so a command that generates terrain in bulk (a
        // random-teleport search walking far, ungenerated chunks) paid for thousands of
        // throwaway lists and linear scans inside a single tick.
        if (!(this.level instanceof net.minecraft.server.level.ServerLevel)) return null;
        return ((io.lunararcdevs.lunararc.common.bridge.LevelBridge) this.level).lunararc$getWorld();
    }

    @Inject(method = "setLoaded", at = @At("HEAD"), require = 0)
    private void lunararc$chunkUnloadEvent(boolean loaded, CallbackInfo ci) {
        // setLoaded is a general vanilla method reachable from chunk generation on worker
        // threads — confirmed real risk, same class of bug as a real crash in
        // LivingEntity.addEffect() during structure population. Skip firing off-thread rather
        // than let PaperEventManager's safety check throw and abort chunk generation.
        if (loaded || !this.loaded || !org.bukkit.Bukkit.isPrimaryThread()) return;
        if (org.bukkit.event.world.ChunkUnloadEvent.getHandlerList().getRegisteredListeners().length == 0) return;
        org.bukkit.craftbukkit.CraftWorld world = lunararc$craftWorld();
        if (world == null) return;
        org.bukkit.Chunk chunk = new org.bukkit.craftbukkit.CraftChunk((LevelChunk) (Object) this, world);
        org.bukkit.event.world.ChunkUnloadEvent event = new org.bukkit.event.world.ChunkUnloadEvent(chunk, true);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
    }

    @Inject(method = "setLoaded", at = @At("TAIL"), require = 0)
    private void lunararc$chunkLoadEvent(boolean loaded, CallbackInfo ci) {
        if (!loaded) return;
        boolean isNewChunk = this.lunararc$needsDecoration;
        // setLoaded is a general vanilla method reachable from chunk generation on worker
        // threads — same class of risk as lunararc$chunkUnloadEvent above. Unlike that one,
        // this method also clears the needsDecoration flag as a real side effect, so that part
        // must always run regardless of thread — only the two event-firing calls below are
        // skipped off-thread, not the whole method, or the flag would never reset and this
        // chunk would incorrectly look "new" again the next time it's loaded on the main thread.
        this.lunararc$needsDecoration = false;
        if (!org.bukkit.Bukkit.isPrimaryThread()) return;

        // Chunk load is about as hot as a server path gets, and a CraftChunk plus a full event
        // dispatch per chunk is not free. Checking for a listener first means a server with
        // nothing watching chunk loads pays two array-length reads instead, which is the same
        // guard the hopper events use and the reason Paper puts one on its own hot events.
        boolean wantsLoad = org.bukkit.event.world.ChunkLoadEvent.getHandlerList()
                .getRegisteredListeners().length > 0;
        boolean wantsPopulate = isNewChunk && org.bukkit.event.world.ChunkPopulateEvent.getHandlerList()
                .getRegisteredListeners().length > 0;
        if (!wantsLoad && !wantsPopulate) return;

        org.bukkit.craftbukkit.CraftWorld world = lunararc$craftWorld();
        if (world == null) return;
        org.bukkit.Chunk chunk = new org.bukkit.craftbukkit.CraftChunk((LevelChunk) (Object) this, world);
        if (wantsLoad) {
            org.bukkit.Bukkit.getPluginManager().callEvent(
                    new org.bukkit.event.world.ChunkLoadEvent(chunk, isNewChunk));
        }

        if (!wantsPopulate) return;

        // Bukkit BlockPopulators are already executed at the real generation-decoration
        // boundary by LunarArcChunkPopulators. The first live LevelChunk load owns only
        // the provenance-sensitive lifecycle events, otherwise populators would run twice.
        org.bukkit.Bukkit.getPluginManager().callEvent(new org.bukkit.event.world.ChunkPopulateEvent(chunk));
    }
}
