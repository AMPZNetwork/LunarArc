package org.bukkit.craftbukkit.entity;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionContents;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Arrow;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Nullable;

public class CraftArrow extends CraftAbstractArrow implements Arrow {
    private static final int NO_EFFECT_COLOR = -1;

    public CraftArrow(CraftServer server, net.minecraft.world.entity.projectile.Arrow entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.projectile.Arrow getHandle() {
        return (net.minecraft.world.entity.projectile.Arrow) this.entity;
    }

    @Override
    public boolean addCustomEffect(PotionEffect effect, boolean overwrite) {
        Objects.requireNonNull(effect, "effect");
        if (this.hasCustomEffect(effect.getType())) {
            if (!overwrite) return false;
            this.removeCustomEffect(effect.getType());
        }
        this.getHandle().addEffect(toMinecraft(effect));
        return true;
    }

    @Override
    public void clearCustomEffects() {
        PotionContents old = this.getHandle().getPotionContents();
        this.getHandle().setPotionContents(new PotionContents(old.potion(), old.customColor(), List.of()));
    }

    @Override
    public List<PotionEffect> getCustomEffects() {
        return this.getHandle().getPotionContents().customEffects().stream().map(CraftArrow::toBukkit).toList();
    }

    @Override
    public boolean hasCustomEffect(@Nullable PotionEffectType type) {
        if (type == null) return false;
        ResourceLocation wanted = ResourceLocation.parse(type.getKey().toString());
        return this.getHandle().getPotionContents().customEffects().stream().anyMatch(effect -> {
            ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value());
            return wanted.equals(id);
        });
    }

    @Override
    public boolean hasCustomEffects() {
        return !this.getHandle().getPotionContents().customEffects().isEmpty();
    }

    @Override
    public boolean removeCustomEffect(PotionEffectType type) {
        Objects.requireNonNull(type, "type");
        ResourceLocation wanted = ResourceLocation.parse(type.getKey().toString());
        PotionContents old = this.getHandle().getPotionContents();
        List<MobEffectInstance> kept = old.customEffects().stream().filter(effect -> {
            ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value());
            return !wanted.equals(id);
        }).toList();
        if (kept.size() == old.customEffects().size()) return false;
        this.getHandle().setPotionContents(new PotionContents(old.potion(), old.customColor(), kept));
        return true;
    }

    @Override
    @Deprecated
    public void setBasePotionData(@Nullable PotionData data) {
        this.setBasePotionType(data == null ? null : data.getType());
    }

    @Override
    @Deprecated
    public @Nullable PotionData getBasePotionData() {
        PotionType type = this.getBasePotionType();
        return type == null ? null : new PotionData(type);
    }

    @Override
    public void setBasePotionType(@Nullable PotionType type) {
        PotionContents old = this.getHandle().getPotionContents();
        if (type == null) {
            this.getHandle().setPotionContents(new PotionContents(Optional.empty(), old.customColor(), old.customEffects()));
            return;
        }
        var registry = this.getHandle().registryAccess().registryOrThrow(Registries.POTION);
        Holder<net.minecraft.world.item.alchemy.Potion> holder = registry.getHolder(ResourceLocation.parse(type.getKey().toString()))
                .orElseThrow(() -> new IllegalArgumentException("Unknown potion type " + type.getKey()));
        this.getHandle().setPotionContents(new PotionContents(Optional.of(holder), old.customColor(), old.customEffects()));
    }

    @Override
    public @Nullable PotionType getBasePotionType() {
        return this.getHandle().getPotionContents().potion().map(holder -> {
            ResourceLocation id = this.getHandle().registryAccess().registryOrThrow(Registries.POTION).getKey(holder.value());
            if (id == null) throw new IllegalStateException("Arrow has an unregistered potion");
            return Registry.POTION.get(new NamespacedKey(id.getNamespace(), id.getPath()));
        }).orElse(null);
    }

    @Override
    public void setColor(@Nullable Color color) {
        PotionContents old = this.getHandle().getPotionContents();
        Optional<Integer> customColor = color == null ? Optional.empty() : Optional.of(color.asARGB());
        this.getHandle().setPotionContents(new PotionContents(old.potion(), customColor, old.customEffects()));
    }

    @Override
    public @Nullable Color getColor() {
        int color = this.getHandle().getColor();
        return color == NO_EFFECT_COLOR ? null : Color.fromARGB(color);
    }

    private static MobEffectInstance toMinecraft(PotionEffect effect) {
        ResourceLocation id = ResourceLocation.parse(effect.getType().getKey().toString());
        Holder<MobEffect> holder = BuiltInRegistries.MOB_EFFECT.getHolder(id)
                .orElseThrow(() -> new IllegalArgumentException("Unknown potion effect " + effect.getType().getKey()));
        return new MobEffectInstance(holder, effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.hasParticles(), effect.hasIcon());
    }

    private static PotionEffect toBukkit(MobEffectInstance effect) {
        ResourceLocation id = BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value());
        if (id == null) throw new IllegalStateException("Unregistered mob effect " + effect.getEffect());
        PotionEffectType type = PotionEffectType.getByKey(new NamespacedKey(id.getNamespace(), id.getPath()));
        if (type == null) throw new IllegalStateException("No Bukkit potion effect for " + id);
        return new PotionEffect(type, effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon());
    }

    @Override
    public String toString() {
        return "CraftArrow";
    }
}
