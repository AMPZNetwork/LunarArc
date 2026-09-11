package io.lunararcdevs.lunararc.common.mixin.core.entity;

import io.lunararcdevs.lunararc.common.LunarArcServerAccess;
import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import io.lunararcdevs.lunararc.common.bridge.PlayerAffectsSpawningBridge;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Player.class)
public abstract class PlayerMixin implements PlayerAffectsSpawningBridge, io.lunararcdevs.lunararc.common.bridge.PlayerExhaustionBridge {
    @Unique
    private boolean lunararc$affectsSpawning = true;
    @Unique private org.bukkit.event.entity.EntityExhaustionEvent.ExhaustionReason lunararc$exhaustionReason = org.bukkit.event.entity.EntityExhaustionEvent.ExhaustionReason.UNKNOWN;

    @Override
    public boolean lunararc$getAffectsSpawning() {
        return this.lunararc$affectsSpawning;
    }

    @Override
    public void lunararc$setAffectsSpawning(boolean affectsSpawning) {
        this.lunararc$affectsSpawning = affectsSpawning;
    }

    @Override
    public void lunararc$pushExhaustionReason(org.bukkit.event.entity.EntityExhaustionEvent.ExhaustionReason reason) {
        this.lunararc$exhaustionReason = java.util.Objects.requireNonNull(reason, "reason");
    }


    @Inject(
            method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("RETURN"),
            cancellable = true,
            require = 0)
    private void lunararc$onDrop(
            ItemStack droppedStack,
            boolean dropAround,
            boolean traceItem,
            CallbackInfoReturnable<ItemEntity> cir) {
        ItemEntity dropped = cir.getReturnValue();
        if (dropped == null || droppedStack.isEmpty()) {
            return;
        }

        Player handle = (Player) (Object) this;
        if (!(handle instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return;
        }
        Object bukkitPlayerObject = ((EntityBridge) handle).lunararc$getBukkitEntity();
        Object bukkitItemObject = ((EntityBridge) dropped).lunararc$getBukkitEntity();
        if (!(bukkitPlayerObject instanceof org.bukkit.entity.Player bukkitPlayer)
                || !(bukkitItemObject instanceof org.bukkit.entity.Item bukkitItem)) {
            return;
        }

        org.bukkit.event.player.PlayerDropItemEvent event =
                new org.bukkit.event.player.PlayerDropItemEvent(bukkitPlayer, bukkitItem);
        LunarArcServerAccess.getCraftServer(serverPlayer.server)
                .getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            return;
        }

        // Keep loader/mod drop processing intact; cancellation only removes the
        // resulting vanilla item entity and restores the dropped Bukkit stack.
        dropped.discard();

        org.bukkit.inventory.ItemStack restore = bukkitItem.getItemStack();
        if (traceItem) {
            org.bukkit.inventory.ItemStack current = bukkitPlayer.getInventory().getItemInMainHand();
            if (current.getType().isAir()) {
                bukkitPlayer.getInventory().setItemInMainHand(restore);
            } else if (current.isSimilar(restore)
                    && current.getAmount() < current.getMaxStackSize()
                    && restore.getAmount() == 1) {
                current.setAmount(current.getAmount() + 1);
                bukkitPlayer.getInventory().setItemInMainHand(current);
            } else {
                bukkitPlayer.getInventory().addItem(restore);
            }
        } else {
            bukkitPlayer.getInventory().addItem(restore);
        }
        cir.setReturnValue(null);
    }
    @com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod(method = "causeFoodExhaustion")
    private void lunararc$exhaustion(float exhaustion, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        Player handle = (Player) (Object) this;
        var reason = this.lunararc$exhaustionReason;
        this.lunararc$exhaustionReason = org.bukkit.event.entity.EntityExhaustionEvent.ExhaustionReason.UNKNOWN;
        Object bukkit = ((EntityBridge) handle).lunararc$getBukkitEntity();
        if (!(bukkit instanceof org.bukkit.entity.HumanEntity human) || handle.level().isClientSide) {
            original.call(exhaustion);
            return;
        }
        var event = new org.bukkit.event.entity.EntityExhaustionEvent(human, reason, exhaustion);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) original.call(event.getExhaustion());
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;heal(F)V"), require = 0)
    private void lunararc$peacefulRegen(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        ((io.lunararcdevs.lunararc.common.bridge.LivingEntityBridge) this)
                .lunararc$pushHealReason(org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason.REGEN, false);
    }


    @Inject(
            method = "jumpFromGround",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"),
            require = 0)
    private void lunararc$jumpExhaustion(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        Player self = (Player) (Object) this;
        this.lunararc$pushExhaustionReason(self.isSprinting()
                ? org.bukkit.event.entity.EntityExhaustionEvent.ExhaustionReason.JUMP_SPRINT
                : org.bukkit.event.entity.EntityExhaustionEvent.ExhaustionReason.JUMP);
    }

    @Inject(
            method = "actuallyHurt",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"),
            require = 0)
    private void lunararc$damageExhaustion(
            net.minecraft.world.damagesource.DamageSource source, float amount,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        this.lunararc$pushExhaustionReason(org.bukkit.event.entity.EntityExhaustionEvent.ExhaustionReason.DAMAGED);
    }

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"), require = 0)
    private void lunararc$attackExhaustion(net.minecraft.world.entity.Entity target, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        this.lunararc$pushExhaustionReason(org.bukkit.event.entity.EntityExhaustionEvent.ExhaustionReason.ATTACK);
    }

    @Inject(
            method = "turtleHelmetTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"),
            require = 0)
    private void lunararc$turtleHelmetCause(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        ((io.lunararcdevs.lunararc.common.bridge.LivingEntityBridge) this)
                .lunararc$pushEffectCause(org.bukkit.event.entity.EntityPotionEffectEvent.Cause.TURTLE_HELMET);
    }

}
