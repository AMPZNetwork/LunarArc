package io.lunararcdevs.lunararc.common.bridge.access;

import net.minecraft.world.level.LevelSettings;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface PrimaryLevelDataAccessBridge {
    LevelSettings lunararc$getSettings();
    void lunararc$setSettings(LevelSettings settings);
}
