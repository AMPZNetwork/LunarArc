package io.lunararcdevs.lunararc.common.mixin.core.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Narrow compile/runtime bridge for CraftMob APIs that need protected/private Mob state. */
@Mixin(Mob.class)
public interface MobAccessor extends io.lunararcdevs.lunararc.common.bridge.access.MobAccessBridge {
    @Accessor("persistenceRequired") void lunararc$setPersistenceRequired(boolean value);
    @Accessor("bodyArmorDropChance") float lunararc$getBodyArmorDropChance();
    @Accessor("bodyArmorDropChance") void lunararc$setBodyArmorDropChance(float value);
    @Invoker("getAmbientSound") SoundEvent lunararc$invokeGetAmbientSound();
}
