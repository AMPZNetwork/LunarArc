package io.lunararcdevs.lunararc.neoforge.mixin.compat;

import io.lunararcdevs.lunararc.common.compat.SableCompatibility;
import java.util.function.Consumer;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "dev.ryanhcode.sable.util.SubLevelInclusiveLevelEntityGetter", remap = false)
public abstract class SableEntityQueryMixin {
    @Inject(method = "get(Lnet/minecraft/world/phys/AABB;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private void lunararc$guardEntityQuery(AABB bounds, Consumer<EntityAccess> consumer, CallbackInfo ci) {
        if (!SableCompatibility.allowEntityQuery(bounds)) ci.cancel();
    }

    @Inject(method = "get(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/util/AbortableIterationConsumer;)V", at = @At("HEAD"), cancellable = true, require = 1, remap = false)
    private void lunararc$guardTypedEntityQuery(EntityTypeTest<?, ?> type, AABB bounds,
            AbortableIterationConsumer<?> consumer, CallbackInfo ci) {
        if (!SableCompatibility.allowEntityQuery(bounds)) ci.cancel();
    }
}
