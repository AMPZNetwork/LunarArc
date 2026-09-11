package io.lunararcdevs.lunararc.common.mixin.core.entity;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Narrow bridge for Bukkit's dynamic attribute registration. */
@Mixin(AttributeMap.class)
public interface AttributeMapAccessor extends io.lunararcdevs.lunararc.common.bridge.access.AttributeMapAccessBridge {
    @Accessor("attributes") Map<Holder<Attribute>, AttributeInstance> lunararc$getAttributes();
}
