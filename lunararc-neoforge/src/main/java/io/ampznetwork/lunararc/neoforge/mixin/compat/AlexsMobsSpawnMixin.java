package io.ampznetwork.lunararc.neoforge.mixin.compat;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.github.alexthe666.alexsmobs.world.AMWorldRegistry", remap = false)
public abstract class AlexsMobsSpawnMixin {
    @Inject(method = "addBiomeSpawns", at = @At("RETURN"), require = 1, remap = false)
    private static void lunararc$correctSkreecherCategory(Holder<Biome> biome,
            ModifiableBiomeInfo.BiomeInfo.Builder builder, CallbackInfo ci) {
        var settings = builder.getMobSpawnSettings();
        var iterator = settings.getSpawner(MobCategory.MONSTER).iterator();
        while (iterator.hasNext()) {
            var spawn = iterator.next();
            var key = BuiltInRegistries.ENTITY_TYPE.getKey(spawn.type);
            if (key != null && key.getNamespace().equals("alexsmobs") && key.getPath().equals("skreecher")
                    && spawn.type.getCategory() != MobCategory.MONSTER) {
                iterator.remove();
                settings.getSpawner(spawn.type.getCategory()).add(spawn);
            }
        }
    }
}
