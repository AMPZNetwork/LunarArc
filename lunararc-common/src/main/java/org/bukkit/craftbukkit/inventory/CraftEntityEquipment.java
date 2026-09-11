package org.bukkit.craftbukkit.inventory;

import java.util.Objects;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Live EntityEquipment view over the loader-owned NMS LivingEntity equipment slots. */
public final class CraftEntityEquipment implements EntityEquipment {
    private final CraftLivingEntity entity;

    public CraftEntityEquipment(CraftLivingEntity entity) {
        this.entity = Objects.requireNonNull(entity, "entity");
    }

    @Override public void setItem(@NotNull EquipmentSlot slot, @Nullable ItemStack item) { setItem(slot, item, false); }
    @Override public void setItem(@NotNull EquipmentSlot slot, @Nullable ItemStack item, boolean silent) {
        setEquipment(CraftEquipmentSlot.getNMS(slot), item);
    }
    @Override public @NotNull ItemStack getItem(@NotNull EquipmentSlot slot) { return getEquipment(CraftEquipmentSlot.getNMS(slot)); }
    @Override public @NotNull ItemStack getItemInMainHand() { return getEquipment(net.minecraft.world.entity.EquipmentSlot.MAINHAND); }
    @Override public void setItemInMainHand(@Nullable ItemStack item) { setItemInMainHand(item, false); }
    @Override public void setItemInMainHand(@Nullable ItemStack item, boolean silent) { setEquipment(net.minecraft.world.entity.EquipmentSlot.MAINHAND, item); }
    @Override public @NotNull ItemStack getItemInOffHand() { return getEquipment(net.minecraft.world.entity.EquipmentSlot.OFFHAND); }
    @Override public void setItemInOffHand(@Nullable ItemStack item) { setItemInOffHand(item, false); }
    @Override public void setItemInOffHand(@Nullable ItemStack item, boolean silent) { setEquipment(net.minecraft.world.entity.EquipmentSlot.OFFHAND, item); }
    @Override @Deprecated public @NotNull ItemStack getItemInHand() { return getItemInMainHand(); }
    @Override @Deprecated public void setItemInHand(@Nullable ItemStack stack) { setItemInMainHand(stack); }
    @Override public @Nullable ItemStack getHelmet() { return nullableEquipment(net.minecraft.world.entity.EquipmentSlot.HEAD); }
    @Override public void setHelmet(@Nullable ItemStack item) { setHelmet(item, false); }
    @Override public void setHelmet(@Nullable ItemStack item, boolean silent) { setEquipment(net.minecraft.world.entity.EquipmentSlot.HEAD, item); }
    @Override public @Nullable ItemStack getChestplate() { return nullableEquipment(net.minecraft.world.entity.EquipmentSlot.CHEST); }
    @Override public void setChestplate(@Nullable ItemStack item) { setChestplate(item, false); }
    @Override public void setChestplate(@Nullable ItemStack item, boolean silent) { setEquipment(net.minecraft.world.entity.EquipmentSlot.CHEST, item); }
    @Override public @Nullable ItemStack getLeggings() { return nullableEquipment(net.minecraft.world.entity.EquipmentSlot.LEGS); }
    @Override public void setLeggings(@Nullable ItemStack item) { setLeggings(item, false); }
    @Override public void setLeggings(@Nullable ItemStack item, boolean silent) { setEquipment(net.minecraft.world.entity.EquipmentSlot.LEGS, item); }
    @Override public @Nullable ItemStack getBoots() { return nullableEquipment(net.minecraft.world.entity.EquipmentSlot.FEET); }
    @Override public void setBoots(@Nullable ItemStack item) { setBoots(item, false); }
    @Override public void setBoots(@Nullable ItemStack item, boolean silent) { setEquipment(net.minecraft.world.entity.EquipmentSlot.FEET, item); }

    @Override
    public @NotNull ItemStack[] getArmorContents() {
        return new ItemStack[] {
                getEquipment(net.minecraft.world.entity.EquipmentSlot.FEET),
                getEquipment(net.minecraft.world.entity.EquipmentSlot.LEGS),
                getEquipment(net.minecraft.world.entity.EquipmentSlot.CHEST),
                getEquipment(net.minecraft.world.entity.EquipmentSlot.HEAD)
        };
    }

