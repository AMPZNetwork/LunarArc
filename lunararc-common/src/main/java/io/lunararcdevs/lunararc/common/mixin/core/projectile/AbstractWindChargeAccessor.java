package io.lunararcdevs.lunararc.common.mixin.core.projectile;

import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractWindCharge.class)
public interface AbstractWindChargeAccessor extends io.lunararcdevs.lunararc.common.bridge.access.AbstractWindChargeAccessBridge {
    @Invoker("explode") void lunararc$invokeExplode(Vec3 position);
}
