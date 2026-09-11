package io.lunararcdevs.lunararc.common.mixin.core.world;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.lunararcdevs.lunararc.common.LunarArcServerAccess;
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
public abstract class LevelMixin implements io.lunararcdevs.lunararc.common.bridge.LevelBridge {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("LunarArc/LevelMixin");

    /**
     * Real Paper's anti-xray reveal trigger, verified against
     * {@code patches/server/0992-Anti-Xray.patch}'s {@code Level#setBlock} hook - see
     * {@link io.lunararcdevs.lunararc.common.server.LunarArcAntiXrayEngine#onBlockChange} for the
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
            io.lunararcdevs.lunararc.common.server.LunarArcAntiXrayEngine.forLevel((ServerLevel) level)
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

        if (org.bukkit.event.block.BlockPhysicsEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return;
        }

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