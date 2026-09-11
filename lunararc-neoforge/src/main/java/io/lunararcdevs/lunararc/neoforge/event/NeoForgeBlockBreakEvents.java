package io.lunararcdevs.lunararc.neoforge.event;

import io.lunararcdevs.lunararc.common.mod.util.LunarArcBlockBreakCapture;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

public final class NeoForgeBlockBreakEvents {
    private NeoForgeBlockBreakEvents() {}

    public static void register() { NeoForge.EVENT_BUS.addListener(NeoForgeBlockBreakEvents::onBreak); }

    private static void onBreak(BlockEvent.BreakEvent neoEvent) {
        if (!(neoEvent.getPlayer() instanceof ServerPlayer player)) return;
        org.bukkit.event.block.BlockBreakEvent bukkit = LunarArcBlockBreakCapture.matching(player, neoEvent.getPos());
        if (bukkit == null) {
            bukkit = org.bukkit.craftbukkit.event.CraftEventFactory.callBlockBreakEvent(
                    player.serverLevel(), neoEvent.getPos(), player);
        }
        neoEvent.setCanceled(bukkit.isCancelled());
    }
}
