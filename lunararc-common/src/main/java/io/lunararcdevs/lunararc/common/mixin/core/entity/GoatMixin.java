package io.lunararcdevs.lunararc.common.mixin.core.entity;
import io.lunararcdevs.lunararc.common.bridge.entity.GoatBridge;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.goat.Goat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
@Mixin(Goat.class)
public abstract class GoatMixin implements GoatBridge {
    @Shadow @Final private static EntityDataAccessor<Boolean> DATA_HAS_LEFT_HORN;
    @Shadow @Final private static EntityDataAccessor<Boolean> DATA_HAS_RIGHT_HORN;
    @Override public void lunararc$setLeftHorn(boolean value) { ((Goat)(Object)this).getEntityData().set(DATA_HAS_LEFT_HORN, value); }
    @Override public void lunararc$setRightHorn(boolean value) { ((Goat)(Object)this).getEntityData().set(DATA_HAS_RIGHT_HORN, value); }
}
