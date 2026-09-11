package io.lunararcdevs.lunararc.common.mixin.core.entity;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor extends io.lunararcdevs.lunararc.common.bridge.access.EntityAccessBridge {
    @Invoker("setSharedFlag")
    void lunararc$setSharedFlag(int flag, boolean value);
}
