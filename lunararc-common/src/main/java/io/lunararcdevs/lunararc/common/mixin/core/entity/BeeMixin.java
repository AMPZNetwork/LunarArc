package io.lunararcdevs.lunararc.common.mixin.core.entity;
import io.lunararcdevs.lunararc.common.bridge.entity.BeeBridge;
import net.kyori.adventure.util.TriState;
import net.minecraft.world.entity.animal.Bee;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
@Mixin(Bee.class)
public abstract class BeeMixin implements BeeBridge {
    @Unique private TriState lunararc$rollingOverride = TriState.NOT_SET;
    @Override public TriState lunararc$getRollingOverride() { return lunararc$rollingOverride; }
    @Override public void lunararc$setRollingOverride(TriState value) {
        lunararc$rollingOverride = java.util.Objects.requireNonNull(value, "value");
        Bee self = (Bee)(Object)this;
        self.setRolling(self.isRolling());
    }
    @ModifyVariable(method="setRolling", at=@At("HEAD"), argsOnly=true)
    private boolean lunararc$paperRollingOverride(boolean vanilla) {
        return lunararc$rollingOverride.toBooleanOrElse(vanilla);
    }
}
