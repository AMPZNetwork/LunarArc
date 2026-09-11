package org.bukkit.craftbukkit.entity;

import io.lunararcdevs.lunararc.common.bridge.access.AbstractWindChargeAccessBridge;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.AbstractWindCharge;

public class CraftAbstractWindCharge extends CraftFireball implements AbstractWindCharge {
    public CraftAbstractWindCharge(CraftServer server, net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge entity) { super(server, entity); }
    @Override public void explode() {
        ((AbstractWindChargeAccessBridge) (Object) getHandle()).lunararc$invokeExplode(getHandle().position());
        getHandle().discard();
    }
    @Override public net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge getHandle() { return (net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge) this.entity; }
}
