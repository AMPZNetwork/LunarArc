package io.lunararcdevs.lunararc.common.bridge.access;

import net.minecraft.world.level.pathfinder.PathFinder;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface PathNavigationAccessBridge {
    PathFinder lunararc$getPathFinder();
}
