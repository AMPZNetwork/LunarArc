package io.lunararcdevs.lunararc.common.mod.util;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.event.block.BlockBreakEvent;

/** Thread-confined bridge between Bukkit's destroyBlock event and native loader break events. */
public final class LunarArcBlockBreakCapture {
    private LunarArcBlockBreakCapture() {}

    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();

    public static void capture(ServerPlayer player, BlockPos pos, BlockBreakEvent event) {
        CURRENT.set(new Context(player.getUUID(), pos.immutable(), event));
    }

    public static BlockBreakEvent matching(ServerPlayer player, BlockPos pos) {
        Context context = CURRENT.get();
        return context != null && context.playerId.equals(player.getUUID()) && context.pos.equals(pos)
                ? context.event : null;
    }

    public static void clear() { CURRENT.remove(); }

    private record Context(UUID playerId, BlockPos pos, BlockBreakEvent event) {}
}
