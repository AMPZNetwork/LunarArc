package org.bukkit.craftbukkit.entity;
import io.lunararcdevs.lunararc.common.bridge.entity.SlimeBridge;
import org.bukkit.craftbukkit.CraftServer;
public class CraftSlime extends CraftMob implements org.bukkit.entity.Slime, org.bukkit.entity.Enemy {
    public CraftSlime(CraftServer server, net.minecraft.world.entity.monster.Slime entity) { super(server, entity); }
    @Override public net.minecraft.world.entity.monster.Slime getHandle() { return (net.minecraft.world.entity.monster.Slime) entity; }
    private SlimeBridge slimeBridge() { return (SlimeBridge)(Object)getHandle(); }
    @Override public int getSize() { return getHandle().getSize(); }
    @Override public void setSize(int size) { if (size < 1) throw new IllegalArgumentException("size must be >= 1"); getHandle().setSize(size, getHandle().isAlive()); }
    @Override public boolean canWander() { return slimeBridge().lunararc$canWander(); }
    @Override public void setWander(boolean canWander) { slimeBridge().lunararc$setCanWander(canWander); }
    @Override public String toString() { return "CraftSlime"; }
}
