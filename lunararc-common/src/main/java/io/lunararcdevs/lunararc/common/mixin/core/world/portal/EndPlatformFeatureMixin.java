package io.lunararcdevs.lunararc.common.mixin.core.world.portal;

import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.EndPlatformFeature;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.util.BlockStateListPopulator;
import org.bukkit.event.world.PortalCreateEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Captures the real End-platform block writes so PortalCreateEvent can cancel them atomically. */
@Mixin(EndPlatformFeature.class)
public abstract class EndPlatformFeatureMixin {
    @Unique private static final ThreadLocal<BlockStateListPopulator> LUNARARC_POPULATOR = new ThreadLocal<>();
    @Unique private static final ThreadLocal<Boolean> LUNARARC_DROP = new ThreadLocal<>();

    @Inject(method = "createEndPlatform", at = @At("HEAD"), require = 0)
    private static void lunararc$beginCapture(ServerLevelAccessor level, BlockPos pos, boolean drop, CallbackInfo ci) {
        LUNARARC_POPULATOR.set(new BlockStateListPopulator(level));
        LUNARARC_DROP.set(drop);
    }

    @Redirect(method = "createEndPlatform",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ServerLevelAccessor;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"),
            require = 0)
    private static BlockState lunararc$capturedGetState(ServerLevelAccessor level, BlockPos pos) {
        BlockStateListPopulator populator = LUNARARC_POPULATOR.get();
        return populator == null ? level.getBlockState(pos) : populator.getBlockState(pos);
    }

    @Redirect(method = "createEndPlatform",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ServerLevelAccessor;destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;)Z"),
            require = 0)
    private static boolean lunararc$deferDestroy(ServerLevelAccessor level, BlockPos pos, boolean drop, Entity entity) {
        // The following setBlock capture records the final AIR/OBSIDIAN result. Actual drops happen only after event acceptance.
        return true;
    }

    @Redirect(method = "createEndPlatform",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ServerLevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"),
            require = 0)
    private static boolean lunararc$captureSetBlock(ServerLevelAccessor level, BlockPos pos, BlockState state, int flags) {
        BlockStateListPopulator populator = LUNARARC_POPULATOR.get();
        return populator == null ? level.setBlock(pos, state, flags) : populator.setBlock(pos, state, flags, 512);
    }

    @Inject(method = "createEndPlatform", at = @At("RETURN"), require = 0)
    private static void lunararc$finishCapture(ServerLevelAccessor level, BlockPos pos, boolean drop, CallbackInfo ci) {
        BlockStateListPopulator populator = LUNARARC_POPULATOR.get();
        LUNARARC_POPULATOR.remove();
        LUNARARC_DROP.remove();
        if (populator == null) return;

        Entity trigger = io.lunararcdevs.lunararc.common.util.LunarArcPortalCapture.endPlatformEntity();
        CraftWorld world = null;
        net.minecraft.server.level.ServerLevel serverLevel = level.getLevel();
        for (org.bukkit.World candidate : Bukkit.getWorlds()) {
            if (candidate instanceof CraftWorld craft && craft.getHandle() == serverLevel) {
                world = craft;
                break;
            }
        }

        boolean accepted = true;
        // createEndPlatform is a real vanilla worldgen Feature, guaranteed to run during chunk
        // generation on worker threads — same class of risk as a real confirmed crash
        // elsewhere on this exact pattern. Unlike the simpler cases, this method must still
        // actually place the captured blocks even when skipping the event (the real
        // setBlock/destroyBlock calls were redirected into the populator earlier in this same
        // method, so skipping placement here would silently break end-platform generation
        // entirely, not just skip a notification). Folding the thread check into the existing
        // "couldn't resolve a Bukkit world" fallback below achieves that correctly — it already
        // defaults to accepted = true and proceeds to place blocks unconditionally.
        if (world != null && trigger != null && org.bukkit.Bukkit.isPrimaryThread()) {
            java.util.List<org.bukkit.block.BlockState> states = populator.getCapturedStates().stream()
                    .map(BlockStateListPopulator.CapturedState::state)
                    .map(state -> (org.bukkit.block.BlockState) state)
                    .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
            org.bukkit.entity.Entity bukkitEntity = ((EntityBridge) trigger).lunararc$getBukkitEntity();
            PortalCreateEvent event = new PortalCreateEvent(states, world, bukkitEntity, PortalCreateEvent.CreateReason.END_PLATFORM);
            Bukkit.getPluginManager().callEvent(event);
            accepted = !event.isCancelled();
        }
        if (!accepted) return;

        if (drop) {
            for (BlockStateListPopulator.CapturedState captured : populator.getCapturedStates()) {
                org.bukkit.block.Block block = captured.state().getBlock();
                level.destroyBlock(new BlockPos(block.getX(), block.getY(), block.getZ()), true, null);
            }
        }
        populator.placeSomeBlocks(null, null);
    }
}
