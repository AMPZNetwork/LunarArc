package org.bukkit.craftbukkit.entity;

import io.lunararcdevs.lunararc.common.bridge.LargeFireballBridge;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.LargeFireball;

public final class CraftLargeFireball extends CraftSizedFireball implements LargeFireball {
    public CraftLargeFireball(CraftServer server, net.minecraft.world.entity.projectile.LargeFireball entity) {
        super(server, entity);
    }
    @Override public float getYield() { return ((LargeFireballBridge) (Object) getHandle()).lunararc$getExplosionPower(); }
    @Override public void setYield(float yield) {
        ((LargeFireballBridge) (Object) getHandle()).lunararc$setExplosionPower(Math.round(yield));
    }
    @Override public net.minecraft.world.entity.projectile.LargeFireball getHandle() { return (net.minecraft.world.entity.projectile.LargeFireball) this.entity; }
}
