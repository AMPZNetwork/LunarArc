package io.lunararcdevs.lunararc.common.mixin.core.entity;

import io.lunararcdevs.lunararc.common.server.LunarArcTntBudget;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Caps how many primed TNT can actually detonate in a single tick, matching Spigot's
 * max-tnt-per-tick. See {@link LunarArcTntBudget} for why this exists.
 */
@Mixin(PrimedTnt.class)
public abstract class PrimedTntMixin {
    @Inject(
            method = "tick",
            cancellable = true,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/PrimedTnt;discard()V"),
            require = 0)
    private void lunararc$capExplosionsPerTick(CallbackInfo ci) {
        PrimedTnt self = (PrimedTnt) (Object) this;
        Level level = self.level();
        if (level.isClientSide) return;
        if (!LunarArcTntBudget.tryConsume(level)) {
            self.setFuse(1);
            ci.cancel();
        }
    }
}
