package org.bukkit.craftbukkit;

import java.util.Objects;
import org.bukkit.HeightMap;

public final class CraftHeightMap {
    private CraftHeightMap() {}

    public static net.minecraft.world.level.levelgen.Heightmap.Types toNMS(HeightMap map) {
        return switch (Objects.requireNonNull(map, "map")) {
            case WORLD_SURFACE_WG -> net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG;
            case WORLD_SURFACE -> net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE;
            case OCEAN_FLOOR_WG -> net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG;
            case OCEAN_FLOOR -> net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR;
            case MOTION_BLOCKING -> net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING;
            case MOTION_BLOCKING_NO_LEAVES -> net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
        };
    }

    public static HeightMap fromNMS(net.minecraft.world.level.levelgen.Heightmap.Types type) {
        return switch (Objects.requireNonNull(type, "type")) {
            case WORLD_SURFACE_WG -> HeightMap.WORLD_SURFACE_WG;
            case WORLD_SURFACE -> HeightMap.WORLD_SURFACE;
            case OCEAN_FLOOR_WG -> HeightMap.OCEAN_FLOOR_WG;
            case OCEAN_FLOOR -> HeightMap.OCEAN_FLOOR;
            case MOTION_BLOCKING -> HeightMap.MOTION_BLOCKING;
            case MOTION_BLOCKING_NO_LEAVES -> HeightMap.MOTION_BLOCKING_NO_LEAVES;
        };
    }
}
