package io.lunararcdevs.lunararc.common.mixin.core.pathfinding;

import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathFinder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PathFinder.class)
public interface PathFinderAccessor extends io.lunararcdevs.lunararc.common.bridge.access.PathFinderAccessBridge {
    @Accessor("nodeEvaluator")
    NodeEvaluator lunararc$getNodeEvaluator();
}
