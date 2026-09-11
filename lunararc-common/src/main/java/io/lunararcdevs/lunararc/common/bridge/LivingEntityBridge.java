package io.lunararcdevs.lunararc.common.bridge;

import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.util.TriState;

public interface LivingEntityBridge {
    TriState lunararc$getFrictionState();
    void lunararc$setFrictionState(TriState state);

    int lunararc$getShieldBlockingDelay();
    void lunararc$setShieldBlockingDelay(int delay);

    int lunararc$getMaximumAirOverride();
    void lunararc$setMaximumAirOverride(int ticks);


    boolean lunararc$isCollidable();
    void lunararc$setCollidable(boolean collidable);
    Set<UUID> lunararc$getCollidableExemptions();

    boolean lunararc$getBukkitCanPickupItems();
    void lunararc$setBukkitCanPickupItems(boolean pickup);

    void lunararc$pushHealReason(org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason reason, boolean fastRegen);

    void lunararc$pushEffectCause(org.bukkit.event.entity.EntityPotionEffectEvent.Cause cause);

    boolean lunararc$removeAllEffects(org.bukkit.event.entity.EntityPotionEffectEvent.Cause cause);

    void lunararc$completeUsingItem();

    net.minecraft.network.syncher.EntityDataAccessor<Integer> lunararc$getArrowCountDataAccessorBridge();
    int lunararc$getSpinAttackFlagBridge();
    byte lunararc$entityEventForEquipmentBreakBridge(net.minecraft.world.entity.EquipmentSlot slot);
}
