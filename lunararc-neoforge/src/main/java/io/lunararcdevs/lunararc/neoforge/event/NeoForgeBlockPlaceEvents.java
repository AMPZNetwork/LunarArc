package io.lunararcdevs.lunararc.neoforge.event;

import io.lunararcdevs.lunararc.common.mod.util.LunarArcBlockPlaceCapture;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

public final class NeoForgeBlockPlaceEvents {
    private NeoForgeBlockPlaceEvents() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(NeoForgeBlockPlaceEvents::onPlace);
    }

    private static void onPlace(BlockEvent.EntityPlaceEvent neoEvent) {
        if (!(neoEvent.getEntity() instanceof ServerPlayer player)) return;

        org.bukkit.event.block.BlockPlaceEvent bukkit = LunarArcBlockPlaceCapture.matching(player, neoEvent.getPos());
        if (bukkit == null) {
            net.neoforged.neoforge.common.util.BlockSnapshot snapshot = neoEvent.getBlockSnapshot();
            bukkit = org.bukkit.craftbukkit.event.CraftEventFactory.callBlockPlaceEvent(
                    player.serverLevel(), neoEvent.getPos(), player, InteractionHand.MAIN_HAND,
                    neoEvent.getPlacedBlock(), snapshot.getState(), snapshot.getTag());
        }
        neoEvent.setCanceled(bukkit != null && bukkit.isCancelled());
    }
}
