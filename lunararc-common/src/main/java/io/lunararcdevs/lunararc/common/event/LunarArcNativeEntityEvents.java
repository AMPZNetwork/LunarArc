package io.lunararcdevs.lunararc.common.event;

import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import io.lunararcdevs.lunararc.common.mod.util.LunarArcEntityJoinCapture;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class LunarArcNativeEntityEvents {
    private LunarArcNativeEntityEvents() {}

    public static boolean fireFreshEntityJoin(Entity entity) {
        Cancellable bukkit = LunarArcEntityJoinCapture.matching(entity);
        if (bukkit == null) {
            if (entity instanceof LivingEntity living) {
                CreatureSpawnEvent.SpawnReason reason = ((EntityBridge) (Object) living).lunararc$getSpawnReason();
                if (reason == null) reason = CreatureSpawnEvent.SpawnReason.DEFAULT;
                bukkit = org.bukkit.craftbukkit.event.CraftEventFactory.callCreatureSpawnEvent(living, reason);
            } else {
                bukkit = org.bukkit.craftbukkit.event.CraftEventFactory.callEntitySpawnEvent(entity);
            }
        }
        return bukkit != null && bukkit.isCancelled();
    }

    public static TeleportResult fireNativeTeleport(
            Entity entity,
            double targetX,
            double targetY,
            double targetZ,
            PlayerTeleportEvent.TeleportCause playerCause) {
        org.bukkit.entity.Entity bukkit = ((EntityBridge) (Object) entity).lunararc$getBukkitEntity();
        if (bukkit == null) return TeleportResult.pass(targetX, targetY, targetZ);

        Location from = bukkit.getLocation();
        Location to = new Location(bukkit.getWorld(), targetX, targetY, targetZ, from.getYaw(), from.getPitch());

        if (entity instanceof ServerPlayer && bukkit instanceof Player player) {
            PlayerTeleportEvent event = new PlayerTeleportEvent(
                    player,
                    from,
                    to,
                    playerCause == null ? PlayerTeleportEvent.TeleportCause.UNKNOWN : playerCause);
            org.bukkit.Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled() || event.getTo() == null) return TeleportResult.cancel();
            to = event.getTo();
        } else {
            org.bukkit.event.entity.EntityTeleportEvent event =
                    new org.bukkit.event.entity.EntityTeleportEvent(bukkit, from, to);
            org.bukkit.Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled() || event.getTo() == null) return TeleportResult.cancel();
            to = event.getTo();
        }

        return TeleportResult.pass(to.getX(), to.getY(), to.getZ());
    }

    public record TeleportResult(boolean cancelled, double x, double y, double z) {
        public static TeleportResult pass(double x, double y, double z) {
            return new TeleportResult(false, x, y, z);
        }

        public static TeleportResult cancel() {
            return new TeleportResult(true, 0.0D, 0.0D, 0.0D);
        }
    }
}
