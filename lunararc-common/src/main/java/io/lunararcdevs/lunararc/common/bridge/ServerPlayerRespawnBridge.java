package io.lunararcdevs.lunararc.common.bridge;

import net.minecraft.world.level.portal.DimensionTransition;

/** Narrow vanilla 1.21.1 respawn bridge implemented by ServerPlayerMixin. */
public interface ServerPlayerRespawnBridge {
    DimensionTransition lunararc$findRespawnPositionAndUseSpawnBlock(
            boolean keepEverything,
            DimensionTransition.PostDimensionTransition postTransition);
}
