package org.bukkit.craftbukkit.entity;
import io.lunararcdevs.lunararc.common.bridge.entity.BeeBridge;
import io.lunararcdevs.lunararc.common.bridge.access.BeeAccessBridge;
import net.minecraft.core.BlockPos;
import net.kyori.adventure.util.TriState;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
public final class CraftBee extends CraftAnimals implements org.bukkit.entity.Bee {
    public CraftBee(CraftServer server, net.minecraft.world.entity.animal.Bee entity) { super(server, entity); }
    @Override public net.minecraft.world.entity.animal.Bee getHandle() { return (net.minecraft.world.entity.animal.Bee) entity; }
    private BeeAccessBridge a() { return (BeeAccessBridge)(Object)getHandle(); }
    private BeeBridge b() { return (BeeBridge)(Object)getHandle(); }
    private Location loc(BlockPos p) { return p == null ? null : new Location(getWorld(), p.getX(), p.getY(), p.getZ()); }
    private BlockPos pos(Location l, String what) {
        if (l == null) return null;
        if (!getWorld().equals(l.getWorld())) throw new IllegalArgumentException(what + " must be in the same world");
        return BlockPos.containing(l.getX(), l.getY(), l.getZ());
    }
    @Override public Location getHive() { return loc(a().lunararc$getHivePos()); }
    @Override public void setHive(Location location) { a().lunararc$setHivePos(pos(location, "Hive")); }
    @Override public Location getFlower() { return loc(getHandle().getSavedFlowerPos()); }
    @Override public void setFlower(Location location) { getHandle().setSavedFlowerPos(pos(location, "Flower")); }
    @Override public boolean hasNectar() { return getHandle().hasNectar(); }
    @Override public void setHasNectar(boolean nectar) { getHandle().setHasNectar(nectar); }
    @Override public boolean hasStung() { return getHandle().hasStung(); }
    @Override public void setHasStung(boolean stung) { getHandle().setHasStung(stung); }
    @Override public int getAnger() { return getHandle().getRemainingPersistentAngerTime(); }
    @Override public void setAnger(int anger) { getHandle().setRemainingPersistentAngerTime(anger); }
    @Override public int getCannotEnterHiveTicks() { return a().lunararc$getStayOutOfHiveCountdown(); }
    @Override public void setCannotEnterHiveTicks(int ticks) { getHandle().setStayOutOfHiveCountdown(ticks); }
    @Override public void setRollingOverride(TriState rolling) { b().lunararc$setRollingOverride(rolling); }
    @Override public TriState getRollingOverride() { return b().lunararc$getRollingOverride(); }
    @Override public boolean isRolling() { return getRollingOverride().toBooleanOrElse(getHandle().isRolling()); }
    @Override public void setCropsGrownSincePollination(int crops) { a().lunararc$setCropsGrown(crops); }
    @Override public int getCropsGrownSincePollination() { return a().lunararc$getCropsGrown(); }
    @Override public void setTicksSincePollination(int ticks) { a().lunararc$setTicksSincePollination(ticks); }
    @Override public int getTicksSincePollination() { return a().lunararc$getTicksSincePollination(); }
    public void setTimeSinceSting(int time) { if (time < 0) throw new IllegalArgumentException("time must be non-negative"); a().lunararc$setTimeSinceSting(time); }
    public int getTimeSinceSting() { return a().lunararc$getTimeSinceSting(); }
    @Override public String toString() { return "CraftBee"; }
}
