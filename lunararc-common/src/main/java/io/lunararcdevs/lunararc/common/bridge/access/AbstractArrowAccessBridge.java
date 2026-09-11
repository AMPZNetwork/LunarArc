package io.lunararcdevs.lunararc.common.bridge.access;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface AbstractArrowAccessBridge {
    boolean lunararc$isInGround();
    AbstractArrow.Pickup lunararc$getPickup();
    void lunararc$setPickup(AbstractArrow.Pickup pickup);
    int lunararc$getLife();
    void lunararc$setLife(int life);
    ItemStack lunararc$getPickupItemStack();
    void lunararc$setPickupItemStack(ItemStack stack);
    ItemStack lunararc$getFiredFromWeapon();
    void lunararc$setFiredFromWeapon(ItemStack stack);
    SoundEvent lunararc$getSoundEvent();
    boolean lunararc$shotFromCrossbow();
}
