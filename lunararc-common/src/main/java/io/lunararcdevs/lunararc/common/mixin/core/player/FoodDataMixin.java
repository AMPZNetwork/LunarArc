package io.lunararcdevs.lunararc.common.mixin.core.player;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import io.lunararcdevs.lunararc.common.bridge.FoodDataBridge;
import io.lunararcdevs.lunararc.common.bridge.LivingEntityBridge;
import io.lunararcdevs.lunararc.common.bridge.PlayerExhaustionBridge;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FoodData.class, priority = 2000)
public abstract class FoodDataMixin implements FoodDataBridge {
    @Shadow public int foodLevel;
    @Shadow public float saturationLevel;
    @Shadow private int lastFoodLevel;

    @Unique private int lunararc$saturatedRegenRate;
    @Unique private int lunararc$unsaturatedRegenRate;
    @Unique private int lunararc$starvationRate;
    @Unique private Player lunararc$owner;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void lunararc$initRates(CallbackInfo ci) {
        this.lunararc$saturatedRegenRate = 10;
        this.lunararc$unsaturatedRegenRate = 80;
        this.lunararc$starvationRate = 80;
    }

    @Override public int lunararc$getSaturatedRegenRate() { return this.lunararc$saturatedRegenRate; }
    @Override public void lunararc$setSaturatedRegenRate(int rate) { this.lunararc$saturatedRegenRate = rate; }
    @Override public int lunararc$getUnsaturatedRegenRate() { return this.lunararc$unsaturatedRegenRate; }
    @Override public void lunararc$setUnsaturatedRegenRate(int rate) { this.lunararc$unsaturatedRegenRate = rate; }
    @Override public int lunararc$getStarvationRate() { return this.lunararc$starvationRate; }
    @Override public void lunararc$setStarvationRate(int rate) { this.lunararc$starvationRate = rate; }
    @Override public void lunararc$setOwner(Player player) { this.lunararc$owner = player; }

    @Inject(method = "tick", at = @At("HEAD"), require = 0)
    private void lunararc$captureOwner(Player player, CallbackInfo ci) {
        this.lunararc$owner = player;
    }

    @WrapMethod(method = "eat(Lnet/minecraft/world/food/FoodProperties;)V")
    private void lunararc$foodEat(FoodProperties food, Operation<Void> original) {
        Player owner = this.lunararc$owner;
        if (owner == null || owner.level().isClientSide) {
            original.call(food);
            return;
        }
        Object bukkit = ((EntityBridge) owner).lunararc$getBukkitEntity();
        if (!(bukkit instanceof org.bukkit.entity.HumanEntity human)) {
            original.call(food);
            return;
        }
        int target = net.minecraft.util.Mth.clamp(this.foodLevel + food.nutrition(), 0, 20);
        org.bukkit.event.entity.FoodLevelChangeEvent event = new org.bukkit.event.entity.FoodLevelChangeEvent(human, target);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            int delta = net.minecraft.util.Mth.clamp(event.getFoodLevel(), 0, 20) - this.foodLevel;
            ((FoodData) (Object) this).eat(delta, food.saturation());
        }
        if (owner instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                && ((EntityBridge) owner).lunararc$getBukkitEntity() instanceof org.bukkit.craftbukkit.entity.CraftPlayer craftPlayer) {
            craftPlayer.sendHealthUpdate();
        }
    }

    @ModifyExpressionValue(method = "tick",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I"), require = 0)
    private int lunararc$foodExhaustionLevelChange(int vanillaLevel) {
        Player owner = this.lunararc$owner;
        if (owner == null || owner.level().isClientSide || vanillaLevel >= this.lastFoodLevel) return vanillaLevel;
        Object bukkit = ((EntityBridge) owner).lunararc$getBukkitEntity();
        if (!(bukkit instanceof org.bukkit.entity.HumanEntity human)) return vanillaLevel;
        var event = new org.bukkit.event.entity.FoodLevelChangeEvent(human, vanillaLevel);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        return event.isCancelled() ? this.lastFoodLevel : net.minecraft.util.Mth.clamp(event.getFoodLevel(), 0, 20);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;heal(F)V"), require = 0)
    private void lunararc$satiatedHeal(Player player, CallbackInfo ci) {
        ((LivingEntityBridge) player).lunararc$pushHealReason(
                org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason.SATIATED,
                this.saturationLevel > 0.0F);
    }

    @Redirect(method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;addExhaustion(F)V"), require = 0)
    private void lunararc$regenExhaustion(FoodData instance, float amount) {
        Player owner = this.lunararc$owner;
        if (owner == null) {
            instance.addExhaustion(amount);
            return;
        }
        ((PlayerExhaustionBridge) owner).lunararc$pushExhaustionReason(
                org.bukkit.event.entity.EntityExhaustionEvent.ExhaustionReason.REGEN);
        owner.causeFoodExhaustion(amount);
    }
}
