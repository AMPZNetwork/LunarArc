package io.lunararcdevs.lunararc.common.mod.util;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Thread-confined bridge between Bukkit's BlockItem placement event and native loader place events.
 * The capture lives for the outer BlockItem#place call so Forge/NeoForge events that fire after
 * placeBlock can reuse the already-fired Bukkit event instead of firing a duplicate.
 */
public final class LunarArcBlockPlaceCapture {
    private LunarArcBlockPlaceCapture() {}

    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();

    public static void capture(ServerPlayer player, BlockPos pos, BlockPlaceEvent event) {
        CURRENT.set(new Context(player.getUUID(), pos.immutable(), event));
    }

    public static BlockPlaceEvent matching(ServerPlayer player, BlockPos pos) {
        Context context = CURRENT.get();
        return context != null && context.playerId.equals(player.getUUID()) && context.pos.equals(pos)
                ? context.event : null;
    }

    public static void clear() {
        CURRENT.remove();
    }

    private record Context(UUID playerId, BlockPos pos, BlockPlaceEvent event) {}
}
