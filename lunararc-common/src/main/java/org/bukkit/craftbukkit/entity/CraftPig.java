package org.bukkit.craftbukkit.entity;

import io.lunararcdevs.lunararc.common.bridge.entity.ItemBasedSteeringBridge;
import io.lunararcdevs.lunararc.common.bridge.access.PigAccessBridge;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Pig;

public final class CraftPig extends CraftAnimals implements Pig {
    public CraftPig(CraftServer server, net.minecraft.world.entity.animal.Pig entity) {
        super(server, entity);
    }

    @Override public net.minecraft.world.entity.animal.Pig getHandle() {
        return (net.minecraft.world.entity.animal.Pig) this.entity;
    }

    private net.minecraft.world.entity.ItemBasedSteering steering() {
        return ((PigAccessBridge) (Object) getHandle()).lunararc$getSteering();
    }

    private ItemBasedSteeringBridge steeringBridge() {
        return (ItemBasedSteeringBridge) (Object) steering();
    }

    @Override public boolean hasSaddle() { return getHandle().isSaddled(); }
    @Override public void setSaddle(boolean saddled) { steering().setSaddle(saddled); }

    @Override
    public int getBoostTicks() {
        return steeringBridge().lunararc$isBoosting() ? steeringBridge().lunararc$getBoostTimeTotal() : 0;
    }

    @Override
    public void setBoostTicks(int ticks) {
        if (ticks < 0) throw new IllegalArgumentException("ticks must be >= 0");
        steeringBridge().lunararc$setBoostTicks(ticks);
    }

    @Override
    public int getCurrentBoostTicks() {
        return steeringBridge().lunararc$isBoosting() ? steeringBridge().lunararc$getBoostTime() : 0;
    }

    @Override
    public void setCurrentBoostTicks(int ticks) {
        ItemBasedSteeringBridge steering = steeringBridge();
        if (!steering.lunararc$isBoosting()) return;
        int max = steering.lunararc$getBoostTimeTotal();
        if (ticks < 0 || ticks > max) throw new IllegalArgumentException("boost ticks must be between 0 and " + max);
        steering.lunararc$setBoostTime(ticks);
    }

    @Override public Material getSteerMaterial() { return Material.CARROT_ON_A_STICK; }
    @Override public String toString() { return "CraftPig"; }
}
