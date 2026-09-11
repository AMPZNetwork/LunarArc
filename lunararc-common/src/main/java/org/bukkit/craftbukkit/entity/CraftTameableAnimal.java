package org.bukkit.craftbukkit.entity;

import java.util.UUID;
import net.minecraft.world.entity.TamableAnimal;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Tameable;

public class CraftTameableAnimal extends CraftAnimals implements Tameable, Creature {
    public CraftTameableAnimal(CraftServer server, TamableAnimal entity) { super(server, entity); }

    @Override public TamableAnimal getHandle() { return (TamableAnimal) this.entity; }

    @Override public UUID getOwnerUniqueId() { return getOwnerUUID(); }

    public UUID getOwnerUUID() {
        try {
            return getHandle().getOwnerUUID();
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public void setOwnerUUID(UUID uuid) { getHandle().setOwnerUUID(uuid); }

    @Override
    public AnimalTamer getOwner() {
        UUID ownerId = getOwnerUUID();
        if (ownerId == null) return null;
        AnimalTamer owner = getServer().getPlayer(ownerId);
        return owner != null ? owner : getServer().getOfflinePlayer(ownerId);
    }

    @Override public boolean isTamed() { return getHandle().isTame(); }

    @Override
    public void setOwner(AnimalTamer tamer) {
        if (tamer == null) {
            setTamed(false);
            setOwnerUUID(null);
            return;
        }
        setTamed(true);
        getHandle().setTarget(null);
        setOwnerUUID(tamer.getUniqueId());
    }

    @Override
    public void setTamed(boolean tame) {
        getHandle().setTame(tame, true);
        if (!tame) setOwnerUUID(null);
    }

    public boolean isSitting() { return getHandle().isInSittingPose(); }

    public void setSitting(boolean sitting) {
        getHandle().setInSittingPose(sitting);
        getHandle().setOrderedToSit(sitting);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{owner=" + getOwner() + ",tamed=" + isTamed() + "}";
    }
}
