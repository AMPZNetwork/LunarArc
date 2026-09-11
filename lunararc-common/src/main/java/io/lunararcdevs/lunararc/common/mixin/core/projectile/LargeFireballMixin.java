package io.lunararcdevs.lunararc.common.mixin.core.projectile;

import io.lunararcdevs.lunararc.common.bridge.AbstractHurtingProjectileBridge;
import io.lunararcdevs.lunararc.common.bridge.LargeFireballBridge;
import net.minecraft.world.entity.projectile.LargeFireball;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LargeFireball.class)
public abstract class LargeFireballMixin implements LargeFireballBridge {
    @Accessor("explosionPower") public abstract int lunararc$getExplosionPower();
    @Accessor("explosionPower") @Mutable public abstract void lunararc$setExplosionPower(int power);

    @ModifyArg(
            method = "onHit",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;"),
            index = 5,
            require = 0
    )
    private boolean lunararc$useBukkitIncendiary(boolean vanilla) {
        return ((AbstractHurtingProjectileBridge) (Object) this).lunararc$isIncendiary();
    }
}
