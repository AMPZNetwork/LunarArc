package io.lunararcdevs.lunararc.common.mixin.core.entity;

import io.lunararcdevs.lunararc.common.bridge.entity.ItemBasedSteeringBridge;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.ItemBasedSteering;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemBasedSteering.class)
public abstract class ItemBasedSteeringMixin implements ItemBasedSteeringBridge {
    @Shadow @Final private SynchedEntityData entityData;
    @Shadow @Final private EntityDataAccessor<Integer> boostTimeAccessor;
    @Shadow private boolean boosting;
    @Shadow private int boostTime;

    @Override public boolean lunararc$isBoosting() { return this.boosting; }
    @Override public int lunararc$getBoostTime() { return this.boostTime; }
    @Override public void lunararc$setBoostTime(int ticks) { this.boostTime = ticks; }
    @Override public int lunararc$getBoostTimeTotal() { return this.entityData.get(this.boostTimeAccessor); }
    @Override public void lunararc$setBoostTicks(int ticks) {
        this.boosting = true;
        this.boostTime = 0;
        this.entityData.set(this.boostTimeAccessor, ticks);
    }
}
