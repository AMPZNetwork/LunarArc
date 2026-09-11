package io.lunararcdevs.lunararc.common.mixin.core.world.portal;

import io.lunararcdevs.lunararc.common.bridge.PortalForcerBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalForcer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.util.BlockStateListPopulator;
import org.bukkit.event.world.PortalCreateEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Optional;
import net.minecraft.BlockUtil;
import net.minecraft.core.Direction;

/**
 * Applies Bukkit's per-event portal radii to the real 1.21.1 PortalForcer.
 * No portal search/create algorithm is copied or replaced.
 */
@Mixin(value = PortalForcer.class, priority = 1500)
public abstract class PortalForcerMixin implements PortalForcerBridge {
    @Shadow @Final protected ServerLevel level;
    @Unique private int lunararc$searchRadius = -1;
    @Unique private int lunararc$createRadius = -1;
    @Unique private Entity lunararc$portalEntity;
    @Unique private boolean lunararc$canCreate = true;
    @Unique private BlockStateListPopulator lunararc$populator;

    @Override
    public void lunararc$pushSearchRadius(int searchRadius) {
        this.lunararc$searchRadius = searchRadius;
    }

    @Override
    public void lunararc$pushPortalCreate(Entity entity, int creationRadius, boolean canCreate) {
        this.lunararc$portalEntity = entity;
        this.lunararc$createRadius = creationRadius;
        this.lunararc$canCreate = canCreate;
    }

    @ModifyVariable(method = "findClosestPortalPosition", ordinal = 0,
            at = @At(value = "STORE", ordinal = 0), require = 0)
    private int lunararc$useSearchRadius(int vanillaRadius) {
        return this.lunararc$searchRadius < 0 ? vanillaRadius : this.lunararc$searchRadius;
    }

    @Inject(method = "createPortal", at = @At("HEAD"), cancellable = true, require = 0)
    private void lunararc$suppressPortalCreation(BlockPos pos, Direction.Axis axis,
                                                  CallbackInfoReturnable<Optional<BlockUtil.FoundRectangle>> cir) {
        this.lunararc$populator = null;
        if (!this.lunararc$canCreate) cir.setReturnValue(Optional.empty());
    }

    @Redirect(method = "createPortal",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"),
            require = 0)
    private boolean lunararc$capturePortalBlockUpdate(ServerLevel level, BlockPos pos, BlockState state) {
        if (this.lunararc$populator == null) this.lunararc$populator = new BlockStateListPopulator(level);
        return this.lunararc$populator.setBlock(pos, state, net.minecraft.world.level.block.Block.UPDATE_ALL, 512);
    }

    @Redirect(method = "createPortal",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"),
            require = 0)
    private boolean lunararc$capturePortalBlock(ServerLevel level, BlockPos pos, BlockState state, int flags) {
        if (this.lunararc$populator == null) this.lunararc$populator = new BlockStateListPopulator(level);
        return this.lunararc$populator.setBlock(pos, state, flags, 512);
    }

    @Inject(method = "createPortal", at = @At("RETURN"), cancellable = true, require = 0)
    private void lunararc$portalCreateEvent(BlockPos pos, Direction.Axis axis,
                                             CallbackInfoReturnable<Optional<BlockUtil.FoundRectangle>> cir) {
        BlockStateListPopulator populator = this.lunararc$populator;
        this.lunararc$populator = null;
        if (populator == null) return;

        CraftWorld world = null;
        for (org.bukkit.World candidate : org.bukkit.Bukkit.getWorlds()) {
            if (candidate instanceof CraftWorld craftWorld && craftWorld.getHandle() == this.level) {
                world = craftWorld;
                break;
            }
        }
        if (world == null || !org.bukkit.Bukkit.isPrimaryThread()) {
            // Never swallow loader-owned placement merely because Bukkit has no wrapper yet,
            // or because createPortal is reachable from dimension-travel/worldgen on a worker
            // thread (same class of risk as a real confirmed crash elsewhere on this exact
            // pattern). Either way, the real block placement must still happen — only the
            // Bukkit event is being skipped.
            populator.placeSomeBlocks(null, null);
            return;
        }

        java.util.List<org.bukkit.block.BlockState> states = populator.getCapturedStates().stream()
                .map(BlockStateListPopulator.CapturedState::state)
                .map(state -> (org.bukkit.block.BlockState) state)
                .toList();
        org.bukkit.entity.Entity bukkitEntity = this.lunararc$portalEntity == null ? null
                : ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) this.lunararc$portalEntity).lunararc$getBukkitEntity();
        PortalCreateEvent event = new PortalCreateEvent(
                new java.util.ArrayList<>(states), world, bukkitEntity, PortalCreateEvent.CreateReason.NETHER_PAIR);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(Optional.empty());
            return;
        }
        populator.placeSomeBlocks(null, null);
    }

    @ModifyArg(method = "createPortal", index = 1,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/core/BlockPos;spiralAround(Lnet/minecraft/core/BlockPos;ILnet/minecraft/core/Direction;Lnet/minecraft/core/Direction;)Ljava/lang/Iterable;"),
            require = 0)
    private int lunararc$useCreationRadius(int vanillaRadius) {
        return this.lunararc$createRadius < 0 ? vanillaRadius : this.lunararc$createRadius;
    }
}
