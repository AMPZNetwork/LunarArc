package io.ampznetwork.lunararc.common.mixin.core.entity;

import io.ampznetwork.lunararc.common.bridge.EntityBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_21_R1.CraftServer;
import org.bukkit.craftbukkit.v1_21_R1.persistence.CraftPersistentDataContainer;
import org.bukkit.persistence.PersistentDataContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityBridge {

    @Shadow protected int portalCooldown;
    @Shadow public int remainingFireTicks;
    @Override public int lunararc$getPortalCooldown() { return this.portalCooldown; }
    @Override public void lunararc$setPortalCooldown(int cooldown) { this.portalCooldown = cooldown; }
    @Override public int lunararc$getRemainingFireTicks() { return this.remainingFireTicks; }
    @Override public void lunararc$setRemainingFireTicks(int ticks) { this.remainingFireTicks = ticks; }

    private org.bukkit.entity.Entity lunararc$bukkitEntity;
    @Override public void lunararc$setBukkitEntity(org.bukkit.entity.Entity entity) { this.lunararc$bukkitEntity = entity; }

    private CraftPersistentDataContainer lunararc$pdc = new CraftPersistentDataContainer();
    @Override public PersistentDataContainer lunararc$getPersistentDataContainer() { return lunararc$pdc; }
    @Override public org.bukkit.entity.Entity lunararc$getBukkitEntity() { 
        if (lunararc$bukkitEntity == null) {
            lunararc$bukkitEntity = CraftEntity.getEntity((CraftServer) org.bukkit.Bukkit.getServer(), (Entity) (Object) this); 
        }
        return lunararc$bukkitEntity;
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void lunararc$loadEntityData(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("BukkitValues")) {
            lunararc$pdc.fromTag(tag.getCompound("BukkitValues"));
        }
    }

    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void lunararc$saveEntityData(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag bukkitValues = lunararc$pdc.toTag();
        if (!bukkitValues.isEmpty()) {
            tag.put("BukkitValues", bukkitValues);
        }
    }
}
