package io.lunararcdevs.lunararc.common.mixin.core.server;

import io.lunararcdevs.lunararc.common.server.LunarArcTrackingRange;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin_TrackingRange {

    @ModifyVariable(
            method = "addEntity",
            index = 3,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;updateInterval()I"),
            require = 0)
    private int lunararc$trackingRange(int defaultRange, Entity entity) {
        return LunarArcTrackingRange.getEntityTrackingRange(entity, defaultRange);
    }
}
