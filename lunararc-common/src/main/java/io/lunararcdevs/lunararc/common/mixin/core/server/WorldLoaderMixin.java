package io.lunararcdevs.lunararc.common.mixin.core.server;

import io.lunararcdevs.lunararc.common.mod.util.LunarArcWorldLoaderCapture;
import net.minecraft.server.WorldLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(WorldLoader.class)
public abstract class WorldLoaderMixin {
    @ModifyArg(
            method = "load",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/WorldLoader$WorldDataSupplier;get(Lnet/minecraft/server/WorldLoader$DataLoadContext;)Lnet/minecraft/server/WorldLoader$DataLoadOutput;"))
    private static WorldLoader.DataLoadContext lunararc$captureDataLoadContext(WorldLoader.DataLoadContext context) {
        LunarArcWorldLoaderCapture.capture(context);
        return context;
    }
}
