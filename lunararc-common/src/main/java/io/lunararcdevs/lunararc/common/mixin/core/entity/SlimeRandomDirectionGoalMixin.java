package io.lunararcdevs.lunararc.common.mixin.core.entity;
import io.lunararcdevs.lunararc.common.bridge.entity.SlimeBridge;
import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(targets = "net.minecraft.world.entity.monster.Slime$SlimeRandomDirectionGoal", remap = false)
public abstract class SlimeRandomDirectionGoalMixin {
    @Shadow(remap = true) @Final private Slime slime;
    @Inject(method="canUse", remap=true, at=@At("HEAD"), cancellable=true)
    private void lunararc$paperCanWander(CallbackInfoReturnable<Boolean> cir) {
        if (!((SlimeBridge)(Object)slime).lunararc$canWander()) cir.setReturnValue(false);
    }
}
