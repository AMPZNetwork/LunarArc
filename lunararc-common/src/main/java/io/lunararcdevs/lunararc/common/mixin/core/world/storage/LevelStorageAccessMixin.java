package io.lunararcdevs.lunararc.common.mixin.core.world.storage;

import io.lunararcdevs.lunararc.common.bridge.storage.LevelStorageSourceBridge;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LevelStorageSource.LevelStorageAccess.class)
public class LevelStorageAccessMixin implements LevelStorageSourceBridge.AccessBridge {
    @Unique private ResourceKey<LevelStem> lunararc$dimensionType;

    @Override
    public void lunararc$setDimensionType(ResourceKey<LevelStem> typeKey) {
        this.lunararc$dimensionType = typeKey;
    }

    @Override
    public ResourceKey<LevelStem> lunararc$getDimensionType() {
        return this.lunararc$dimensionType;
    }

    @ModifyVariable(method = "getDimensionPath", argsOnly = true, ordinal = 0, at = @At("HEAD"))
    private ResourceKey<Level> lunararc$useActualDimension(ResourceKey<Level> original) {
        ResourceKey<LevelStem> type = this.lunararc$dimensionType;
        return type == null ? original : ResourceKey.create(Registries.DIMENSION, type.location());
    }
}
