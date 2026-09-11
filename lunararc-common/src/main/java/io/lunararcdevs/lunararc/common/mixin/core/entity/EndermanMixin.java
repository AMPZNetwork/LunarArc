package io.lunararcdevs.lunararc.common.mixin.core.entity;

import io.lunararcdevs.lunararc.common.bridge.entity.EndermanBridge;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EnderMan.class)
public abstract class EndermanMixin implements EndermanBridge {
    @Shadow @Final private static EntityDataAccessor<Boolean> DATA_CREEPY;
    @Shadow @Final private static EntityDataAccessor<Boolean> DATA_STARED_AT;
    @Invoker("teleport") public abstract boolean lunararc$invokeTeleport();
    @Invoker("teleportTowards") public abstract boolean lunararc$invokeTeleportTowards(Entity entity);

    @Override public boolean lunararc$teleportRandomly() { return lunararc$invokeTeleport(); }
    @Override public boolean lunararc$teleportTowards(Entity entity) { return lunararc$invokeTeleportTowards(entity); }
    @Override public boolean lunararc$isCreepy() { return ((EnderMan) (Object) this).getEntityData().get(DATA_CREEPY); }
    @Override public void lunararc$setCreepy(boolean creepy) { ((EnderMan) (Object) this).getEntityData().set(DATA_CREEPY, creepy); }
    @Override public boolean lunararc$hasBeenStaredAt() { return ((EnderMan) (Object) this).getEntityData().get(DATA_STARED_AT); }
    @Override public void lunararc$setHasBeenStaredAt(boolean staredAt) { ((EnderMan) (Object) this).getEntityData().set(DATA_STARED_AT, staredAt); }
}
