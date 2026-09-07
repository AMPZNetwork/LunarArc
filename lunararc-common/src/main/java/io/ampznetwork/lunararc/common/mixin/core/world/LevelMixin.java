package io.ampznetwork.lunararc.common.mixin.core.world;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.ampznetwork.lunararc.common.LunarArcServerAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.CraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public abstract class LevelMixin implements io.ampznetwork.lunararc.common.bridge.LevelBridge {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("LunarArc/LevelMixin");

    /**
     * Real Paper's anti-xray reveal trigger, verified against
     * {@code patches/server/0992-Anti-Xray.patch}'s {@code Level#setBlock} hook - see
     * {@link io.ampznetwork.lunararc.common.server.LunarArcAntiXrayEngine#onBlockChange} for the
     * actual algorithm. Wraps the whole call rather than a HEAD/RETURN pair so the "before" state
     * lives in a local variable instead of shared per-thread state - a block's own
     * {@code onPlace}/shape-update side effects can synchronously call {@code setBlock} again on the
     * same thread before this call returns, and a local variable naturally gets a fresh copy per
     * call frame where a shared field or ThreadLocal would need its own reentrancy bookkeeping.
     */
    @WrapMethod(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", require = 0)
    private boolean lunararc$antiXrayOnSetBlock(
            BlockPos pos, BlockState newState, int flags, int maxUpdateDepth, Operation<Boolean> original) {
        Level level = (Level) (Object) this;
        BlockState oldState = level instanceof ServerLevel ? level.getBlockState(pos) : null;
        boolean result = original.call(pos, newState, flags, maxUpdateDepth);
        if (oldState != null) {
            io.ampznetwork.lunararc.common.server.LunarArcAntiXrayEngine.forLevel((ServerLevel) level)
                    .onBlockChange((ServerLevel) level, pos, newState, oldState);
        }
        return result;
    }

    @Override
    public org.bukkit.craftbukkit.CraftWorld lunararc$getWorld() {
        return this.getWorld();
    }

    @Override
    public io.papermc.paper.configuration.WorldConfiguration lunararc$getPaperConfiguration() {
        return this.getWorld().getPaperConfiguration();
    }

    public io.papermc.paper.configuration.WorldConfiguration paperConfig() {
        return lunararc$getPaperConfiguration();
    }

    @Override
    public CraftServer lunararc$getCraftServer() {
        return this.getCraftServer();
    }

    /**
     * CraftBukkit's {@code Level.world} field, under its real name.
     *
     * <p>A getter was not enough. ProtocolLib's BukkitConverters resolves the NMS-world to
     * Bukkit-world conversion by scanning Level for a <em>field</em> whose type is CraftWorld, and
     * died in its static initializer with "Unable to find a field with the type CraftWorld in
     * class net.minecraft.world.level.Level" - which took out packet handling for every plugin
     * built on it. CraftBukkit declares the field and assigns it when the world is created, so
     * that is what this does.</p>
     */
    public org.bukkit.craftbukkit.CraftWorld world;

    @Override
    public void lunararc$attachBukkitWorld(org.bukkit.craftbukkit.CraftWorld world) {
        this.world = world;
    }

    public org.bukkit.craftbukkit.CraftWorld getWorld() {
        if (this.world != null) return this.world;
        Level level = (Level) (Object) this;
        if (!(level instanceof ServerLevel serverLevel)) {
            throw new IllegalStateException("Bukkit world requested from a non-server level");
        }
        // Resolving also populates the field, so a Level that was never handed to CraftWorld's
        // constructor still ends up with it set rather than staying null for reflective readers.
        this.world = LunarArcServerAccess.getCraftServer(serverLevel.getServer()).getCraftWorld(serverLevel);
        return this.world;
    }

    public CraftServer getCraftServer() {
        Level level = (Level) (Object) this;
        if (!(level instanceof ServerLevel serverLevel)) {
            throw new IllegalStateException("CraftServer requested from a non-server level");
        }
        return LunarArcServerAccess.getCraftServer(serverLevel.getServer());
    }

    @Inject(
            method = "neighborChanged(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/core/BlockPos;)V",
            at = @At("HEAD"),
            require = 0)
    private void lunararc$onNeighborChanged(
            BlockPos pos,
            net.minecraft.world.level.block.Block block,
            BlockPos fromPos,
            CallbackInfo ci) {
        Level level = (Level) (Object) this;
        if (!(level instanceof ServerLevel serverLevel)) return;

        // BlockPhysicsEvent is the hottest event in the game - every redstone edge, every fluid
        // step, every block update on every loaded chunk arrives here - and this was building a
        // CraftBlock, a CraftBlockData and an event object for each one whether or not a single
        // plugin was listening. On a modded server that is millions of short-lived objects a
        // second, which is enough to keep the collector busy instead of the tick loop; a server
        // in that state stops keeping up with scheduled ticks, and the visible symptom is
        // ordinary things quietly not happening - poured water that never spreads.
        //
        // Real Paper does not pay that cost either. Its "Only process BlockPhysicsEvent if a
        // plugin has a listener" patch caches
        // BlockPhysicsEvent.getHandlerList().getRegisteredListeners().length > 0 into
        // ServerLevel.hasPhysicsEvent once per world per tick and skips the whole block when it
        // is false. Asking the handler list directly is the same question without the staleness:
        // getRegisteredListeners() is a read of an already-baked volatile array, which is
        // nothing beside the allocations it is guarding.
        if (org.bukkit.event.block.BlockPhysicsEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return;
        }

        // Real Paper never lets BlockPhysicsEvent cancel neighborChanged() itself — it only
        // ever cancels vanilla's own shape-update propagation, from directly inside setBlock()
        // (confirmed from real Paper source: Level.java's markAndNotifyBlock, guarded by
        // AsyncCatcher.catchAsync(), the same architectural pattern as the isPrimaryThread()
        // checks added elsewhere this session). neighborChanged() itself is mod-overridable
        // territory — Aether's own portal-frame block almost certainly overrides it to check
        // for portal completion — and cancelling the whole method based on a Bukkit plugin's
        // decision about an unrelated, vanilla-specific concern (e.g. a protection plugin
        // cancelling BlockPhysicsEvent near a claimed region) would block that mod logic
        // entirely. Modloader is the primary source of truth; this Bukkit-facing event is a
        // layer on top and must never override modded neighborChanged() behavior. Firing it
        // for plugin observability only, never as a veto here.
        try {
            CraftServer craftServer = LunarArcServerAccess.getCraftServer(serverLevel.getServer());
            // The source block is what Paper's own call site passes (CraftBlock.at on sourcePos),
            // and it is what a plugin reads as getSourceBlock(). The two-argument constructor
            // reports the changed block as its own source, which is never true here.
            org.bukkit.event.block.BlockPhysicsEvent event =
                    new org.bukkit.event.block.BlockPhysicsEvent(
                            org.bukkit.craftbukkit.block.CraftBlock.at(serverLevel, pos),
                            org.bukkit.craftbukkit.block.data.CraftBlockData.fromData(
                                    serverLevel.getBlockState(pos)),
                            org.bukkit.craftbukkit.block.CraftBlock.at(serverLevel, fromPos));
            craftServer.getPluginManager().callEvent(event);
        } catch (Throwable t) {
            LOGGER.warn("Failed to fire BlockPhysicsEvent for {} at {} — continuing without it", block, pos, t);
        }
    }
}