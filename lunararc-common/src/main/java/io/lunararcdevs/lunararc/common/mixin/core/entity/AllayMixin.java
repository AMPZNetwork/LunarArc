package io.lunararcdevs.lunararc.common.mixin.core.entity;
import io.lunararcdevs.lunararc.common.bridge.entity.AllayBridge;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.allay.Allay;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Allay.class)
public abstract class AllayMixin implements AllayBridge {
    @Shadow @Final private static EntityDataAccessor<Boolean> DATA_CAN_DUPLICATE;
    @Unique private boolean lunararc$forceDancing;
    @Invoker("canDuplicate") protected abstract boolean lunararc$invokeCanDuplicate();
    @Invoker("resetDuplicationCooldown") protected abstract void lunararc$invokeResetDuplicationCooldown();
    @Override public boolean lunararc$canDuplicate() { return lunararc$invokeCanDuplicate(); }
    @Override public void lunararc$setCanDuplicate(boolean value) { ((Allay)(Object)this).getEntityData().set(DATA_CAN_DUPLICATE, value); }
    @Override public void lunararc$resetDuplicationCooldown() { lunararc$invokeResetDuplicationCooldown(); }
    @Override public void lunararc$setForceDancing(boolean value) { lunararc$forceDancing = value; }
    @Inject(method="shouldStopDancing", at=@At("HEAD"), cancellable=true)
    private void lunararc$paperForceDancing(CallbackInfoReturnable<Boolean> cir) { if (lunararc$forceDancing) cir.setReturnValue(false); }
}
