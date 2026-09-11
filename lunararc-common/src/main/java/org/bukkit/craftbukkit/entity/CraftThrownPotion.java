package org.bukkit.craftbukkit.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionContents;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.LingeringPotion;
import org.bukkit.entity.SplashPotion;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public final class CraftThrownPotion extends CraftThrowableProjectile implements ThrownPotion, SplashPotion, LingeringPotion {
    public CraftThrownPotion(CraftServer server, net.minecraft.world.entity.projectile.ThrownPotion entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.projectile.ThrownPotion getHandle() {
        return (net.minecraft.world.entity.projectile.ThrownPotion) this.entity;
    }

    @Override
    public @NotNull Collection<PotionEffect> getEffects() {
        PotionContents contents = getHandle().getItem().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        List<PotionEffect> effects = new ArrayList<>();
        for (MobEffectInstance effect : contents.getAllEffects()) {
            PotionEffect converted = toBukkit(effect);
            if (converted != null) effects.add(converted);
        }
        return List.copyOf(effects);
    }

    @Override public @NotNull ItemStack getItem() { return CraftItemStack.asBukkitCopy(getHandle().getItem()); }

    @Override
    public void setItem(@NotNull ItemStack item) {
        Objects.requireNonNull(item, "item");
        getHandle().setItem(CraftItemStack.asNMSCopy(item));
    }

    @Override
    public @NotNull PotionMeta getPotionMeta() {
        ItemStack item = getItem();
        if (!(item.getItemMeta() instanceof PotionMeta meta)) {
            throw new IllegalStateException("Potion projectile item did not expose PotionMeta: " + item.getType());
        }
        return meta;
    }

    @Override
    public void setPotionMeta(@NotNull PotionMeta meta) {
        Objects.requireNonNull(meta, "meta");
        ItemStack item = getItem();
        if (!item.setItemMeta(meta)) throw new IllegalArgumentException("PotionMeta is not applicable to " + item.getType());
        getHandle().setItem(CraftItemStack.asNMSCopy(item));
    }

    @Override
    public void splash() {
        net.minecraft.world.phys.BlockHitResult miss = net.minecraft.world.phys.BlockHitResult.miss(
                getHandle().position(), net.minecraft.core.Direction.UP, getHandle().blockPosition());
        ((io.lunararcdevs.lunararc.common.bridge.access.ThrownPotionInvokeBridge) (Object) getHandle())
                .lunararc$invokeOnHit(miss);
    }

    private static PotionEffect toBukkit(MobEffectInstance effect) {
        ResourceLocation id = effect.getEffect().unwrapKey().map(ResourceKey::location)
                .orElseGet(() -> net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()));
        if (id == null) return null;
        PotionEffectType type = PotionEffectType.getByKey(new NamespacedKey(id.getNamespace(), id.getPath()));
        if (type == null) return null;
        return new PotionEffect(type, effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon());
    }
}
