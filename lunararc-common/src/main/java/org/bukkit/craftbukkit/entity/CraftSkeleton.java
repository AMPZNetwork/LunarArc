package org.bukkit.craftbukkit.entity;

import io.lunararcdevs.lunararc.common.bridge.entity.SkeletonBridge;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Skeleton;

public final class CraftSkeleton extends CraftAbstractSkeleton implements Skeleton {
    public CraftSkeleton(CraftServer server, net.minecraft.world.entity.monster.Skeleton entity) { super(server, entity); }
    @Override public net.minecraft.world.entity.monster.Skeleton getHandle() { return (net.minecraft.world.entity.monster.Skeleton) this.entity; }
    private SkeletonBridge skeletonBridge() { return (SkeletonBridge) (Object) getHandle(); }
    @Override public boolean isConverting() { return getHandle().isFreezeConverting(); }
    @Override public int getConversionTime() {
        if (!isConverting()) throw new IllegalStateException("Entity is not converting");
        return skeletonBridge().lunararc$getConversionTime();
    }
    @Override public void setConversionTime(int ticks) {
        if (ticks < 0) skeletonBridge().lunararc$stopFreezeConversion();
        else skeletonBridge().lunararc$startFreezeConversion(ticks);
    }
    @Override public SkeletonType getSkeletonType() { return SkeletonType.NORMAL; }
    @Override public int inPowderedSnowTime() { return skeletonBridge().lunararc$getInPowderSnowTime(); }
    @Override public String toString() { return "CraftSkeleton"; }
}
