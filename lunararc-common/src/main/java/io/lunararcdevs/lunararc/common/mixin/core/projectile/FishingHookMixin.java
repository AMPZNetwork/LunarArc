package io.lunararcdevs.lunararc.common.mixin.core.projectile;

import io.lunararcdevs.lunararc.common.bridge.FishingHookBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;
import net.minecraft.tags.FluidTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin implements FishingHookBridge {
    @Shadow private int outOfWaterTime;
    @Shadow private int timeUntilLured;
    @Shadow private int timeUntilHooked;
    @Shadow private float fishAngle;
    @Shadow private Entity hookedIn;
    @Shadow @Final private int lureSpeed;

    @Unique private int lunararc$minWaitTime = 100;
    @Unique private int lunararc$maxWaitTime = 600;
    @Unique private int lunararc$minLureTime = 20;
    @Unique private int lunararc$maxLureTime = 80;
    @Unique private float lunararc$minLureAngle = 0.0F;
    @Unique private float lunararc$maxLureAngle = 360.0F;
    @Unique private boolean lunararc$applyLure = true;
    @Unique private boolean lunararc$rainInfluenced = true;
    @Unique private boolean lunararc$skyInfluenced = true;
    @Unique private double lunararc$biteChance = -1.0D;

    @Invoker("setHookedEntity") protected abstract void lunararc$invokeSetHookedEntity(Entity entity);
    @Invoker("calculateOpenWater") protected abstract boolean lunararc$invokeCalculateOpenWater(BlockPos pos);
    @Invoker("pullEntity") protected abstract void lunararc$invokePullEntity(Entity entity);

    @Redirect(method="catchingFish", at=@At(value="INVOKE", target="Lnet/minecraft/world/level/Level;isRainingAt(Lnet/minecraft/core/BlockPos;)Z"), require=0)
    private boolean lunararc$rainInfluence(Level level, BlockPos pos) { return this.lunararc$rainInfluenced && level.isRainingAt(pos); }

    @Redirect(method="catchingFish", at=@At(value="INVOKE", target="Lnet/minecraft/world/level/Level;canSeeSky(Lnet/minecraft/core/BlockPos;)Z"), require=0)
    private boolean lunararc$skyInfluence(Level level, BlockPos pos) { return !this.lunararc$skyInfluenced || level.canSeeSky(pos); }

    @Redirect(method="catchingFish", at=@At(value="INVOKE", target="Lnet/minecraft/util/Mth;nextInt(Lnet/minecraft/util/RandomSource;II)I", ordinal=1), require=0)
    private int lunararc$lureRange(RandomSource random, int min, int max) { return Mth.nextInt(random, this.lunararc$minLureTime, this.lunararc$maxLureTime); }

    @Redirect(method="catchingFish", at=@At(value="INVOKE", target="Lnet/minecraft/util/Mth;nextInt(Lnet/minecraft/util/RandomSource;II)I", ordinal=2), require=0)
    private int lunararc$waitRange(RandomSource random, int min, int max) {
        int value = Mth.nextInt(random, this.lunararc$minWaitTime, this.lunararc$maxWaitTime);
        return this.lunararc$applyLure ? value : value + this.lureSpeed;
    }

    @Redirect(method="catchingFish", at=@At(value="INVOKE", target="Lnet/minecraft/util/Mth;nextFloat(Lnet/minecraft/util/RandomSource;FF)F", ordinal=2), require=0)
    private float lunararc$lureAngle(RandomSource random, float min, float max) { return Mth.nextFloat(random, this.lunararc$minLureAngle, this.lunararc$maxLureAngle); }

    @Override public int lunararc$getMinWaitTime(){return lunararc$minWaitTime;} @Override public void lunararc$setMinWaitTime(int v){lunararc$minWaitTime=v;}
    @Override public int lunararc$getMaxWaitTime(){return lunararc$maxWaitTime;} @Override public void lunararc$setMaxWaitTime(int v){lunararc$maxWaitTime=v;}
    @Override public int lunararc$getMinLureTime(){return lunararc$minLureTime;} @Override public void lunararc$setMinLureTime(int v){lunararc$minLureTime=v;}
    @Override public int lunararc$getMaxLureTime(){return lunararc$maxLureTime;} @Override public void lunararc$setMaxLureTime(int v){lunararc$maxLureTime=v;}
    @Override public float lunararc$getMinLureAngle(){return lunararc$minLureAngle;} @Override public void lunararc$setMinLureAngle(float v){lunararc$minLureAngle=v;}
    @Override public float lunararc$getMaxLureAngle(){return lunararc$maxLureAngle;} @Override public void lunararc$setMaxLureAngle(float v){lunararc$maxLureAngle=v;}
    @Override public boolean lunararc$isApplyLure(){return lunararc$applyLure;} @Override public void lunararc$setApplyLure(boolean v){lunararc$applyLure=v;}
    @Override public boolean lunararc$isRainInfluenced(){return lunararc$rainInfluenced;} @Override public void lunararc$setRainInfluenced(boolean v){lunararc$rainInfluenced=v;}
    @Override public boolean lunararc$isSkyInfluenced(){return lunararc$skyInfluenced;} @Override public void lunararc$setSkyInfluenced(boolean v){lunararc$skyInfluenced=v;}
    @Override public double lunararc$getBiteChance(){return lunararc$biteChance;} @Override public void lunararc$setBiteChance(double v){lunararc$biteChance=v;}
    @Override public int lunararc$getOutOfWaterTime(){return outOfWaterTime;}
    @Override public Entity lunararc$getHookedIn(){return hookedIn;} @Override public void lunararc$setHookedIn(Entity e){lunararc$invokeSetHookedEntity(e);}
    @Override public int lunararc$getStateOrdinal(){ FishingHook hook=(FishingHook)(Object)this; if(hookedIn!=null)return 1; return hook.level().getFluidState(hook.blockPosition()).is(FluidTags.WATER)?2:0; }
    @Override public int lunararc$getTimeUntilLured(){return timeUntilLured;} @Override public void lunararc$setTimeUntilLured(int v){timeUntilLured=v;}
    @Override public int lunararc$getTimeUntilHooked(){return timeUntilHooked;} @Override public void lunararc$setTimeUntilHooked(int v){timeUntilHooked=v;}
    @Override public void lunararc$resetFishingState(){timeUntilLured=0; timeUntilHooked=0;}
    @Override public boolean lunararc$calculateOpenWater(BlockPos p){return lunararc$invokeCalculateOpenWater(p);}
    @Override public void lunararc$pullEntity(Entity e){lunararc$invokePullEntity(e);}
}
