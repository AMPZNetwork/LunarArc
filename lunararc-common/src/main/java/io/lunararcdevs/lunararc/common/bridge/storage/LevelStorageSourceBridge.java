package io.lunararcdevs.lunararc.common.bridge.storage;

import java.io.IOException;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.validation.ContentValidationException;

public interface LevelStorageSourceBridge {
    LevelStorageSource.LevelStorageAccess lunararc$validateAndCreateAccess(String saveName, ResourceKey<LevelStem> dimension)
            throws IOException, ContentValidationException;

    LevelStorageSource.LevelStorageAccess lunararc$createAccess(String saveName, ResourceKey<LevelStem> dimension)
            throws IOException;

    interface AccessBridge {
        void lunararc$setDimensionType(ResourceKey<LevelStem> typeKey);
        ResourceKey<LevelStem> lunararc$getDimensionType();
    }
}
