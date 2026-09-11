package org.bukkit.craftbukkit.entity;

import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.ThrowableProjectile;
import org.bukkit.inventory.ItemStack;

public abstract class CraftThrowableProjectile extends CraftProjectile implements ThrowableProjectile {
    protected CraftThrowableProjectile(CraftServer server, ThrowableItemProjectile entity) { super(server, entity); }

    @Override public ThrowableItemProjectile getHandle() { return (ThrowableItemProjectile) this.entity; }

    @Override
    public ItemStack getItem() {
        net.minecraft.world.item.ItemStack item = getHandle().getItem();
        if (item.isEmpty()) {
            item = new net.minecraft.world.item.ItemStack(((io.lunararcdevs.lunararc.common.bridge.access.ThrowableItemProjectileAccessBridge) (Object) getHandle()).lunararc$getDefaultItem());
        }
        return CraftItemStack.asBukkitCopy(item);
    }

    @Override
    public void setItem(ItemStack item) {
        if (item == null) throw new IllegalArgumentException("item");
        getHandle().setItem(CraftItemStack.asNMSCopy(item));
    }
}
