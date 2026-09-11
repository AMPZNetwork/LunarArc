package io.lunararcdevs.lunararc.common.bridge.access;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface LivingEntityAccessBridge {
    int lunararc$getUseItemRemaining();
    void lunararc$setUseItemRemaining(int ticks);
    int lunararc$getRemoveArrowTime();
    void lunararc$setRemoveArrowTime(int ticks);
    int lunararc$getRemoveStingerTime();
    void lunararc$setRemoveStingerTime(int ticks);
    float lunararc$getSidewaysMovement();
    float lunararc$getUpwardsMovement();
    float lunararc$getForwardsMovement();
    boolean lunararc$isJumping();
    void lunararc$setJumping(boolean jumping);
    int lunararc$getInvulnerableDuration();
    void lunararc$setInvulnerableDuration(int ticks);
    float lunararc$getLastHurt();
    void lunararc$setLastHurt(float damage);
    net.minecraft.world.entity.player.Player lunararc$getLastHurtByPlayer();
    void lunararc$setLastHurtByPlayer(net.minecraft.world.entity.player.Player player);
    net.minecraft.world.entity.LivingEntity lunararc$getLastHurtByMob();
    void lunararc$setLastHurtByMob(net.minecraft.world.entity.LivingEntity entity);
    int lunararc$getLastHurtByPlayerTime();
    void lunararc$setLastHurtByPlayerTime(int ticks);
    void lunararc$setLivingEntityFlag(int flag, boolean value);
    boolean lunararc$canUseSlot(net.minecraft.world.entity.EquipmentSlot slot);
    net.minecraft.sounds.SoundEvent lunararc$invokeGetHurtSound(net.minecraft.world.damagesource.DamageSource source);
    net.minecraft.sounds.SoundEvent lunararc$invokeGetDeathSound();
    net.minecraft.sounds.SoundEvent lunararc$invokeGetDrinkingSound(net.minecraft.world.item.ItemStack stack);
    net.minecraft.sounds.SoundEvent lunararc$invokeGetEatingSound(net.minecraft.world.item.ItemStack stack);
    net.minecraft.sounds.SoundEvent lunararc$invokeGetFallDamageSound(int fallHeight);
}
