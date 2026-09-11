package org.bukkit.craftbukkit.entity;

import com.google.common.base.Preconditions;
import io.lunararcdevs.lunararc.common.bridge.FishingHookBridge;
import net.minecraft.core.BlockPos;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.jetbrains.annotations.Nullable;

public final class CraftFishHook extends CraftProjectile implements FishHook {
    public CraftFishHook(CraftServer server, net.minecraft.world.entity.projectile.FishingHook entity) { super(server, entity); }
    private FishingHookBridge hook(){ return (FishingHookBridge)(Object)getHandle(); }
    @Override public int getMinWaitTime(){return hook().lunararc$getMinWaitTime();}
    @Override public void setMinWaitTime(int v){Preconditions.checkArgument(v>=0&&v<=getMaxWaitTime(),"Invalid minimum wait time");hook().lunararc$setMinWaitTime(v);}
    @Override public int getMaxWaitTime(){return hook().lunararc$getMaxWaitTime();}
    @Override public void setMaxWaitTime(int v){Preconditions.checkArgument(v>=0&&v>=getMinWaitTime(),"Invalid maximum wait time");hook().lunararc$setMaxWaitTime(v);}
    @Override public void setWaitTime(int min,int max){Preconditions.checkArgument(min>=0&&max>=min,"Invalid wait range");hook().lunararc$setMinWaitTime(min);hook().lunararc$setMaxWaitTime(max);}
    @Override public int getMinLureTime(){return hook().lunararc$getMinLureTime();}
    @Override public void setMinLureTime(int v){Preconditions.checkArgument(v>=0&&v<=getMaxLureTime(),"Invalid minimum lure time");hook().lunararc$setMinLureTime(v);}
    @Override public int getMaxLureTime(){return hook().lunararc$getMaxLureTime();}
    @Override public void setMaxLureTime(int v){Preconditions.checkArgument(v>=0&&v>=getMinLureTime(),"Invalid maximum lure time");hook().lunararc$setMaxLureTime(v);}
    @Override public void setLureTime(int min,int max){Preconditions.checkArgument(min>=0&&max>=min,"Invalid lure range");hook().lunararc$setMinLureTime(min);hook().lunararc$setMaxLureTime(max);}
    @Override public float getMinLureAngle(){return hook().lunararc$getMinLureAngle();}
    @Override public void setMinLureAngle(float v){Preconditions.checkArgument(v<=getMaxLureAngle(),"Invalid minimum lure angle");hook().lunararc$setMinLureAngle(v);}
    @Override public float getMaxLureAngle(){return hook().lunararc$getMaxLureAngle();}
    @Override public void setMaxLureAngle(float v){Preconditions.checkArgument(v>=getMinLureAngle(),"Invalid maximum lure angle");hook().lunararc$setMaxLureAngle(v);}
    @Override public void setLureAngle(float min,float max){Preconditions.checkArgument(min<=max,"Invalid lure angle range");hook().lunararc$setMinLureAngle(min);hook().lunararc$setMaxLureAngle(max);}
    @Override public boolean isSkyInfluenced(){return hook().lunararc$isSkyInfluenced();} @Override public void setSkyInfluenced(boolean v){hook().lunararc$setSkyInfluenced(v);}
    @Override public boolean isRainInfluenced(){return hook().lunararc$isRainInfluenced();} @Override public void setRainInfluenced(boolean v){hook().lunararc$setRainInfluenced(v);}
    @Override public boolean getApplyLure(){return hook().lunararc$isApplyLure();} @Override public void setApplyLure(boolean v){hook().lunararc$setApplyLure(v);}
    @Override public double getBiteChance(){double v=hook().lunararc$getBiteChance();if(v>=0)return v;return getHandle().level().isRainingAt(BlockPos.containing(getHandle().position()).above())?1/300.0:1/500.0;}
    @Override public void setBiteChance(double v){Preconditions.checkArgument(v>=0&&v<=1,"Bite chance must be between 0 and 1");hook().lunararc$setBiteChance(v);}
    @Override public boolean isInOpenWater(){return hook().lunararc$getOutOfWaterTime()<10&&hook().lunararc$calculateOpenWater(getHandle().blockPosition());}
    @Override public @Nullable Entity getHookedEntity(){net.minecraft.world.entity.Entity e=hook().lunararc$getHookedIn();return e==null?null:CraftEntity.getEntity(server,e);}
    @Override public void setHookedEntity(@Nullable Entity entity){if(entity==null){hook().lunararc$setHookedIn(null);return;}if(!(entity instanceof CraftEntity c))throw new IllegalArgumentException("Entity is not backed by LunarArc");hook().lunararc$setHookedIn(c.getHandle());}
    @Override public boolean pullHookedEntity(){net.minecraft.world.entity.Entity e=hook().lunararc$getHookedIn();if(e==null)return false;hook().lunararc$pullEntity(e);return true;}
    @Override public HookState getState(){return HookState.values()[hook().lunararc$getStateOrdinal()];}
    @Override public int getWaitTime(){return hook().lunararc$getTimeUntilLured();} @Override public void setWaitTime(int v){hook().lunararc$setTimeUntilLured(v);}
    @Override public int getTimeUntilBite(){return hook().lunararc$getTimeUntilHooked();}
    @Override public void setTimeUntilBite(int v){Preconditions.checkArgument(v>=1,"Time until bite must be at least 1");hook().lunararc$setTimeUntilLured(0);hook().lunararc$setTimeUntilHooked(v);}
    @Override public void resetFishingState(){hook().lunararc$resetFishingState();}
    @Override public net.minecraft.world.entity.projectile.FishingHook getHandle(){return (net.minecraft.world.entity.projectile.FishingHook)this.entity;}
}
