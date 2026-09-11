package io.lunararcdevs.lunararc.common.mixin.core.projectile;

import io.lunararcdevs.lunararc.common.bridge.AbstractHurtingProjectileBridge;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmallFireball.class)
public abstract class SmallFireballMixin {
    @Inject(
            method = "onHitBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;onHitBlock(Lnet/minecraft/world/phys/BlockHitResult;)V", shift = At.Shift.AFTER),
            cancellable = true,
            require = 0
    )
    private void lunararc$respectNonIncendiary(BlockHitResult hit, CallbackInfo ci) {
        if (!((AbstractHurtingProjectileBridge) (Object) this).lunararc$isIncendiary()) ci.cancel();
    }
}
