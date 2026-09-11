package io.lunararcdevs.lunararc.common.mixin.core.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Bukkit leash-break reasons on the real 1.21.1 Leashable default methods. */
@Mixin(Leashable.class)
public interface LeashableMixin {

    @WrapOperation(
            method = "tickLeash",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Leashable;dropLeash(Lnet/minecraft/world/entity/Entity;ZZ)V"),
            require = 0)
    private static void lunararc$holderGone(
            Entity entity, boolean broadcast, boolean dropLead, Operation<Void> original) {
        if (entity.level().isClientSide) {
            original.call(entity, broadcast, dropLead);
            return;
        }
        EntityUnleashEvent.UnleashReason reason = !entity.isAlive()
                ? EntityUnleashEvent.UnleashReason.PLAYER_UNLEASH
                : EntityUnleashEvent.UnleashReason.HOLDER_GONE;
        EntityUnleashEvent event = new EntityUnleashEvent(
                ((EntityBridge) entity).lunararc$getBukkitEntity(), reason, dropLead);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            original.call(entity, broadcast, event.isDropLeash());
        }
    }

    @Inject(method = "leashTooFarBehaviour", at = @At("HEAD"), cancellable = true, require = 0)
    private void lunararc$distanceUnleash(CallbackInfo ci) {
        if (!((Object) this instanceof Entity entity) || entity.level().isClientSide) return;
        EntityUnleashEvent event = new EntityUnleashEvent(
                ((EntityBridge) entity).lunararc$getBukkitEntity(),
                EntityUnleashEvent.UnleashReason.DISTANCE,
                true);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
