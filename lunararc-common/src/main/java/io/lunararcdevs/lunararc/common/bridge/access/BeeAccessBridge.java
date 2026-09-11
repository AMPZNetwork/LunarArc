package io.lunararcdevs.lunararc.common.bridge.access;

import net.minecraft.core.BlockPos;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface BeeAccessBridge {
    BlockPos lunararc$getHivePos();
    void lunararc$setHivePos(BlockPos pos);
    int lunararc$getStayOutOfHiveCountdown();
    int lunararc$getCropsGrown();
    void lunararc$setCropsGrown(int value);
    int lunararc$getTicksSincePollination();
    void lunararc$setTicksSincePollination(int value);
    int lunararc$getTimeSinceSting();
    void lunararc$setTimeSinceSting(int value);
}
