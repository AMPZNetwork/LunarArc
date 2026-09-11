package io.lunararcdevs.lunararc.neoforge.event;

import io.lunararcdevs.lunararc.common.event.LunarArcNativeEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public final class NeoForgeEntityJoinEvents {
    private NeoForgeEntityJoinEvents() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(NeoForgeEntityJoinEvents::onJoin);
    }

    private static void onJoin(EntityJoinLevelEvent neoEvent) {
        if (neoEvent.loadedFromDisk()
                || !(neoEvent.getLevel() instanceof ServerLevel)
                || neoEvent.getEntity() instanceof ServerPlayer) {
            return;
        }
        neoEvent.setCanceled(LunarArcNativeEntityEvents.fireFreshEntityJoin(neoEvent.getEntity()));
    }
}
