package io.lunararcdevs.lunararc.common.bridge.entity;

import net.minecraft.world.entity.Entity;

/** Narrow Bukkit/Paper state attached directly to the loader-owned NMS Creeper. */
public interface CreeperBridge {
    int lunararc$getSwell();
    void lunararc$setSwell(int ticks);
    int lunararc$getMaxSwell();
    void lunararc$setMaxSwell(int ticks);
    int lunararc$getExplosionRadius();
    void lunararc$setExplosionRadius(int radius);
    void lunararc$setPowered(boolean powered);
    void lunararc$setIgnitedDirect(boolean ignited);
    void lunararc$explode();
    Entity lunararc$getIgniter();
    void lunararc$setIgniter(Entity entity);
}
