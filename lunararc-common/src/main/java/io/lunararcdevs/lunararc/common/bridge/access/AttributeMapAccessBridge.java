package io.lunararcdevs.lunararc.common.bridge.access;

import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface AttributeMapAccessBridge {
    Map<Holder<Attribute>, AttributeInstance> lunararc$getAttributes();
}
