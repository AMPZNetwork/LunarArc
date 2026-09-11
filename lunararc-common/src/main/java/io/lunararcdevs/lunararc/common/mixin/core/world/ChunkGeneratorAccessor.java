package io.lunararcdevs.lunararc.common.mixin.core.world;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;

@Mixin(ChunkGenerator.class)
public interface ChunkGeneratorAccessor extends io.lunararcdevs.lunararc.common.bridge.access.ChunkGeneratorAccessBridge {
    @Accessor("generationSettingsGetter")
    Function<Holder<Biome>, BiomeGenerationSettings> lunararc$getGenerationSettingsGetter();
}
