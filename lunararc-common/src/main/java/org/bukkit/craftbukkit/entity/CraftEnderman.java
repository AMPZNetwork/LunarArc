package org.bukkit.craftbukkit.entity;

import io.lunararcdevs.lunararc.common.bridge.entity.EndermanBridge;
import java.util.Objects;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.Enderman;
import org.bukkit.material.MaterialData;

public final class CraftEnderman extends CraftMonster implements Enderman {
    public CraftEnderman(CraftServer server, net.minecraft.world.entity.monster.EnderMan entity) { super(server, entity); }
    @Override public net.minecraft.world.entity.monster.EnderMan getHandle() { return (net.minecraft.world.entity.monster.EnderMan) this.entity; }
    private EndermanBridge endermanBridge() { return (EndermanBridge) (Object) getHandle(); }

    @Override public MaterialData getCarriedMaterial() {
        BlockState state = getHandle().getCarriedBlock();
        return state == null ? Material.AIR.getNewData((byte) 0) : CraftMagicNumbers.getMaterial(state);
    }
    @Override public BlockData getCarriedBlock() {
        BlockState state = getHandle().getCarriedBlock();
        return state == null ? null : CraftBlockData.fromData(state);
    }
    @Override public void setCarriedMaterial(MaterialData data) { getHandle().setCarriedBlock(CraftMagicNumbers.getBlock(data)); }
    @Override public void setCarriedBlock(BlockData data) {
        getHandle().setCarriedBlock(data == null ? null : ((CraftBlockData) data).getState());
    }

    @Override public boolean isScreaming() { return endermanBridge().lunararc$isCreepy(); }
    @Override public void setScreaming(boolean screaming) { endermanBridge().lunararc$setCreepy(screaming); }
    @Override public boolean hasBeenStaredAt() { return endermanBridge().lunararc$hasBeenStaredAt(); }
    @Override public void setHasBeenStaredAt(boolean staredAt) { endermanBridge().lunararc$setHasBeenStaredAt(staredAt); }
    @Override public boolean teleportRandomly() { return endermanBridge().lunararc$teleportRandomly(); }
    @Override public boolean teleport() { return endermanBridge().lunararc$teleportRandomly(); }
    @Override public boolean teleportTowards(org.bukkit.entity.Entity entity) {
        Objects.requireNonNull(entity, "entity");
        if (!(entity instanceof CraftEntity craft)) throw new IllegalArgumentException("Entity is not backed by LunarArc CraftEntity");
        return endermanBridge().lunararc$teleportTowards(craft.getHandle());
    }
    @Override public String toString() { return "CraftEnderman"; }
}
