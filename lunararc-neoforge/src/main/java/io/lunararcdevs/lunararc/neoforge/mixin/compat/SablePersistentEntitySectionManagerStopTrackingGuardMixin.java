package io.lunararcdevs.lunararc.neoforge.mixin.compat;

import io.lunararcdevs.lunararc.common.compat.SableCompatibility;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PersistentEntitySectionManager.class)
public abstract class SablePersistentEntitySectionManagerStopTrackingGuardMixin {

    @Redirect(
            method = "stopTracking",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/entity/EntityLookup;remove(Lnet/minecraft/world/level/entity/EntityAccess;)V"))
    private void lunararc$safeEntityLookupRemove(EntityLookup<EntityAccess> instance, EntityAccess entity) {
        SableCompatibility.safeEntityLookupRemove(() -> instance.remove(entity));
    }
}
