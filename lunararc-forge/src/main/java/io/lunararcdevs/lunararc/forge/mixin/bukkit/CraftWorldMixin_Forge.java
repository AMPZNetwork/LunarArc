package io.lunararcdevs.lunararc.forge.mixin.bukkit;

import net.minecraft.world.level.biome.Biome;
import org.bukkit.craftbukkit.CraftWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CraftWorld.class, remap = false)
public abstract class CraftWorldMixin_Forge {
    @Redirect(
            method = "getHumidity(III)D",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/level/biome/Biome;climateSettings:Lnet/minecraft/world/level/biome/Biome$ClimateSettings;",
                    remap = true))
    private Biome.ClimateSettings lunararc$useLoaderClimateSettings(Biome biome) {
        return biome.getModifiedClimateSettings();
    }
}
