package org.bukkit.craftbukkit.entity.memory;

import io.lunararcdevs.lunararc.common.LunarArcServerAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;

import java.util.UUID;

public final class CraftMemoryMapper {
    private CraftMemoryMapper() {}

    public static Object fromNms(Object value) {
        if (value instanceof GlobalPos pos) {
            ServerLevel level = LunarArcServerAccess.getMinecraftServer().getLevel(pos.dimension());
            if (level == null) throw new IllegalStateException("Memory references an unloaded dimension: " + pos.dimension().location());
            CraftWorld world = LunarArcServerAccess.getCraftServer().getCraftWorld(level);
            BlockPos blockPos = pos.pos();
            return new Location(world, blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }
        if (value instanceof BlockPos blockPos) {
            CraftWorld world = LunarArcServerAccess.getCraftServer().getCraftWorld(LunarArcServerAccess.getMinecraftServer().overworld());
            return new Location(world, blockPos.getX(), blockPos.getY(), blockPos.getZ());
        }
        if (value instanceof net.minecraft.world.entity.Entity entity) {
            return ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) entity).lunararc$getBukkitEntity();
        }
        if (value instanceof Long || value instanceof UUID || value instanceof Boolean || value instanceof Integer || value instanceof String) return value;
        return value;
    }

    public static Object toNms(Object value) {
        if (value == null) return null;
        if (value instanceof Location location) {
            if (!(location.getWorld() instanceof CraftWorld world)) {
                throw new IllegalArgumentException("Memory location must reference a CraftWorld");
            }
            return GlobalPos.of(world.getHandle().dimension(), new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        }
        if (value instanceof org.bukkit.craftbukkit.entity.CraftEntity entity) return entity.getHandle();
        if (value instanceof BlockPos || value instanceof net.minecraft.world.entity.Entity) return value;
        if (value instanceof Long || value instanceof UUID || value instanceof Boolean || value instanceof Integer || value instanceof String) return value;
        return value;
    }
}
