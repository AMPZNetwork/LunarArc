package io.lunararcdevs.lunararc.common.mixin.core.entity;

import io.lunararcdevs.lunararc.common.bridge.FallingBlockBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin implements FallingBlockBridge {
    @Shadow private BlockState blockState;
    @Shadow private @Nullable CompoundTag blockData;
    @Shadow public int time;
    @Shadow public boolean dropItem;
    @Shadow public boolean cancelDrop;
    @Shadow private boolean hurtEntities;
    @Shadow private float fallDamagePerDistance;
    @Shadow private int fallDamageMax;

    /** Paper-only state; vanilla has no matching field. */
    @Unique private boolean lunararc$autoExpire = true;

    @Override public BlockState lunararc$getBlockState() { return this.blockState; }
    @Override public void lunararc$setBlockState(BlockState state) { this.blockState = state; }
    @Override public @Nullable CompoundTag lunararc$getBlockData() { return this.blockData; }
    @Override public void lunararc$setBlockData(@Nullable CompoundTag tag) { this.blockData = tag; }
    @Override public int lunararc$getTime() { return this.time; }
    @Override public void lunararc$setTime(int time) { this.time = time; }
    @Override public boolean lunararc$getDropItem() { return this.dropItem; }
    @Override public void lunararc$setDropItem(boolean value) { this.dropItem = value; }
    @Override public boolean lunararc$getCancelDrop() { return this.cancelDrop; }
    @Override public void lunararc$setCancelDrop(boolean value) { this.cancelDrop = value; }
    @Override public boolean lunararc$getHurtEntities() { return this.hurtEntities; }
    @Override public void lunararc$setHurtEntities(boolean value) { this.hurtEntities = value; }
    @Override public float lunararc$getDamagePerDistance() { return this.fallDamagePerDistance; }
    @Override public void lunararc$setDamagePerDistance(float value) { this.fallDamagePerDistance = value; }
    @Override public int lunararc$getMaxDamage() { return this.fallDamageMax; }
    @Override public void lunararc$setMaxDamage(int value) { this.fallDamageMax = value; }
    @Override public boolean lunararc$getAutoExpire() { return this.lunararc$autoExpire; }
    @Override public void lunararc$setAutoExpire(boolean value) { this.lunararc$autoExpire = value; }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 100), require = 0)
    private int lunararc$outsideWorldExpiry(int original) {
        return this.lunararc$autoExpire ? original : Integer.MAX_VALUE;
    }

    @ModifyConstant(method = "tick", constant = @Constant(intValue = 600), require = 0)
    private int lunararc$normalExpiry(int original) {
        return this.lunararc$autoExpire ? original : Integer.MAX_VALUE;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"), require = 0)
    private void lunararc$saveAutoExpire(CompoundTag tag, CallbackInfo ci) {
        if (!this.lunararc$autoExpire) tag.putBoolean("Paper.AutoExpire", false);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"), require = 0)
    private void lunararc$loadAutoExpire(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("Paper.AutoExpire")) this.lunararc$autoExpire = tag.getBoolean("Paper.AutoExpire");
    }
}
