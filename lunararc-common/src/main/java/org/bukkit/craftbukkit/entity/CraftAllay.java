package org.bukkit.craftbukkit.entity;
import io.lunararcdevs.lunararc.common.bridge.ServerLevelBridge;
import io.lunararcdevs.lunararc.common.bridge.entity.AllayBridge;
import io.lunararcdevs.lunararc.common.bridge.access.AllayAccessBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftNMSInventory;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.Inventory;
public final class CraftAllay extends CraftCreature implements org.bukkit.entity.Allay {
    public CraftAllay(CraftServer server, net.minecraft.world.entity.animal.allay.Allay entity) { super(server, entity); }
    @Override public net.minecraft.world.entity.animal.allay.Allay getHandle() { return (net.minecraft.world.entity.animal.allay.Allay) entity; }
    private AllayAccessBridge a() { return (AllayAccessBridge)(Object)getHandle(); }
    private AllayBridge b() { return (AllayBridge)(Object)getHandle(); }
    @Override public Inventory getInventory() { return new CraftNMSInventory(a().lunararc$getInventory(), this); }
    @Override public boolean canDuplicate() { return b().lunararc$canDuplicate(); }
    @Override public void setCanDuplicate(boolean value) { b().lunararc$setCanDuplicate(value); }
    @Override public long getDuplicationCooldown() { return a().lunararc$getDuplicationCooldown(); }
    @Override public void setDuplicationCooldown(long cooldown) { a().lunararc$setDuplicationCooldown(cooldown); }
    @Override public void resetDuplicationCooldown() { b().lunararc$resetDuplicationCooldown(); }
    @Override public boolean isDancing() { return getHandle().isDancing(); }
    @Override public void startDancing(Location location) {
        java.util.Objects.requireNonNull(location, "location");
        if (!getWorld().equals(location.getWorld())) throw new IllegalArgumentException("Jukebox must be in the same world");
        BlockPos pos = BlockPos.containing(location.getX(), location.getY(), location.getZ());
        if (!getHandle().level().getBlockState(pos).is(net.minecraft.world.level.block.Blocks.JUKEBOX)) throw new IllegalArgumentException("Location must contain a jukebox");
        b().lunararc$setForceDancing(false);
        getHandle().setJukeboxPlaying(pos, true);
    }
    @Override public void startDancing() { b().lunararc$setForceDancing(true); getHandle().setDancing(true); }
    @Override public void stopDancing() { b().lunararc$setForceDancing(false); a().lunararc$setJukeboxPos(null); getHandle().setDancing(false); }
    @Override public org.bukkit.entity.Allay duplicateAllay() {
        if (!(getHandle().level() instanceof ServerLevel level)) return null;
        net.minecraft.world.entity.animal.allay.Allay duplicate = EntityType.ALLAY.create(level);
        if (duplicate == null) return null;
        duplicate.moveTo(getHandle().position());
        duplicate.setPersistenceRequired();
        ((AllayBridge)(Object)duplicate).lunararc$resetDuplicationCooldown();
        b().lunararc$resetDuplicationCooldown();
        boolean added = ((ServerLevelBridge)(Object)level).lunararc$addFreshEntity(duplicate, CreatureSpawnEvent.SpawnReason.DUPLICATION);
        return added ? (org.bukkit.entity.Allay) CraftEntity.getEntity(server, duplicate) : null;
    }
    @Override public Location getJukebox() { BlockPos p=a().lunararc$getJukeboxPos(); return p==null?null:new Location(getWorld(),p.getX(),p.getY(),p.getZ()); }
    @Override public String toString() { return "CraftAllay"; }
}
