package io.ampznetwork.lunararc.common.bridge;

public interface EntityBridge {
    int lunararc$getPortalCooldown();
    void lunararc$setPortalCooldown(int cooldown);
    int lunararc$getRemainingFireTicks();
    void lunararc$setRemainingFireTicks(int ticks);
    org.bukkit.persistence.PersistentDataContainer lunararc$getPersistentDataContainer();
    org.bukkit.entity.Entity lunararc$getBukkitEntity();
    void lunararc$setBukkitEntity(org.bukkit.entity.Entity entity);
}
