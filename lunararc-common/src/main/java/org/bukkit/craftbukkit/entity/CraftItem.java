package org.bukkit.craftbukkit.entity;

import io.lunararcdevs.lunararc.common.bridge.ItemEntityBridge;
import io.lunararcdevs.lunararc.common.bridge.access.ItemEntityAccessBridge;
import net.kyori.adventure.util.TriState;
import net.minecraft.world.entity.item.ItemEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;


public final class CraftItem extends CraftEntity implements org.bukkit.entity.Item {
    private static final int NO_AGE_TIME = Short.MIN_VALUE;
    private static final int NO_PICKUP_TIME = Short.MAX_VALUE;

    public CraftItem(CraftServer server, ItemEntity entity) {
        super(server, entity);
    }

    @Override
    public ItemEntity getHandle() {
        return (ItemEntity) super.getHandle();
    }

    private ItemEntityAccessBridge access() {
        return (ItemEntityAccessBridge) getHandle();
    }

    private ItemEntityBridge bridgeItem() {
        return (ItemEntityBridge) getHandle();
    }

    @Override
    public @NotNull ItemStack getItemStack() {
        return CraftItemStack.asBukkitCopy(getHandle().getItem());
    }

    @Override
    public void setItemStack(@NotNull ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        getHandle().setItem(CraftItemStack.asNMSCopy(stack));
    }

    @Override
    public int getPickupDelay() {
        return access().lunararc$getPickupDelay();
    }

    @Override
    public void setPickupDelay(int delay) {
        access().lunararc$setPickupDelay(Math.min(NO_PICKUP_TIME, Math.max(0, delay)));
    }

    @Override
    public void setUnlimitedLifetime(boolean unlimited) {
        if (unlimited) getHandle().setUnlimitedLifetime();
        else if (access().lunararc$getAge() == NO_AGE_TIME) access().lunararc$setAge(0);
    }

    @Override
    public boolean isUnlimitedLifetime() {
        return access().lunararc$getAge() == NO_AGE_TIME;
    }

    @Override
    public void setOwner(@Nullable UUID owner) {
        getHandle().setTarget(owner);
    }

    @Override
    public @Nullable UUID getOwner() {
        return access().lunararc$getTarget();
    }

    @Override
    public void setThrower(@Nullable UUID uuid) {
        access().lunararc$setThrower(uuid);
    }

    @Override
    public @Nullable UUID getThrower() {
        return access().lunararc$getThrower();
    }

    @Override
    public boolean canMobPickup() {
        return bridgeItem().lunararc$canMobPickup();
    }

    @Override
    public void setCanMobPickup(boolean canMobPickup) {
        bridgeItem().lunararc$setCanMobPickup(canMobPickup);
    }

    @Override
    public boolean canPlayerPickup() {
        return access().lunararc$getPickupDelay() != NO_PICKUP_TIME;
    }

    @Override
    public void setCanPlayerPickup(boolean canPlayerPickup) {
        access().lunararc$setPickupDelay(canPlayerPickup ? 0 : NO_PICKUP_TIME);
    }

    @Override
    public boolean willAge() {
        return access().lunararc$getAge() != NO_AGE_TIME;
    }

    @Override
    public void setWillAge(boolean willAge) {
        access().lunararc$setAge(willAge ? 0 : NO_AGE_TIME);
    }

    @Override
    public int getHealth() {
        return access().lunararc$getHealth();
    }

    @Override
    public void setHealth(int health) {
        if (health <= 0) {
            getHandle().discard();
        } else {
            access().lunararc$setHealth(health);
        }
    }

    @Override
    public @NotNull TriState getFrictionState() {
        return bridgeItem().lunararc$getFrictionState();
    }

    @Override
    public void setFrictionState(@NotNull TriState state) {
        bridgeItem().lunararc$setFrictionState(Objects.requireNonNull(state, "state"));
    }
}
