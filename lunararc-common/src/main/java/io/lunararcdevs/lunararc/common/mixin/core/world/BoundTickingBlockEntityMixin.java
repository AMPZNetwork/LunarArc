package io.lunararcdevs.lunararc.common.mixin.core.world;

import io.lunararcdevs.lunararc.common.mod.server.LunarArcTickingTrackerImpl;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// remap = false to match this project's other inner-class string targets (Slime$SlimeFloatGoal and
// friends): the runtime is Mojang-mapped, so the target name and members are already correct.
@Mixin(targets = "net.minecraft.world.level.chunk.LevelChunk$BoundTickingBlockEntity", remap = false)
public abstract class BoundTickingBlockEntityMixin {

    @Shadow @Final private BlockEntity blockEntity;

    @Inject(method = "tick", at = @At("HEAD"), require = 0)
    private void lunararc$pushTickingBlockEntity(CallbackInfo ci) {
        LunarArcTickingTrackerImpl.pushBlockEntity(this.blockEntity);
    }

    @Inject(method = "tick", at = @At("RETURN"), require = 0)
    private void lunararc$popTickingBlockEntity(CallbackInfo ci) {
        LunarArcTickingTrackerImpl.pop();
    }
}
