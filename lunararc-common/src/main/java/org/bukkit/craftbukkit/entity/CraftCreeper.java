package org.bukkit.craftbukkit.entity;

import io.lunararcdevs.lunararc.common.bridge.entity.CreeperBridge;
import java.util.Objects;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Creeper;
import org.bukkit.event.entity.CreeperPowerEvent;

public final class CraftCreeper extends CraftMonster implements Creeper {
    public CraftCreeper(CraftServer server, net.minecraft.world.entity.monster.Creeper entity) {
        super(server, entity);
    }

    private CreeperBridge creeperBridge() { return (CreeperBridge) (Object) getHandle(); }

    @Override public net.minecraft.world.entity.monster.Creeper getHandle() {
        return (net.minecraft.world.entity.monster.Creeper) this.entity;
    }

    @Override public boolean isPowered() { return getHandle().isPowered(); }

    @Override
    public void setPowered(boolean powered) {
        if (powered == isPowered()) return;
        CreeperPowerEvent.PowerCause cause = powered ? CreeperPowerEvent.PowerCause.SET_ON : CreeperPowerEvent.PowerCause.SET_OFF;
        CreeperPowerEvent event = new CreeperPowerEvent(this, cause);
        server.getPluginManager().callEvent(event);
        if (!event.isCancelled()) creeperBridge().lunararc$setPowered(powered);
    }

    @Override public void setMaxFuseTicks(int ticks) {
        if (ticks < 0) throw new IllegalArgumentException("ticks < 0");
        creeperBridge().lunararc$setMaxSwell(ticks);
    }
    @Override public int getMaxFuseTicks() { return creeperBridge().lunararc$getMaxSwell(); }

    @Override public void setFuseTicks(int ticks) {
        if (ticks < 0) throw new IllegalArgumentException("ticks < 0");
        if (ticks > getMaxFuseTicks()) throw new IllegalArgumentException("ticks > maxFuseTicks");
        creeperBridge().lunararc$setSwell(ticks);
    }
    @Override public int getFuseTicks() { return creeperBridge().lunararc$getSwell(); }

    @Override public void setExplosionRadius(int radius) {
        if (radius < 0) throw new IllegalArgumentException("radius < 0");
        creeperBridge().lunararc$setExplosionRadius(radius);
    }
    @Override public int getExplosionRadius() { return creeperBridge().lunararc$getExplosionRadius(); }

    @Override public void explode() { creeperBridge().lunararc$explode(); }

    @Override
    public void ignite(org.bukkit.entity.Entity entity) {
        Objects.requireNonNull(entity, "entity");
        if (!(entity instanceof CraftEntity craft)) throw new IllegalArgumentException("Entity is not backed by LunarArc CraftEntity");
        creeperBridge().lunararc$setIgniter(craft.getHandle());
        getHandle().ignite();
    }

    @Override public void ignite() { getHandle().ignite(); }

    @Override
    public org.bukkit.entity.Entity getIgniter() {
        net.minecraft.world.entity.Entity igniter = creeperBridge().lunararc$getIgniter();
        return igniter == null ? null : CraftEntity.getEntity(server, igniter);
    }

    @Override public void setIgnited(boolean ignited) {
        if (ignited == isIgnited()) return;
        if (ignited) getHandle().ignite();
        else {
            CreeperIgniteEventCompat.callAndSet(this, creeperBridge(), false);
        }
    }

    @Override public boolean isIgnited() { return getHandle().isIgnited(); }

    @Override public String toString() { return "CraftCreeper"; }

    /** Keeps the Paper event dependency isolated from normal NMS access. */
    private static final class CreeperIgniteEventCompat {
        static void callAndSet(CraftCreeper creeper, CreeperBridge bridge, boolean ignited) {
            com.destroystokyo.paper.event.entity.CreeperIgniteEvent event =
                    new com.destroystokyo.paper.event.entity.CreeperIgniteEvent(creeper, ignited);
            if (event.callEvent()) bridge.lunararc$setIgnitedDirect(event.isIgnited());
        }
    }
}
