package io.lunararcdevs.lunararc.common.bridge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

public interface FishingHookBridge {
    int lunararc$getMinWaitTime(); void lunararc$setMinWaitTime(int value);
    int lunararc$getMaxWaitTime(); void lunararc$setMaxWaitTime(int value);
    int lunararc$getMinLureTime(); void lunararc$setMinLureTime(int value);
    int lunararc$getMaxLureTime(); void lunararc$setMaxLureTime(int value);
    float lunararc$getMinLureAngle(); void lunararc$setMinLureAngle(float value);
    float lunararc$getMaxLureAngle(); void lunararc$setMaxLureAngle(float value);
    boolean lunararc$isApplyLure(); void lunararc$setApplyLure(boolean value);
    boolean lunararc$isRainInfluenced(); void lunararc$setRainInfluenced(boolean value);
    boolean lunararc$isSkyInfluenced(); void lunararc$setSkyInfluenced(boolean value);
    double lunararc$getBiteChance(); void lunararc$setBiteChance(double value);
    int lunararc$getOutOfWaterTime();
    Entity lunararc$getHookedIn(); void lunararc$setHookedIn(Entity entity);
    int lunararc$getStateOrdinal();
    int lunararc$getTimeUntilLured(); void lunararc$setTimeUntilLured(int value);
    int lunararc$getTimeUntilHooked(); void lunararc$setTimeUntilHooked(int value);
    void lunararc$resetFishingState();
    boolean lunararc$calculateOpenWater(BlockPos pos);
    void lunararc$pullEntity(Entity entity);
}
