package io.lunararcdevs.lunararc.common.mixin.core.entity;

import net.minecraft.world.entity.ItemBasedSteering;
import net.minecraft.world.entity.animal.Pig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Pig.class)
public interface PigAccessor extends io.lunararcdevs.lunararc.common.bridge.access.PigAccessBridge {
    @Accessor("steering") ItemBasedSteering lunararc$getSteering();
}
