package io.lunararcdevs.lunararc.common.mixin.core.world;

import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(PrimaryLevelData.class)
public interface PrimaryLevelDataAccessor extends io.lunararcdevs.lunararc.common.bridge.access.PrimaryLevelDataAccessBridge {
    @Accessor("settings")
    LevelSettings lunararc$getSettings();

    @Accessor("settings")
    void lunararc$setSettings(LevelSettings settings);
}
