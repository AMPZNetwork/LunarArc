package io.papermc.paper.entity;

import io.ampznetwork.lunararc.common.bridge.EntityBridge;
import org.bukkit.craftbukkit.entity.CraftEntity;

public interface PaperLeashable extends Leashable {
    net.minecraft.world.entity.Leashable getHandle();

    default boolean isLeashed() {
        return getHandle().getLeashHolder() != null;
    }

    default org.bukkit.entity.Entity getLeashHolder() {
        com.google.common.base.Preconditions.checkState(isLeashed(), "Entity not leashed");
        return ((EntityBridge) getHandle().getLeashHolder()).lunararc$getBukkitEntity();
    }

    private boolean unleash() {
        if (!isLeashed()) return false;
        getHandle().dropLeash(true, false);
        return true;
    }

    default boolean setLeashHolder(org.bukkit.entity.Entity holder) {
        if (getHandle() instanceof EntityBridge entity && !entity.lunararc$isInWorld()) return false;
        if (holder == null) return unleash();
        if (holder.isDead()) return false;
        unleash();
        getHandle().setLeashedTo(((CraftEntity) holder).getHandle(), true);
        return true;
    }
}
