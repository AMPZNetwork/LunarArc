package org.bukkit.craftbukkit.entity;

import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import io.lunararcdevs.lunararc.common.bridge.FireworkRocketBridge;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.Nullable;

public final class CraftFirework extends CraftProjectile implements Firework {
    private final Random random = new Random();

    public CraftFirework(CraftServer server, FireworkRocketEntity entity) {
        super(server, entity);
    }

    @Override
    public FireworkRocketEntity getHandle() {
        return (FireworkRocketEntity) this.entity;
    }

    private FireworkRocketBridge fireworkBridge() {
        return (FireworkRocketBridge) this.entity;
    }

    @Override
    public FireworkMeta getFireworkMeta() {
        org.bukkit.inventory.meta.ItemMeta meta = getItem().getItemMeta();
        if (!(meta instanceof FireworkMeta fireworkMeta)) throw new IllegalStateException("Firework item has no FireworkMeta");
        return fireworkMeta;
    }

    @Override
    public void setFireworkMeta(FireworkMeta meta) {
        Objects.requireNonNull(meta, "meta");
        ItemStack item = getItem();
        if (!item.setItemMeta(meta)) throw new IllegalArgumentException("FireworkMeta cannot be applied to firework rocket");
        setItem(item);
        fireworkBridge().lunararc$setLifetime(10 * (1 + meta.getPower()) + random.nextInt(6) + random.nextInt(7));
    }

    @Override
    public boolean setAttachedTo(@Nullable LivingEntity entity) {
        if (isDetonated()) return false;
        if (entity == null) {
            fireworkBridge().lunararc$setAttachedEntity(null);
            return true;
        }
        if (!(entity instanceof CraftEntity craft) || !(craft.getHandle() instanceof net.minecraft.world.entity.LivingEntity living)) {
            throw new IllegalArgumentException("LivingEntity is not backed by LunarArc");
        }
        fireworkBridge().lunararc$setAttachedEntity(living);
        return true;
    }

    @Override
    public @Nullable LivingEntity getAttachedTo() {
        net.minecraft.world.entity.LivingEntity attached = fireworkBridge().lunararc$getAttachedEntity();
        if (attached == null) return null;
        org.bukkit.entity.Entity bukkit = ((EntityBridge) attached).lunararc$getBukkitEntity();
        return bukkit instanceof LivingEntity living ? living : null;
    }

    @Override
    @Deprecated
    public int getLife() {
        return fireworkBridge().lunararc$getLife();
    }

    @Override
    @Deprecated
    public int getMaxLife() {
        return fireworkBridge().lunararc$getLifetime();
    }

    @Override
    @Deprecated
    public boolean setLife(int ticks) {
        if (ticks < 0) throw new IllegalArgumentException("ticks must be greater than or equal to 0");
        if (isDetonated()) return false;
        fireworkBridge().lunararc$setLife(ticks);
        return true;
    }

    @Override
    @Deprecated
    public boolean setMaxLife(int ticks) {
        if (ticks <= 0) throw new IllegalArgumentException("ticks must be greater than 0");
        if (isDetonated()) return false;
        fireworkBridge().lunararc$setLifetime(ticks);
        return true;
    }

    @Override
    public void detonate() {
        fireworkBridge().lunararc$setLife(getTicksToDetonate() + 1);
    }

    @Override
    public boolean isDetonated() {
        return fireworkBridge().lunararc$getLife() > fireworkBridge().lunararc$getLifetime();
    }

    @Override
    public boolean isShotAtAngle() {
        return fireworkBridge().lunararc$isShotAtAngle();
    }

    @Override
    public void setShotAtAngle(boolean shotAtAngle) {
        fireworkBridge().lunararc$setShotAtAngle(shotAtAngle);
    }

    @Override
    public @Nullable UUID getSpawningEntity() {
        return fireworkBridge().lunararc$getSpawningEntity();
    }

    @Override
    public ItemStack getItem() {
        return CraftItemStack.asBukkitCopy(fireworkBridge().lunararc$getItem());
    }

    @Override
    public void setItem(@Nullable ItemStack itemStack) {
        net.minecraft.world.item.ItemStack item = itemStack == null
                ? new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FIREWORK_ROCKET)
                : CraftItemStack.asNMSCopy(itemStack);
        if (!item.is(net.minecraft.world.item.Items.FIREWORK_ROCKET)) {
            throw new IllegalArgumentException("Firework item must be a firework rocket");
        }
        fireworkBridge().lunararc$setItem(item);
    }

    @Override
    public int getTicksFlown() {
        return fireworkBridge().lunararc$getLife();
    }

    @Override
    public void setTicksFlown(int ticks) {
        if (ticks < 0) throw new IllegalArgumentException("ticks must be greater than or equal to 0");
        fireworkBridge().lunararc$setLife(ticks);
    }

    @Override
    public int getTicksToDetonate() {
        return fireworkBridge().lunararc$getLifetime();
    }

    @Override
    public void setTicksToDetonate(int ticks) {
        if (ticks <= 0) throw new IllegalArgumentException("ticks must be greater than 0");
        fireworkBridge().lunararc$setLifetime(ticks);
    }
}
