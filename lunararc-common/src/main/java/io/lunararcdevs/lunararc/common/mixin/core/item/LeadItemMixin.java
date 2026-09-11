package io.lunararcdevs.lunararc.common.mixin.core.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Bukkit leash-to-fence cancellation around the real LeadItem leash assignment. */
@Mixin(LeadItem.class)
public abstract class LeadItemMixin {
    @Unique private static final ThreadLocal<InteractionHand> lunararc$hand =
            ThreadLocal.withInitial(() -> InteractionHand.MAIN_HAND);

    @Inject(method = "useOn", at = @At("HEAD"), require = 0)
    private void lunararc$captureHand(UseOnContext context, CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir) {
        lunararc$hand.set(context.getHand());
    }

    @WrapOperation(
            method = "bindPlayerMobs",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Leashable;setLeashedTo(Lnet/minecraft/world/entity/Entity;Z)V"),
            require = 0)
    private static void lunararc$leashEvent(
            Leashable leashable, Entity holder, boolean broadcast, Operation<Void> original,
            Player player, Level level, BlockPos pos) {
        if (leashable instanceof Entity entity) {
            var event = org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerLeashEntityEvent(
                    entity, holder, player, lunararc$hand.get());
            if (event.isCancelled()) {
                return;
            }
        }
        original.call(leashable, holder, broadcast);
    }
}
