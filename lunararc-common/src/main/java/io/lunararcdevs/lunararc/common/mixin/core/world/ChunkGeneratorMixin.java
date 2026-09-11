package io.lunararcdevs.lunararc.common.mixin.core.world;

import io.lunararcdevs.lunararc.common.mod.util.LunarArcChunkPopulators;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.bukkit.craftbukkit.generator.CustomChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds Bukkit populators after native decoration for loader-owned generators. */
@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {
    @Inject(method = "applyBiomeDecoration", at = @At("TAIL"))
    private void lunararc$runBukkitPopulators(WorldGenLevel level, ChunkAccess chunk, StructureManager structures, CallbackInfo ci) {
        // CustomChunkGenerator calls this helper itself so populators still run when vanilla decorations are disabled.
        if ((Object) this instanceof CustomChunkGenerator) return;
        LunarArcChunkPopulators.populate(level, chunk);
    }
}
