package io.lunararcdevs.lunararc.common.mixin.core.entity;

import io.lunararcdevs.lunararc.common.bridge.entity.AbstractSkeletonBridge;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractSkeleton.class)
public abstract class AbstractSkeletonMixin implements AbstractSkeletonBridge {
    @Unique private boolean lunararc$shouldBurnInDay = true;
    @Override public boolean lunararc$shouldBurnInDay() { return this.lunararc$shouldBurnInDay; }
    @Override public void lunararc$setShouldBurnInDay(boolean burn) { this.lunararc$shouldBurnInDay = burn; }

    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/AbstractSkeleton;isSunBurnTick()Z"))
    private boolean lunararc$paperBurnInDay(AbstractSkeleton self) {
        return this.lunararc$shouldBurnInDay && self.isSunBurnTick();
    }
}
