package io.lunararcdevs.lunararc.common.mixin.core.world;

import io.lunararcdevs.lunararc.common.bridge.BlockEntityBridge;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements BlockEntityBridge {
    @Unique
    private CraftPersistentDataContainer lunararc$persistentDataContainer;

    @Override
    public CraftPersistentDataContainer lunararc$getPersistentDataContainer() {
        CraftPersistentDataContainer container = this.lunararc$persistentDataContainer;
        if (container == null) {
            container = new CraftPersistentDataContainer();
            this.lunararc$persistentDataContainer = container;
        }
        return container;
    }

    @Inject(
            method = "loadAdditional(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V",
            at = @At("RETURN"))
    private void lunararc$loadPersistentData(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        CraftPersistentDataContainer container = new CraftPersistentDataContainer();
        if (tag.contains("PublicBukkitValues")) {
            container.putAll(tag.getCompound("PublicBukkitValues"));
        }
        this.lunararc$persistentDataContainer = container;
    }

    @Inject(
            method = "saveAdditional(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V",
            at = @At("RETURN"))
    private void lunararc$savePersistentData(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        CraftPersistentDataContainer container = this.lunararc$persistentDataContainer;
        if (container != null && !container.isEmpty()) {
            tag.put("PublicBukkitValues", container.toTagCompound());
        }
    }
}
