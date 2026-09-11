package io.lunararcdevs.lunararc.common.mixin.core.world.storage;

import io.lunararcdevs.lunararc.common.bridge.storage.LevelStorageSourceBridge;
import java.io.IOException;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.validation.ContentValidationException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelStorageSource.class)
public abstract class LevelStorageSourceMixin implements LevelStorageSourceBridge {
    @Shadow public abstract LevelStorageSource.LevelStorageAccess validateAndCreateAccess(String saveName)
            throws IOException, ContentValidationException;

    @Shadow public abstract LevelStorageSource.LevelStorageAccess createAccess(String saveName) throws IOException;

    @Override
    public LevelStorageSource.LevelStorageAccess lunararc$validateAndCreateAccess(
            String saveName, ResourceKey<LevelStem> dimension) throws IOException, ContentValidationException {
        LevelStorageSource.LevelStorageAccess access = this.validateAndCreateAccess(saveName);
        ((LevelStorageSourceBridge.AccessBridge) (Object) access).lunararc$setDimensionType(dimension);
        return access;
    }

    @Override
    public LevelStorageSource.LevelStorageAccess lunararc$createAccess(String saveName, ResourceKey<LevelStem> dimension)
            throws IOException {
        LevelStorageSource.LevelStorageAccess access = this.createAccess(saveName);
        ((LevelStorageSourceBridge.AccessBridge) (Object) access).lunararc$setDimensionType(dimension);
        return access;
    }

    // Real Paper adds this exact overload (as part of a class it renames to Convertable) for its
    // own regen support; WorldEdit's PaperweightAdapter calls it directly for //regen and throws
    // NoSuchMethodError without it - confirmed, against the real Mojang mapping table, that vanilla
    // never has a two-arg createAccess at all. ResourceKey erases to a raw type in the bytecode
    // descriptor regardless of the generic parameter used here, so this matches WorldEdit's call
    // site exactly.
    public LevelStorageSource.LevelStorageAccess createAccess(String saveName, ResourceKey<LevelStem> dimension)
            throws IOException {
        return this.lunararc$createAccess(saveName, dimension);
    }
}
