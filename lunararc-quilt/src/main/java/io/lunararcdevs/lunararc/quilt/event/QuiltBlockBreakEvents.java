package io.lunararcdevs.lunararc.quilt.event;

import io.lunararcdevs.lunararc.common.mod.util.LunarArcBlockBreakCapture;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.server.level.ServerPlayer;

public final class QuiltBlockBreakEvents {
    private QuiltBlockBreakEvents() {}

    public static void register() {
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) return true;
            org.bukkit.event.block.BlockBreakEvent bukkit = LunarArcBlockBreakCapture.matching(serverPlayer, pos);
            if (bukkit == null) {
                bukkit = org.bukkit.craftbukkit.event.CraftEventFactory.callBlockBreakEvent(
                        serverPlayer.serverLevel(), pos, serverPlayer);
                LunarArcBlockBreakCapture.capture(serverPlayer, pos, bukkit);
            }
            if (bukkit.isCancelled()) {
                serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket(
                        serverPlayer.serverLevel(), pos));
                LunarArcBlockBreakCapture.clear();
                return false;
            }
            return true;
        });
    }
}
