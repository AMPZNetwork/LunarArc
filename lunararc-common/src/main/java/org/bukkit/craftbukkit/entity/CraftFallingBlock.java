package org.bukkit.craftbukkit.entity;

import io.lunararcdevs.lunararc.common.bridge.FallingBlockBridge;
import java.util.Objects;
import net.minecraft.world.entity.item.FallingBlockEntity;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.entity.FallingBlock;

public final class CraftFallingBlock extends CraftEntity implements FallingBlock {
    public CraftFallingBlock(CraftServer server, FallingBlockEntity entity) {
        super(server, entity);
    }

    @Override
    public FallingBlockEntity getHandle() {
        return (FallingBlockEntity) this.entity;
    }

    private FallingBlockBridge fallingBridge() {
        return (FallingBlockBridge) this.entity;
    }

    @Override
    @Deprecated
    public Material getMaterial() {
        return getBlockData().getMaterial();
    }

    @Override
    public BlockData getBlockData() {
        return CraftBlockData.createData(fallingBridge().lunararc$getBlockState());
    }

    @Override
    public void setBlockData(BlockData blockData) {
        Objects.requireNonNull(blockData, "blockData");
        CraftBlockData craft = blockData instanceof CraftBlockData value ? value : CraftBlockData.parse(blockData.getAsString());
        fallingBridge().lunararc$setBlockState(craft.getState());
        fallingBridge().lunararc$setBlockData(null);
    }

    @Override
    public BlockState getBlockState() {
        return CraftBlockState.unplaced(fallingBridge().lunararc$getBlockState());
    }

    @Override
    public void setBlockState(BlockState blockState) {
        Objects.requireNonNull(blockState, "blockState");
        setBlockData(blockState.getBlockData());
        if (blockState instanceof CraftBlockEntityState<?> tile) {
            fallingBridge().lunararc$setBlockData(tile.getSnapshotNBT());
        }
    }

    @Override public boolean getDropItem() { return fallingBridge().lunararc$getDropItem(); }
    @Override public void setDropItem(boolean drop) { fallingBridge().lunararc$setDropItem(drop); }
    @Override public boolean getCancelDrop() { return fallingBridge().lunararc$getCancelDrop(); }
    @Override public void setCancelDrop(boolean cancelDrop) { fallingBridge().lunararc$setCancelDrop(cancelDrop); }
    @Override public boolean canHurtEntities() { return fallingBridge().lunararc$getHurtEntities(); }
    @Override public void setHurtEntities(boolean hurtEntities) { fallingBridge().lunararc$setHurtEntities(hurtEntities); }

    @Override
    public void setTicksLived(int value) {
        super.setTicksLived(value);
        fallingBridge().lunararc$setTime(value);
    }

    @Override public float getDamagePerBlock() { return fallingBridge().lunararc$getDamagePerDistance(); }
    @Override public void setDamagePerBlock(float damage) {
        if (damage < 0.0F) throw new IllegalArgumentException("damage must be >= 0.0");
        fallingBridge().lunararc$setDamagePerDistance(damage);
        if (damage > 0.0F) setHurtEntities(true);
    }
    @Override public int getMaxDamage() { return fallingBridge().lunararc$getMaxDamage(); }
    @Override public void setMaxDamage(int damage) {
        if (damage < 0) throw new IllegalArgumentException("damage must be >= 0");
        fallingBridge().lunararc$setMaxDamage(damage);
        if (damage > 0) setHurtEntities(true);
    }
    @Override public boolean doesAutoExpire() { return fallingBridge().lunararc$getAutoExpire(); }
    @Override public void shouldAutoExpire(boolean autoExpires) { fallingBridge().lunararc$setAutoExpire(autoExpires); }
}
