package org.bukkit.craftbukkit.entity;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.Nullable;

public class CraftAbstractArrow extends CraftProjectile implements org.bukkit.entity.AbstractArrow {
    public CraftAbstractArrow(CraftServer server, AbstractArrow entity) {
        super(server, entity);
    }

    private io.lunararcdevs.lunararc.common.bridge.access.AbstractArrowAccessBridge arrowAccess() {
        return (io.lunararcdevs.lunararc.common.bridge.access.AbstractArrowAccessBridge) (Object) this.getHandle();
    }

    @Override
    public AbstractArrow getHandle() {
        return (AbstractArrow) this.entity;
    }

    @Override
    @Deprecated
    public void setKnockbackStrength(int knockbackStrength) {
        // Deprecated upstream: knockback is derived from the firing weapon in 1.21.1.
    }

    @Override
    @Deprecated
    public int getKnockbackStrength() {
        return 0;
    }

    @Override
    public double getDamage() {
        return this.getHandle().getBaseDamage();
    }

    @Override
    public void setDamage(double damage) {
        if (damage < 0.0D) throw new IllegalArgumentException("Damage must be non-negative");
        this.getHandle().setBaseDamage(damage);
    }

    @Override
    public int getPierceLevel() {
        return this.getHandle().getPierceLevel();
    }

    @Override
    public void setPierceLevel(int pierceLevel) {
        if (pierceLevel < 0 || pierceLevel > Byte.MAX_VALUE) {
            throw new IllegalArgumentException("Pierce level must be between 0 and 127");
        }
        this.getHandle().setPierceLevel((byte) pierceLevel);
    }

    @Override
    public boolean isCritical() {
        return this.getHandle().isCritArrow();
    }

    @Override
    public void setCritical(boolean critical) {
        this.getHandle().setCritArrow(critical);
    }

    @Override
    public boolean isInBlock() {
        return this.arrowAccess().lunararc$isInGround();
    }

    @Override
    public @Nullable Block getAttachedBlock() {
        if (!this.isInBlock()) return null;
        BlockPos pos = this.getHandle().blockPosition();
        return this.getWorld().getBlockAt(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public PickupStatus getPickupStatus() {
        return switch (this.arrowAccess().lunararc$getPickup()) {
            case DISALLOWED -> PickupStatus.DISALLOWED;
            case ALLOWED -> PickupStatus.ALLOWED;
            case CREATIVE_ONLY -> PickupStatus.CREATIVE_ONLY;
        };
    }

    @Override
    public void setPickupStatus(PickupStatus status) {
        Objects.requireNonNull(status, "status");
        this.arrowAccess().lunararc$setPickup(switch (status) {
            case DISALLOWED -> AbstractArrow.Pickup.DISALLOWED;
            case ALLOWED -> AbstractArrow.Pickup.ALLOWED;
            case CREATIVE_ONLY -> AbstractArrow.Pickup.CREATIVE_ONLY;
        });
    }

    @Override
    public void setTicksLived(int value) {
        super.setTicksLived(value);
        this.arrowAccess().lunararc$setLife(value);
    }

    @Override
    public boolean isShotFromCrossbow() {
        return this.arrowAccess().lunararc$shotFromCrossbow();
    }

    @Override
    @Deprecated
    public void setShotFromCrossbow(boolean shotFromCrossbow) {
        // Deprecated upstream; this is derived from the firing weapon.
    }

    @Override
    public ItemStack getItem() {
        return CraftItemStack.asBukkitCopy(this.arrowAccess().lunararc$getPickupItemStack());
    }

    @Override
    public void setItem(ItemStack item) {
        Objects.requireNonNull(item, "item");
        this.arrowAccess().lunararc$setPickupItemStack(CraftItemStack.asNMSCopy(item));
    }

    @Override
    public @Nullable ItemStack getWeapon() {
        net.minecraft.world.item.ItemStack weapon = this.arrowAccess().lunararc$getFiredFromWeapon();
        return weapon == null ? null : CraftItemStack.asBukkitCopy(weapon);
    }

    @Override
    public void setWeapon(ItemStack item) {
        Objects.requireNonNull(item, "item");
        this.arrowAccess().lunararc$setFiredFromWeapon(CraftItemStack.asNMSCopy(item));
    }

    @Override
    public ItemStack getItemStack() {
        return CraftItemStack.asBukkitCopy(this.arrowAccess().lunararc$getPickupItemStack());
    }

    @Override
    public void setItemStack(ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        this.arrowAccess().lunararc$setPickupItemStack(CraftItemStack.asNMSCopy(stack));
    }

    @Override
    public void setLifetimeTicks(int ticks) {
        this.arrowAccess().lunararc$setLife(ticks);
    }

    @Override
    public int getLifetimeTicks() {
        return this.arrowAccess().lunararc$getLife();
    }

    @Override
    public Sound getHitSound() {
        SoundEvent event = this.arrowAccess().lunararc$getSoundEvent();
        ResourceLocation key = BuiltInRegistries.SOUND_EVENT.getKey(event);
        Sound sound = key == null ? null : org.bukkit.Registry.SOUNDS.get(new NamespacedKey(key.getNamespace(), key.getPath()));
        if (sound == null) throw new IllegalStateException("No Bukkit sound registered for arrow hit sound " + key);
        return sound;
    }

    @Override
    public void setHitSound(Sound sound) {
        Objects.requireNonNull(sound, "sound");
        ResourceLocation key = ResourceLocation.fromNamespaceAndPath(sound.getKey().getNamespace(), sound.getKey().getKey());
        SoundEvent event = BuiltInRegistries.SOUND_EVENT.get(key);
        if (event == null) throw new IllegalArgumentException("Unknown sound " + sound.getKey());
        this.getHandle().setSoundEvent(event);
    }

    @Override
    public void setShooter(@Nullable ProjectileSource source, boolean resetPickupStatus) {
        AbstractArrow.Pickup pickup = this.arrowAccess().lunararc$getPickup();
        super.setShooter(source);
        if (!resetPickupStatus) {
            this.arrowAccess().lunararc$setPickup(pickup);
        }
    }

    @Override
    public String toString() {
        return "CraftAbstractArrow";
    }
}
