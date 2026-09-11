package org.bukkit.craftbukkit.entity;

import io.lunararcdevs.lunararc.common.bridge.entity.AbstractSkeletonBridge;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.AbstractSkeleton;
import org.bukkit.entity.Skeleton;

public abstract class CraftAbstractSkeleton extends CraftMonster implements AbstractSkeleton {
    public CraftAbstractSkeleton(CraftServer server, net.minecraft.world.entity.monster.AbstractSkeleton entity) { super(server, entity); }
    @Override public net.minecraft.world.entity.monster.AbstractSkeleton getHandle() { return (net.minecraft.world.entity.monster.AbstractSkeleton) this.entity; }
    private AbstractSkeletonBridge skeletonBridge() { return (AbstractSkeletonBridge) (Object) getHandle(); }
    @Override public void setSkeletonType(Skeleton.SkeletonType type) { throw new UnsupportedOperationException("Skeleton type conversion is not mutable through this API"); }
    @Override public void rangedAttack(org.bukkit.entity.LivingEntity target, float charge) {
        if (!(target instanceof CraftLivingEntity craftTarget)) throw new IllegalArgumentException("target must be backed by LunarArc CraftLivingEntity");
        getHandle().performRangedAttack(craftTarget.getHandle(), charge);
    }
    @Override public void setChargingAttack(boolean charging) { getHandle().setAggressive(charging); }
    @Override public boolean shouldBurnInDay() { return skeletonBridge().lunararc$shouldBurnInDay(); }
    @Override public void setShouldBurnInDay(boolean burn) { skeletonBridge().lunararc$setShouldBurnInDay(burn); }
}
