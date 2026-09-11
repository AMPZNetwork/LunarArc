package io.lunararcdevs.lunararc.common.mixin.core.entity;

import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractSchoolingFish.class)
public interface SchoolingFishAccessor {
    @Accessor("schoolSize") int lunararc$getSchoolSize();
    @Accessor("leader") AbstractSchoolingFish lunararc$getLeader();
}
