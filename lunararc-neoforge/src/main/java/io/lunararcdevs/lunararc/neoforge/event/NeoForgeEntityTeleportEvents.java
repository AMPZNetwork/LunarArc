package io.lunararcdevs.lunararc.neoforge.event;

import io.lunararcdevs.lunararc.common.event.LunarArcNativeEntityEvents;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class NeoForgeEntityTeleportEvents {
    private NeoForgeEntityTeleportEvents() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(NeoForgeEntityTeleportEvents::onTeleport);
    }

    private static void onTeleport(EntityTeleportEvent neoEvent) {
        LunarArcNativeEntityEvents.TeleportResult result = LunarArcNativeEntityEvents.fireNativeTeleport(
                neoEvent.getEntity(),
                neoEvent.getTargetX(),
                neoEvent.getTargetY(),
                neoEvent.getTargetZ(),
                cause(neoEvent));
        if (result.cancelled()) {
            neoEvent.setCanceled(true);
            return;
        }
        neoEvent.setTargetX(result.x());
        neoEvent.setTargetY(result.y());
        neoEvent.setTargetZ(result.z());
    }

    private static PlayerTeleportEvent.TeleportCause cause(EntityTeleportEvent event) {
        if (event instanceof EntityTeleportEvent.EnderPearl) return PlayerTeleportEvent.TeleportCause.ENDER_PEARL;
        if (event instanceof EntityTeleportEvent.ChorusFruit) return PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT;
        if (event instanceof EntityTeleportEvent.TeleportCommand || event instanceof EntityTeleportEvent.SpreadPlayersCommand) {
            return PlayerTeleportEvent.TeleportCause.COMMAND;
        }
        return PlayerTeleportEvent.TeleportCause.UNKNOWN;
    }
}
