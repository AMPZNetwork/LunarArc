package io.papermc.paper.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import io.papermc.paper.math.Position;

public final class MCUtil {
    private MCUtil() {}

    public static BlockPos toBlockPosition(Location location) {
        return new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static BlockPos toBlockPos(Position position) {
        return new BlockPos(position.blockX(), position.blockY(), position.blockZ());
    }

    public static Location toLocation(Level level, BlockPos position) {
        return new Location(io.ampznetwork.lunararc.common.LunarArcServerAccess.getCraftWorld(level),
                position.getX(), position.getY(), position.getZ());
    }
}
