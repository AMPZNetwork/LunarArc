package io.lunararcdevs.lunararc.common.mixin.core.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;


@Mixin(LivingEntity.class)
public interface LivingEntityAccessor extends io.lunararcdevs.lunararc.common.bridge.access.LivingEntityAccessBridge {
    @Accessor("DATA_ARROW_COUNT_ID")
    static EntityDataAccessor<Integer> lunararc$getArrowCountDataAccessor() {
        throw new AssertionError();
    }

    @Accessor("useItemRemaining") int lunararc$getUseItemRemaining();
    @Accessor("useItemRemaining") void lunararc$setUseItemRemaining(int ticks);

    @Accessor("removeArrowTime") int lunararc$getRemoveArrowTime();
    @Accessor("removeArrowTime") void lunararc$setRemoveArrowTime(int ticks);

    @Accessor("removeStingerTime") int lunararc$getRemoveStingerTime();
    @Accessor("removeStingerTime") void lunararc$setRemoveStingerTime(int ticks);

    @Accessor("xxa") float lunararc$getSidewaysMovement();
    @Accessor("yya") float lunararc$getUpwardsMovement();
    @Accessor("zza") float lunararc$getForwardsMovement();

    @Accessor("jumping") boolean lunararc$isJumping();
    @Accessor("jumping") void lunararc$setJumping(boolean jumping);

    @Accessor("invulnerableDuration") int lunararc$getInvulnerableDuration();
    @Accessor("invulnerableDuration") void lunararc$setInvulnerableDuration(int ticks);

    @Accessor("lastHurt") float lunararc$getLastHurt();
    @Accessor("lastHurt") void lunararc$setLastHurt(float damage);

    @Accessor("lastHurtByPlayer") net.minecraft.world.entity.player.Player lunararc$getLastHurtByPlayer();
    @Accessor("lastHurtByPlayer") void lunararc$setLastHurtByPlayer(net.minecraft.world.entity.player.Player player);

    @Accessor("lastHurtByMob") net.minecraft.world.entity.LivingEntity lunararc$getLastHurtByMob();
    @Accessor("lastHurtByMob") void lunararc$setLastHurtByMob(net.minecraft.world.entity.LivingEntity entity);

    @Accessor("lastHurtByPlayerTime") int lunararc$getLastHurtByPlayerTime();
    @Accessor("lastHurtByPlayerTime") void lunararc$setLastHurtByPlayerTime(int ticks);

    @Accessor("LIVING_ENTITY_FLAG_SPIN_ATTACK")
    static int lunararc$getSpinAttackFlag() { throw new AssertionError(); }

    @Invoker("setLivingEntityFlag")
    void lunararc$setLivingEntityFlag(int flag, boolean value);

    @Invoker("entityEventForEquipmentBreak")
    static byte lunararc$entityEventForEquipmentBreak(net.minecraft.world.entity.EquipmentSlot slot) {
        throw new AssertionError();
    }

    @Invoker("canUseSlot")
    boolean lunararc$canUseSlot(net.minecraft.world.entity.EquipmentSlot slot);

    @Invoker("getHurtSound")
    net.minecraft.sounds.SoundEvent lunararc$invokeGetHurtSound(net.minecraft.world.damagesource.DamageSource source);

    @Invoker("getDeathSound")
    net.minecraft.sounds.SoundEvent lunararc$invokeGetDeathSound();

    @Invoker("getDrinkingSound")
    net.minecraft.sounds.SoundEvent lunararc$invokeGetDrinkingSound(net.minecraft.world.item.ItemStack stack);

    @Invoker("getEatingSound")
    net.minecraft.sounds.SoundEvent lunararc$invokeGetEatingSound(net.minecraft.world.item.ItemStack stack);

    @Invoker("getFallDamageSound")
    net.minecraft.sounds.SoundEvent lunararc$invokeGetFallDamageSound(int fallHeight);

}
