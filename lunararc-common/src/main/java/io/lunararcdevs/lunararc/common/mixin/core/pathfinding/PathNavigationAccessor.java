package io.lunararcdevs.lunararc.common.mixin.core.pathfinding;

import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.PathFinder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PathNavigation.class)
public interface PathNavigationAccessor extends io.lunararcdevs.lunararc.common.bridge.access.PathNavigationAccessBridge {
    @Accessor("pathFinder")
    PathFinder lunararc$getPathFinder();
}
