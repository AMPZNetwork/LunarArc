package io.lunararcdevs.lunararc.common.mixin.core.projectile;

import io.lunararcdevs.lunararc.common.bridge.AbstractHurtingProjectileBridge;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractHurtingProjectile.class)
public abstract class AbstractHurtingProjectileMixin implements AbstractHurtingProjectileBridge {
    @Unique private float lunararc$bukkitYield = 1.0F;
    @Unique private boolean lunararc$incendiary = true;

    @Override public float lunararc$getBukkitYield() { return this.lunararc$bukkitYield; }
    @Override public void lunararc$setBukkitYield(float yield) { this.lunararc$bukkitYield = yield; }
    @Override public boolean lunararc$isIncendiary() { return this.lunararc$incendiary; }
    @Override public void lunararc$setIncendiary(boolean incendiary) { this.lunararc$incendiary = incendiary; }
}
