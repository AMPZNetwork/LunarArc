package io.lunararcdevs.lunararc.common.mixin.core.entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Bee;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(Bee.class)
public interface BeeAccessor extends io.lunararcdevs.lunararc.common.bridge.access.BeeAccessBridge {
    @Accessor("hivePos") BlockPos lunararc$getHivePos();
    @Accessor("hivePos") void lunararc$setHivePos(BlockPos pos);
    @Accessor("stayOutOfHiveCountdown") int lunararc$getStayOutOfHiveCountdown();
    @Accessor("numCropsGrownSincePollination") int lunararc$getCropsGrown();
    @Accessor("numCropsGrownSincePollination") void lunararc$setCropsGrown(int value);
    @Accessor("ticksWithoutNectarSinceExitingHive") int lunararc$getTicksSincePollination();
    @Accessor("ticksWithoutNectarSinceExitingHive") void lunararc$setTicksSincePollination(int value);
    @Accessor("timeSinceSting") int lunararc$getTimeSinceSting();
    @Accessor("timeSinceSting") void lunararc$setTimeSinceSting(int value);
}
