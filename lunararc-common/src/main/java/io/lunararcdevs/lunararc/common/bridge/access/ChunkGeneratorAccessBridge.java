package io.lunararcdevs.lunararc.common.bridge.access;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import java.util.function.Function;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface ChunkGeneratorAccessBridge {
    Function<Holder<Biome>, BiomeGenerationSettings> lunararc$getGenerationSettingsGetter();
}
