package io.lunararcdevs.lunararc.common.mixin.core.entity;

import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Paper/Bukkit XP pickup event without replacing orb pickup or mending logic. */
@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {

    @Redirect(
            method = "playerTouch",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperiencePoints(I)V"),
            require = 0)
    private void lunararc$playerExpChange(Player player, int amount) {
        ExperienceOrb self = (ExperienceOrb) (Object) this;
        int awarded = amount;
        if (player instanceof ServerPlayer && !self.level().isClientSide) {
            Object bukkitPlayer = ((EntityBridge) player).lunararc$getBukkitEntity();
            Object bukkitOrb = ((EntityBridge) self).lunararc$getBukkitEntity();
            if (bukkitPlayer instanceof org.bukkit.entity.Player bp
                    && bukkitOrb instanceof org.bukkit.entity.ExperienceOrb bo) {
                org.bukkit.event.player.PlayerExpChangeEvent event =
                        new org.bukkit.event.player.PlayerExpChangeEvent(bp, bo, amount);
                org.bukkit.Bukkit.getPluginManager().callEvent(event);
                awarded = event.getAmount();
            }
        }
        player.giveExperiencePoints(awarded);
    }
}
