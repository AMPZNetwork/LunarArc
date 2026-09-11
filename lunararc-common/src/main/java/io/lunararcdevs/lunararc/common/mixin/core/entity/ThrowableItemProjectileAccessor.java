package io.lunararcdevs.lunararc.common.mixin.core.entity;

import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Narrow bridge for the vanilla default projectile item. */
@Mixin(ThrowableItemProjectile.class)
public interface ThrowableItemProjectileAccessor extends io.lunararcdevs.lunararc.common.bridge.access.ThrowableItemProjectileAccessBridge {
    @Invoker("getDefaultItem") Item lunararc$getDefaultItem();
}
