package io.lunararcdevs.lunararc.common.mixin.core.entity;

import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import io.lunararcdevs.lunararc.common.bridge.ItemEntityBridge;
import io.lunararcdevs.lunararc.common.bridge.MobBridge;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Targeted Bukkit/Paper hooks on the real loader-owned Mob. */
@Mixin(Mob.class)
public abstract class MobMixin implements MobBridge {
    @Unique private boolean lunararc$aware = true;
    @Unique private org.bukkit.event.entity.EntityTargetEvent.TargetReason lunararc$targetReason = org.bukkit.event.entity.EntityTargetEvent.TargetReason.UNKNOWN;
    @Unique private org.bukkit.event.entity.EntityTransformEvent.TransformReason lunararc$transformReason = org.bukkit.event.entity.EntityTransformEvent.TransformReason.UNKNOWN;
    @Unique private org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason lunararc$transformSpawnReason;

    @Override public boolean lunararc$isAware() { return lunararc$aware; }
    @Override public void lunararc$setAware(boolean aware) { lunararc$aware = aware; }
    @Override public void lunararc$pushTargetReason(org.bukkit.event.entity.EntityTargetEvent.TargetReason reason) {
        this.lunararc$targetReason = java.util.Objects.requireNonNull(reason, "reason");
    }
    @Override public void lunararc$pushTransformReason(org.bukkit.event.entity.EntityTransformEvent.TransformReason reason) {
        this.lunararc$transformReason = java.util.Objects.requireNonNull(reason, "reason");
    }
    @Override public void lunararc$pushTransformContext(
            org.bukkit.event.entity.EntityTransformEvent.TransformReason reason,
            org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason spawnReason) {
        this.lunararc$transformReason = java.util.Objects.requireNonNull(reason, "reason");
        this.lunararc$transformSpawnReason = java.util.Objects.requireNonNull(spawnReason, "spawnReason");
    }


    /**
     * Fire Bukkit target changes exactly once at the real Mob#setTarget boundary.
     * Loader-native target hooks (Forge/NeoForge) run later in the same NMS method,
     * so they naturally observe the Bukkit-adjusted target without a second bridge.
     */
    @ModifyVariable(method = "setTarget", at = @At("HEAD"), argsOnly = true, require = 0)
    private LivingEntity lunararc$bukkitTargetChange(LivingEntity requestedTarget) {
        Mob self = (Mob) (Object) this;
        // setTarget is typically tick-driven (main-thread), but can plausibly be called by mod
        // code during entity finalization/spawn on a worker thread, same class of risk as a
        // real confirmed crash elsewhere on this exact pattern.
        if (self.level().isClientSide || !org.bukkit.Bukkit.isPrimaryThread()) return requestedTarget;

        org.bukkit.entity.Entity bukkitSelf = ((EntityBridge) self).lunararc$getBukkitEntity();
        org.bukkit.entity.LivingEntity bukkitRequested = null;
        if (requestedTarget != null) {
            org.bukkit.entity.Entity converted = ((EntityBridge) requestedTarget).lunararc$getBukkitEntity();
            if (converted instanceof org.bukkit.entity.LivingEntity living) bukkitRequested = living;
        }

        var reason = this.lunararc$targetReason;
        this.lunararc$targetReason = org.bukkit.event.entity.EntityTargetEvent.TargetReason.UNKNOWN;
        org.bukkit.event.entity.EntityTargetLivingEntityEvent event =
                new org.bukkit.event.entity.EntityTargetLivingEntityEvent(
                        bukkitSelf, bukkitRequested, reason);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return self.getTarget();
        org.bukkit.entity.LivingEntity replacement = event.getTarget();
        if (replacement == null) return null;
        if (replacement instanceof org.bukkit.craftbukkit.entity.CraftLivingEntity craft) {
            net.minecraft.world.entity.LivingEntity handle = craft.getHandle();
            return handle.level() == self.level() ? handle : self.getTarget();
        }
        return self.getTarget();
    }

    @Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true, require = 0)
    private void lunararc$paperAware(CallbackInfo ci) {
        if (!lunararc$aware) ci.cancel();
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"), require = 0)
    private void lunararc$saveAware(CompoundTag tag, CallbackInfo ci) {
        if (!lunararc$aware) tag.putBoolean("Bukkit.Aware", false);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"), require = 0)
    private void lunararc$loadAware(CompoundTag tag, CallbackInfo ci) {
        lunararc$aware = !tag.contains("Bukkit.Aware") || tag.getBoolean("Bukkit.Aware");
    }

    @Inject(method = "pickUpItem", at = @At("HEAD"), cancellable = true)
    private void lunararc$paperCanMobPickup(ItemEntity item, CallbackInfo ci) {
        if (item instanceof ItemEntityBridge bridge && !bridge.lunararc$canMobPickup()) ci.cancel();
    }

    @Inject(
            method = "convertTo",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"),
            cancellable = true,
            require = 0)
    private void lunararc$transformEvent(
            net.minecraft.world.entity.EntityType<?> targetType, boolean keepEquipment,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Mob> cir,
            @Local Mob converted) {
        if (converted == null || !org.bukkit.Bukkit.isPrimaryThread()) return;
        Mob self = (Mob) (Object) this;
        var reason = this.lunararc$transformReason;
        var spawnReason = this.lunararc$transformSpawnReason;
        this.lunararc$transformReason = org.bukkit.event.entity.EntityTransformEvent.TransformReason.UNKNOWN;
        this.lunararc$transformSpawnReason = null;
        if (spawnReason != null) {
            ((EntityBridge) converted).lunararc$setSpawnReason(spawnReason);
        }
        var event = new org.bukkit.event.entity.EntityTransformEvent(
                ((EntityBridge) self).lunararc$getBukkitEntity(),
                java.util.List.of(((EntityBridge) converted).lunararc$getBukkitEntity()),
                reason);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(null);
        }
    }

}
