package io.lunararcdevs.lunararc.common.mixin.core.entity;

import io.lunararcdevs.lunararc.common.bridge.ThrownTridentBridge;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.projectile.ThrownTrident;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ThrownTrident.class)
public abstract class ThrownTridentMixin implements ThrownTridentBridge {
    @Shadow @Final private static EntityDataAccessor<Byte> ID_LOYALTY;
    @Shadow @Final private static EntityDataAccessor<Boolean> ID_FOIL;
    @Shadow private boolean dealtDamage;

    @Override
    public boolean lunararc$hasFoil() {
        return ((ThrownTrident) (Object) this).getEntityData().get(ID_FOIL);
    }

    @Override
    public void lunararc$setFoil(boolean foil) {
        ((ThrownTrident) (Object) this).getEntityData().set(ID_FOIL, foil);
    }

    @Override
    public int lunararc$getLoyalty() {
        return Byte.toUnsignedInt(((ThrownTrident) (Object) this).getEntityData().get(ID_LOYALTY));
    }

    @Override
    public void lunararc$setLoyalty(byte loyalty) {
        ((ThrownTrident) (Object) this).getEntityData().set(ID_LOYALTY, loyalty);
    }

    @Override
    public boolean lunararc$hasDealtDamage() {
        return this.dealtDamage;
    }

    @Override
    public void lunararc$setHasDealtDamage(boolean dealtDamage) {
        this.dealtDamage = dealtDamage;
    }

    @ModifyConstant(method = "onHitEntity", constant = @Constant(floatValue = 8.0F), require = 1)
    private float lunararc$useConfiguredBaseDamage(float vanillaDamage) {
        return (float) ((ThrownTrident) (Object) this).getBaseDamage();
    }
}
