package io.lunararcdevs.lunararc.common.mixin.core.entity;

import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TamableAnimal.class)
public abstract class TamableAnimalMixin {
    @Inject(method = "tame", at = @At("HEAD"), cancellable = true, require = 0)
    private void lunararc$tameEvent(Player owner, CallbackInfo ci) {
        TamableAnimal animal = (TamableAnimal) (Object) this;
        // tame() takes a Player parameter directly rather than being purely
        // interaction-dispatched, so mod code can plausibly call it during entity
        // finalization/spawn on a worker thread — same class of risk as a real confirmed
        // crash elsewhere on this exact pattern.
        if (animal.level().isClientSide || owner == null || !org.bukkit.Bukkit.isPrimaryThread()) {
            return;
        }
        Object bukkitAnimal = ((EntityBridge) animal).lunararc$getBukkitEntity();
        Object bukkitOwner = ((EntityBridge) owner).lunararc$getBukkitEntity();
        if (!(bukkitAnimal instanceof org.bukkit.entity.LivingEntity living)
                || !(bukkitOwner instanceof org.bukkit.entity.AnimalTamer tamer)) {
            return;
        }
        org.bukkit.event.entity.EntityTameEvent event = new org.bukkit.event.entity.EntityTameEvent(living, tamer);
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
