package io.lunararcdevs.lunararc.common.bridge.access;

import net.minecraft.world.level.pathfinder.NodeEvaluator;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface PathFinderAccessBridge {
    NodeEvaluator lunararc$getNodeEvaluator();
}
