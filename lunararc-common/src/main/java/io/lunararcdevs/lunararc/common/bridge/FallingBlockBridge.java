package io.lunararcdevs.lunararc.common.bridge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface FallingBlockBridge {
    BlockState lunararc$getBlockState();
    void lunararc$setBlockState(BlockState state);
    @Nullable CompoundTag lunararc$getBlockData();
    void lunararc$setBlockData(@Nullable CompoundTag tag);
    int lunararc$getTime();
    void lunararc$setTime(int time);
    boolean lunararc$getDropItem();
    void lunararc$setDropItem(boolean value);
    boolean lunararc$getCancelDrop();
    void lunararc$setCancelDrop(boolean value);
    boolean lunararc$getHurtEntities();
    void lunararc$setHurtEntities(boolean value);
    float lunararc$getDamagePerDistance();
    void lunararc$setDamagePerDistance(float value);
    int lunararc$getMaxDamage();
    void lunararc$setMaxDamage(int value);
    boolean lunararc$getAutoExpire();
    void lunararc$setAutoExpire(boolean value);
}
