package io.lunararcdevs.lunararc.common.mixin.core.world;

import io.lunararcdevs.lunararc.common.mod.server.LunarArcTickingTrackerImpl;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.server.level.ServerLevel.class)
public abstract class ServerLevelTickingMixin {

    @Inject(method = "tickNonPassenger", at = @At("HEAD"), require = 0)
    private void lunararc$pushTickingEntity(Entity entity, CallbackInfo ci) {
        LunarArcTickingTrackerImpl.pushEntity(entity);
    }

    @Inject(method = "tickNonPassenger", at = @At("RETURN"), require = 0)
    private void lunararc$popTickingEntity(Entity entity, CallbackInfo ci) {
        LunarArcTickingTrackerImpl.pop();
    }

    // Block-entity tick tracking lives in BoundTickingBlockEntityMixin. It was here, injecting
    // into ServerLevel#tickBlockEntities()V, but that method is declared on Level rather than
    // ServerLevel so Mixin never resolved it and the hook silently did nothing.
}
