package io.lunararcdevs.lunararc.common.mixin.core.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractArrow.class)
public interface AbstractArrowAccessor extends io.lunararcdevs.lunararc.common.bridge.access.AbstractArrowAccessBridge {
    @Accessor("inGround")
    boolean lunararc$isInGround();

    @Accessor("pickup")
    AbstractArrow.Pickup lunararc$getPickup();

    @Accessor("pickup")
    void lunararc$setPickup(AbstractArrow.Pickup pickup);

    @Accessor("life")
    int lunararc$getLife();

    @Accessor("life")
    void lunararc$setLife(int life);

    @Accessor("pickupItemStack")
    ItemStack lunararc$getPickupItemStack();

    @Accessor("pickupItemStack")
    void lunararc$setPickupItemStack(ItemStack stack);

    @Accessor("firedFromWeapon")
    ItemStack lunararc$getFiredFromWeapon();

    @Accessor("firedFromWeapon")
    void lunararc$setFiredFromWeapon(ItemStack stack);

    @Accessor("soundEvent")
    SoundEvent lunararc$getSoundEvent();

    @Invoker("shotFromCrossbow")
    boolean lunararc$shotFromCrossbow();
}