    @Override
    public void setArmorContents(@NotNull ItemStack[] items) {
        Objects.requireNonNull(items, "items");
        setEquipment(net.minecraft.world.entity.EquipmentSlot.FEET, items.length > 0 ? items[0] : null);
        setEquipment(net.minecraft.world.entity.EquipmentSlot.LEGS, items.length > 1 ? items[1] : null);
        setEquipment(net.minecraft.world.entity.EquipmentSlot.CHEST, items.length > 2 ? items[2] : null);
        setEquipment(net.minecraft.world.entity.EquipmentSlot.HEAD, items.length > 3 ? items[3] : null);
    }

    @Override public void clear() {
        for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
            setEquipment(slot, null);
        }
    }

    @Override public @Nullable Entity getHolder() { return entity; }
    @Override @Deprecated public float getItemInHandDropChance() { return getItemInMainHandDropChance(); }
    @Override @Deprecated public void setItemInHandDropChance(float chance) { setItemInMainHandDropChance(chance); }
    @Override public float getItemInMainHandDropChance() { return getDropChance(EquipmentSlot.HAND); }
    @Override public void setItemInMainHandDropChance(float chance) { setDropChance(EquipmentSlot.HAND, chance); }
    @Override public float getItemInOffHandDropChance() { return getDropChance(EquipmentSlot.OFF_HAND); }
    @Override public void setItemInOffHandDropChance(float chance) { setDropChance(EquipmentSlot.OFF_HAND, chance); }
    @Override public float getHelmetDropChance() { return getDropChance(EquipmentSlot.HEAD); }
    @Override public void setHelmetDropChance(float chance) { setDropChance(EquipmentSlot.HEAD, chance); }
    @Override public float getChestplateDropChance() { return getDropChance(EquipmentSlot.CHEST); }
    @Override public void setChestplateDropChance(float chance) { setDropChance(EquipmentSlot.CHEST, chance); }
    @Override public float getLeggingsDropChance() { return getDropChance(EquipmentSlot.LEGS); }
    @Override public void setLeggingsDropChance(float chance) { setDropChance(EquipmentSlot.LEGS, chance); }
    @Override public float getBootsDropChance() { return getDropChance(EquipmentSlot.FEET); }
    @Override public void setBootsDropChance(float chance) { setDropChance(EquipmentSlot.FEET, chance); }

    @Override
    public float getDropChance(@NotNull EquipmentSlot slot) {
        net.minecraft.world.entity.Mob mob = mob();
        net.minecraft.world.entity.EquipmentSlot nms = CraftEquipmentSlot.getNMS(slot);
        return switch (nms.getType()) {
            case HAND -> mob.handDropChances[nms.getIndex()];
            case HUMANOID_ARMOR -> mob.armorDropChances[nms.getIndex()];
            case ANIMAL_ARMOR -> ((io.lunararcdevs.lunararc.common.bridge.access.MobAccessBridge) mob).lunararc$getBodyArmorDropChance();
        };
    }

    @Override
    public void setDropChance(@NotNull EquipmentSlot slot, float chance) {
        if (!Float.isFinite(chance) || chance < 0.0F) throw new IllegalArgumentException("chance must be finite and >= 0");
        net.minecraft.world.entity.Mob mob = mob();
        net.minecraft.world.entity.EquipmentSlot nms = CraftEquipmentSlot.getNMS(slot);
        switch (nms.getType()) {
            case HAND -> mob.handDropChances[nms.getIndex()] = chance;
            case HUMANOID_ARMOR -> mob.armorDropChances[nms.getIndex()] = chance;
            case ANIMAL_ARMOR -> ((io.lunararcdevs.lunararc.common.bridge.access.MobAccessBridge) mob).lunararc$setBodyArmorDropChance(chance);
        }
    }

    private net.minecraft.world.entity.Mob mob() {
        if (!(entity.getHandle() instanceof net.minecraft.world.entity.Mob mob)) {
            throw new UnsupportedOperationException("Drop chances are only supported for Mob entities");
        }
        return mob;
    }

    private @NotNull ItemStack getEquipment(net.minecraft.world.entity.EquipmentSlot slot) {
        return CraftItemStack.asBukkitCopy(entity.getHandle().getItemBySlot(slot));
    }

    private @Nullable ItemStack nullableEquipment(net.minecraft.world.entity.EquipmentSlot slot) {
        net.minecraft.world.item.ItemStack stack = entity.getHandle().getItemBySlot(slot);
        return stack.isEmpty() ? null : CraftItemStack.asBukkitCopy(stack);
    }

    private void setEquipment(net.minecraft.world.entity.EquipmentSlot slot, @Nullable ItemStack item) {
        entity.getHandle().setItemSlot(slot, CraftItemStack.asNMSCopy(item));
    }
}
