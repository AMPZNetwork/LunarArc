package io.lunararcdevs.lunararc.forge.event;

import io.lunararcdevs.lunararc.common.event.LunarArcNativeEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public final class ForgeEntityJoinEvents {
    private ForgeEntityJoinEvents() {}

    public static void register(IEventBus bus) {
        bus.addListener(ForgeEntityJoinEvents::onJoin);
    }

    private static void onJoin(EntityJoinLevelEvent forgeEvent) {
        if (forgeEvent.loadedFromDisk()
                || !(forgeEvent.getLevel() instanceof ServerLevel)
                || forgeEvent.getEntity() instanceof ServerPlayer) {
            return;
        }
        forgeEvent.setCanceled(LunarArcNativeEntityEvents.fireFreshEntityJoin(forgeEvent.getEntity()));
    }
}
