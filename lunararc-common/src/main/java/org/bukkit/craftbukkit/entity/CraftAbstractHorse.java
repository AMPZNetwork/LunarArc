package org.bukkit.craftbukkit.entity;

import io.lunararcdevs.lunararc.common.bridge.entity.AbstractHorseBridge;
import io.lunararcdevs.lunararc.common.bridge.access.AbstractHorseAccessBridge;
import java.util.UUID;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftInventoryAbstractHorse;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Horse;
import org.bukkit.inventory.AbstractHorseInventory;

public abstract class CraftAbstractHorse extends CraftAnimals implements org.bukkit.entity.AbstractHorse {
    protected CraftAbstractHorse(CraftServer server, net.minecraft.world.entity.animal.horse.AbstractHorse entity) { super(server, entity); }
    @Override public net.minecraft.world.entity.animal.horse.AbstractHorse getHandle() { return (net.minecraft.world.entity.animal.horse.AbstractHorse) entity; }
    private AbstractHorseBridge horseBridge() { return (AbstractHorseBridge) (Object) getHandle(); }
    private AbstractHorseAccessBridge horseAccessor() { return (AbstractHorseAccessBridge) (Object) getHandle(); }

    @Override public void setVariant(Horse.Variant variant) { throw new UnsupportedOperationException("Horse variant is represented by the concrete entity type"); }
    @Override public int getDomestication() { return getHandle().getTemper(); }
    @Override public void setDomestication(int level) {
        if (level < 0 || level > getMaxDomestication()) throw new IllegalArgumentException("Domestication must be between 0 and max domestication");
        getHandle().setTemper(level);
    }
    @Override public int getMaxDomestication() { return horseBridge().lunararc$getMaxDomestication(); }
    @Override public void setMaxDomestication(int level) {
        if (level <= 0) throw new IllegalArgumentException("Max domestication must be greater than zero");
        horseBridge().lunararc$setMaxDomestication(level);
        if (getDomestication() > level) getHandle().setTemper(level);
    }
    @Override public double getJumpStrength() { return getHandle().getAttributeValue(Attributes.JUMP_STRENGTH); }
    @Override public void setJumpStrength(double strength) {
        if (strength < 0 || strength > 2) throw new IllegalArgumentException("Jump strength must be between 0 and 2");
        getHandle().getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(strength);
    }
    @Override public boolean isTamed() { return getHandle().isTamed(); }
    @Override public void setTamed(boolean tamed) { getHandle().setTamed(tamed); if (!tamed) getHandle().setOwnerUUID(null); }
    @Override public UUID getOwnerUniqueId() { return getHandle().getOwnerUUID(); }
    @Override public AnimalTamer getOwner() {
        UUID id = getOwnerUniqueId();
        if (id == null) return null;
        AnimalTamer online = getServer().getPlayer(id);
        return online != null ? online : getServer().getOfflinePlayer(id);
    }
    @Override public void setOwner(AnimalTamer owner) {
        if (owner == null) { setTamed(false); return; }
        getHandle().setOwnerUUID(owner.getUniqueId());
        getHandle().setTamed(true);
    }
    @Override public boolean isEatingHaystack() { return getHandle().isEating(); }
    @Override public void setEatingHaystack(boolean eating) { getHandle().setEating(eating); }
    @Override public AbstractHorseInventory getInventory() {
        return new CraftInventoryAbstractHorse(horseAccessor().lunararc$getInventory(), horseAccessor().lunararc$getBodyArmorAccess(), this);
    }
    @Override public boolean isEatingGrass() { return getHandle().isEating(); }
    @Override public void setEatingGrass(boolean eating) { getHandle().setEating(eating); }
    @Override public boolean isRearing() { return getHandle().isStanding(); }
    @Override public void setRearing(boolean rearing) { getHandle().setStanding(rearing); }
    @Override public boolean isEating() { return horseBridge().lunararc$isMouthOpen(); }
    @Override public void setEating(boolean eating) { horseBridge().lunararc$setMouthOpen(eating); }

    // CraftBukkit's UUID-level owner accessors, beside the AnimalTamer ones the Bukkit API has.
    public java.util.UUID getOwnerUUID() {
        return this.getHandle().getOwnerUUID();
    }

    public void setOwnerUUID(java.util.UUID uuid) {
        this.getHandle().setOwnerUUID(uuid);
    }
}
