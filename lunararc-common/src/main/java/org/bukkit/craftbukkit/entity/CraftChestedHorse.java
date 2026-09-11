package org.bukkit.craftbukkit.entity;

import io.lunararcdevs.lunararc.common.bridge.access.AbstractHorseAccessBridge;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.ChestedHorse;

public abstract class CraftChestedHorse extends CraftAbstractHorse implements ChestedHorse {
    protected CraftChestedHorse(CraftServer server, AbstractChestedHorse entity) { super(server, entity); }
    @Override public AbstractChestedHorse getHandle() { return (AbstractChestedHorse) entity; }
    @Override public boolean isCarryingChest() { return getHandle().hasChest(); }
    @Override public void setCarryingChest(boolean chest) {
        if (chest == getHandle().hasChest()) return;
        getHandle().setChest(chest);
        ((AbstractHorseAccessBridge)(Object)getHandle()).lunararc$recreateInventory();
    }
}
