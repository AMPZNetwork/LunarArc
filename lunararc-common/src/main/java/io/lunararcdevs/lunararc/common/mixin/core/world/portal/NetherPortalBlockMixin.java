package io.lunararcdevs.lunararc.common.mixin.core.world.portal;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import io.lunararcdevs.lunararc.common.bridge.PortalForcerBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.portal.DimensionTransition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.event.CraftPortalEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bukkit portal event bridge around the loader-owned 1.21.1 Nether portal path.
 *
 * The event is deliberately fired with the full precision scaled target rather than
 * the already-clamped BlockPos. This carries the Arclight #2132 regression fix into
 * LunarArc from the start. If a plugin changes worlds we also re-read that world's
 * border before continuing with the real NMS portal search.
 */
@Mixin(NetherPortalBlock.class)
public abstract class NetherPortalBlockMixin {
    @Inject(method = "entityInside",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setAsInsidePortal(Lnet/minecraft/world/level/block/Portal;Lnet/minecraft/core/BlockPos;)V"),
            cancellable = true, require = 0)
    private void lunararc$portalEnter(net.minecraft.world.level.block.state.BlockState state, Level level,
                                      BlockPos pos, Entity entity, CallbackInfo ci) {
        org.bukkit.entity.Entity bukkit = ((EntityBridge) entity).lunararc$getBukkitEntity();
        org.bukkit.World world = Bukkit.getWorld(level.dimension().location().toString());
        if (world == null || !Bukkit.isPrimaryThread()) return;
        EntityPortalEnterEvent event = new EntityPortalEnterEvent(bukkit,
                new Location(world, pos.getX(), pos.getY(), pos.getZ()), org.bukkit.PortalType.NETHER);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) ci.cancel();
    }

    @WrapOperation(method = "getPortalDestination",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/NetherPortalBlock;getExitPortal(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/level/border/WorldBorder;)Lnet/minecraft/world/level/portal/DimensionTransition;"),
            require = 0)
    private DimensionTransition lunararc$portalEvent(NetherPortalBlock instance, ServerLevel targetWorld,
                                                       Entity entity, BlockPos entrancePos, BlockPos ignoredClampedTarget,
                                                       boolean destinationIsNether, WorldBorder ignoredBorder,
                                                       Operation<DimensionTransition> original) {
        double scale = DimensionType.getTeleportationScale(entity.level().dimensionType(), targetWorld.dimensionType());
        org.bukkit.World initialBukkitWorld = Bukkit.getWorld(targetWorld.dimension().location().toString());
        if (initialBukkitWorld == null || !Bukkit.isPrimaryThread()) {
            return original.call(instance, targetWorld, entity, entrancePos, ignoredClampedTarget, destinationIsNether, ignoredBorder);
        }

        // Preserve sub-block scaled coordinates for PlayerPortalEvent/EntityPortalEvent (#2132).
        Location scaledTarget = new Location(initialBukkitWorld,
                entity.getX() * scale, entity.getY(), entity.getZ() * scale,
                entity.getYRot(), entity.getXRot());
        int searchRadius = destinationIsNether ? 16 : 128;
        CraftPortalEvent event = CraftEventFactory.callPortalEvent(entity, scaledTarget,
                PlayerTeleportEvent.TeleportCause.NETHER_PORTAL, searchRadius, 16);
        if (event == null || event.getTo() == null || event.getTo().getWorld() == null) return null;

        if (!(event.getTo().getWorld() instanceof CraftWorld destinationWorld)) return null;
        ServerLevel finalWorld = destinationWorld.getHandle();
        // Plugins may redirect the portal to another world: never reuse the old border (#2132).
        WorldBorder finalBorder = finalWorld.getWorldBorder();
        BlockPos finalTarget = finalBorder.clampToBounds(
                event.getTo().getX(), event.getTo().getY(), event.getTo().getZ());

        PortalForcerBridge forcer = (PortalForcerBridge) finalWorld.getPortalForcer();
        forcer.lunararc$pushSearchRadius(event.getSearchRadius());
        forcer.lunararc$pushPortalCreate(entity, event.getCreationRadius(), event.getCanCreatePortal());
        try {
            return original.call(instance, finalWorld, entity, entrancePos, finalTarget, destinationIsNether, finalBorder);
        } finally {
            forcer.lunararc$pushSearchRadius(-1);
            forcer.lunararc$pushPortalCreate(null, -1, true);
        }
    }
}
