package org.bukkit.craftbukkit.entity;

import io.lunararcdevs.lunararc.common.bridge.entity.ZombieBridge;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Zombie;

public class CraftZombie extends CraftMonster implements Zombie {
    public CraftZombie(CraftServer server, net.minecraft.world.entity.monster.Zombie entity) { super(server, entity); }
    @Override public net.minecraft.world.entity.monster.Zombie getHandle() { return (net.minecraft.world.entity.monster.Zombie) this.entity; }
    private ZombieBridge zombieBridge() { return (ZombieBridge) (Object) getHandle(); }

    @Override public boolean isBaby() { return getHandle().isBaby(); }
    @Override public void setBaby(boolean baby) { getHandle().setBaby(baby); }
    @Override public void setBaby() { getHandle().setBaby(true); }
    @Override public void setAdult() { getHandle().setBaby(false); }
    @Override public boolean isAdult() { return !getHandle().isBaby(); }
    @Override public int getAge() { return isBaby() ? -1 : 0; }
    @Override public void setAge(int age) { getHandle().setBaby(age < 0); }
    @Override public boolean getAgeLock() { return false; }
    @Override public void setAgeLock(boolean lock) { }
    @Override public boolean canBreed() { return false; }
    @Override public void setBreed(boolean breed) { }

    @Override public boolean isVillager() { return getHandle() instanceof net.minecraft.world.entity.monster.ZombieVillager; }
    @Override public void setVillager(boolean villager) { throw new UnsupportedOperationException("Zombie type conversion is not mutable through this API"); }
    @Override public void setVillagerProfession(Villager.Profession profession) { throw new UnsupportedOperationException("Only ZombieVillager has a villager profession"); }
    @Override public Villager.Profession getVillagerProfession() { return null; }

    @Override public boolean isConverting() { return getHandle().isUnderWaterConverting(); }
    @Override public int getConversionTime() {
        if (!isConverting()) throw new IllegalStateException("Entity not converting");
        return zombieBridge().lunararc$getConversionTime();
    }
    @Override public void setConversionTime(int time) {
        if (time < 0) zombieBridge().lunararc$stopDrowning();
        else getHandle().startUnderWaterConversion(time);
    }

    @Override public boolean isDrowning() { return getHandle().isUnderWaterConverting(); }
    @Override public void startDrowning(int ticks) { getHandle().startUnderWaterConversion(ticks); }
    @Override public void stopDrowning() { zombieBridge().lunararc$stopDrowning(); }
    @Override public boolean shouldBurnInDay() { return zombieBridge().lunararc$shouldBurnInDay(); }
    @Override public void setShouldBurnInDay(boolean burn) { zombieBridge().lunararc$setShouldBurnInDay(burn); }
    @Override public boolean isArmsRaised() { return getHandle().isAggressive(); }
    @Override public void setArmsRaised(boolean raised) { getHandle().setAggressive(raised); }
    @Override public boolean supportsBreakingDoors() { return zombieBridge().lunararc$supportsBreakDoorGoal(); }
    @Override public boolean canBreakDoors() { return getHandle().canBreakDoors(); }
    @Override public void setCanBreakDoors(boolean canBreakDoors) { getHandle().setCanBreakDoors(canBreakDoors); }

    @Override public String toString() { return "CraftZombie"; }
}
