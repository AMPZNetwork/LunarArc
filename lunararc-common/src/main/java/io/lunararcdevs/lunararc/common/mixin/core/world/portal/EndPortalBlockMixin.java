package io.lunararcdevs.lunararc.common.mixin.core.world.portal;

import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.event.CraftPortalEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** End-portal Bukkit bridge around the real 1.21.1 portal implementation. */
@Mixin(EndPortalBlock.class)
public abstract class EndPortalBlockMixin {

    @Inject(method = "entityInside",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;setAsInsidePortal(Lnet/minecraft/world/level/block/Portal;Lnet/minecraft/core/BlockPos;)V"),
            cancellable = true, require = 0)
    private void lunararc$portalEnter(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (!(level instanceof ServerLevel serverLevel) || !Bukkit.isPrimaryThread()) return;
        CraftWorld world = lunararc$craftWorld(serverLevel);
        if (world == null) return;
        EntityPortalEnterEvent event = new EntityPortalEnterEvent(
                ((EntityBridge) entity).lunararc$getBukkitEntity(),
                new Location(world, pos.getX(), pos.getY(), pos.getZ()), PortalType.ENDER);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) ci.cancel();
    }


    @Inject(method = "getPortalDestination",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/feature/EndPlatformFeature;createEndPlatform(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Z)V"),
            require = 0)
    private void lunararc$captureEndPlatformEntity(ServerLevel targetLevel, Entity entity, BlockPos portalPos,
                                                     CallbackInfoReturnable<DimensionTransition> cir) {
        io.lunararcdevs.lunararc.common.util.LunarArcPortalCapture.pushEndPlatformEntity(entity);
    }

    @Inject(method = "getPortalDestination",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/feature/EndPlatformFeature;createEndPlatform(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Z)V",
                    shift = At.Shift.AFTER),
            require = 0)
    private void lunararc$clearEndPlatformEntity(ServerLevel targetLevel, Entity entity, BlockPos portalPos,
                                                   CallbackInfoReturnable<DimensionTransition> cir) {
        io.lunararcdevs.lunararc.common.util.LunarArcPortalCapture.clearEndPlatformEntity();
    }

    @Inject(method = "getPortalDestination", at = @At("RETURN"), cancellable = true, require = 0)
    private void lunararc$endPortalEvent(ServerLevel targetLevel, Entity entity, BlockPos portalPos,
                                          CallbackInfoReturnable<DimensionTransition> cir) {
        DimensionTransition transition = cir.getReturnValue();
        if (transition == null || !Bukkit.isPrimaryThread()) return;

        // A player leaving The End is a respawn flow in 1.21.1. PlayerRespawnEvent /
        // PlayerPostRespawnEvent own that path; do not double-report it as PlayerPortalEvent.
        if (entity instanceof ServerPlayer && entity.level().dimension() == Level.END) return;

        CraftWorld destination = lunararc$craftWorld(transition.newLevel());
        if (destination == null) return;
        Location exit = new Location(destination,
                transition.pos().x, transition.pos().y, transition.pos().z,
                transition.yRot(), transition.xRot());
        CraftPortalEvent event = CraftEventFactory.callPortalEvent(
                entity, exit, PlayerTeleportEvent.TeleportCause.END_PORTAL, 0, 0);
        if (event == null || event.getTo() == null || !(event.getTo().getWorld() instanceof CraftWorld finalWorld)) {
            cir.setReturnValue(null);
            return;
        }

        Location to = event.getTo();
        cir.setReturnValue(new DimensionTransition(
                finalWorld.getHandle(),
                new net.minecraft.world.phys.Vec3(to.getX(), to.getY(), to.getZ()),
                transition.speed(),
                to.getYaw(),
                to.getPitch(),
                transition.missingRespawnBlock(),
                transition.postDimensionTransition()));
    }

    private static CraftWorld lunararc$craftWorld(ServerLevel level) {
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            if (world instanceof CraftWorld craftWorld && craftWorld.getHandle() == level) return craftWorld;
        }
        return null;
    }
}
