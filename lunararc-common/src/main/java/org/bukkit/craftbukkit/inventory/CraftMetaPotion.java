package org.bukkit.craftbukkit.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** 1.21.1 PotionMeta backed directly by the POTION_CONTENTS data component. */
public final class CraftMetaPotion extends CraftItemMeta implements PotionMeta {
    private PotionType baseType;
    private List<PotionEffect> customEffects = new ArrayList<>();
    private Integer color;

    public CraftMetaPotion() {
        super();
    }

    public CraftMetaPotion(ItemStack nms) {
        super(nms);
        if (nms == null || nms.isEmpty()) return;
        PotionContents contents = nms.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
        if (contents == null) return;
        this.baseType = contents.potion().map(CraftMetaPotion::toBukkitPotion).orElse(null);
        this.color = contents.customColor().orElse(null);
        for (MobEffectInstance effect : contents.customEffects()) {
            PotionEffect converted = toBukkitEffect(effect);
            if (converted != null) this.customEffects.add(converted);
        }
    }

    @Override
    public void applyToNms(ItemStack nms) {
        super.applyToNms(nms);
        Optional<Holder<Potion>> potion = this.baseType == null ? Optional.empty() : Optional.of(toMinecraftPotion(this.baseType));
        Optional<Integer> customColor = this.color == null ? Optional.empty() : Optional.of(this.color);
        List<MobEffectInstance> effects = this.customEffects.stream().map(CraftMetaPotion::toMinecraftEffect).toList();
        if (potion.isEmpty() && customColor.isEmpty() && effects.isEmpty()) {
            nms.remove(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
        } else {
            nms.set(net.minecraft.core.component.DataComponents.POTION_CONTENTS, new PotionContents(potion, customColor, effects));
        }
    }

    @Override
    @Deprecated
    public void setBasePotionData(@Nullable PotionData data) {
        this.baseType = data == null ? null : data.getType();
    }

    @Override
    @Deprecated
    public @Nullable PotionData getBasePotionData() {
        return this.baseType == null ? null : new PotionData(this.baseType);
    }

    @Override public void setBasePotionType(@Nullable PotionType type) { this.baseType = type; }
    @Override public @Nullable PotionType getBasePotionType() { return this.baseType; }
    @Override public boolean hasBasePotionType() { return this.baseType != null; }
    @Override public boolean hasCustomEffects() { return !this.customEffects.isEmpty(); }
    @Override public @NotNull List<PotionEffect> getCustomEffects() { return List.copyOf(this.customEffects); }

    @Override
    public boolean addCustomEffect(@NotNull PotionEffect effect, boolean overwrite) {
        Objects.requireNonNull(effect, "effect");
        int index = indexOf(effect.getType());
        if (index >= 0) {
            if (!overwrite) return false;
            if (this.customEffects.get(index).equals(effect)) return false;
            this.customEffects.set(index, effect);
            return true;
        }
        this.customEffects.add(effect);
        return true;
    }

    @Override
    public boolean removeCustomEffect(@NotNull PotionEffectType type) {
        Objects.requireNonNull(type, "type");
        return this.customEffects.removeIf(effect -> effect.getType().equals(type));
    }

    @Override public boolean hasCustomEffect(@NotNull PotionEffectType type) { return indexOf(Objects.requireNonNull(type, "type")) >= 0; }

    @Override
    @Deprecated
    public boolean setMainEffect(@NotNull PotionEffectType type) {
        int index = indexOf(Objects.requireNonNull(type, "type"));
        if (index <= 0) return false;
        PotionEffect first = this.customEffects.get(0);
        this.customEffects.set(0, this.customEffects.get(index));
        this.customEffects.set(index, first);
        return true;
    }

    @Override
    public boolean clearCustomEffects() {
        boolean changed = !this.customEffects.isEmpty();
        this.customEffects.clear();
        return changed;
    }

    @Override public boolean hasColor() { return this.color != null; }
    @Override public @Nullable Color getColor() { return this.color == null ? null : Color.fromRGB(this.color & 0xFFFFFF); }
    @Override public void setColor(@Nullable Color color) { this.color = color == null ? null : color.asRGB(); }

    @Override
    public CraftMetaPotion clone() {
        CraftMetaPotion clone = (CraftMetaPotion) super.clone();
        clone.customEffects = new ArrayList<>(this.customEffects);
        return clone;
    }

    private int indexOf(PotionEffectType type) {
        for (int i = 0; i < this.customEffects.size(); ++i) {
            if (this.customEffects.get(i).getType().equals(type)) return i;
        }
        return -1;
    }

    private static PotionType toBukkitPotion(Holder<Potion> holder) {
        ResourceLocation id = holder.unwrapKey().map(ResourceKey::location)
                .orElseGet(() -> server().registryAccess().registryOrThrow(Registries.POTION).getKey(holder.value()));
        if (id == null) throw new IllegalStateException("Unregistered potion " + holder.value());
        PotionType type = Registry.POTION.get(new NamespacedKey(id.getNamespace(), id.getPath()));
        if (type == null) throw new IllegalStateException("No Bukkit PotionType for " + id);
        return type;
    }

    private static Holder<Potion> toMinecraftPotion(PotionType type) {
        NamespacedKey key = type.getKey();
        ResourceKey<Potion> resourceKey = ResourceKey.create(Registries.POTION,
                ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getKey()));
        return server().registryAccess().registryOrThrow(Registries.POTION).getHolder(resourceKey)
                .orElseThrow(() -> new IllegalArgumentException("Unknown potion " + key));
    }

    private static @Nullable PotionEffect toBukkitEffect(MobEffectInstance effect) {
        ResourceLocation id = effect.getEffect().unwrapKey().map(ResourceKey::location)
                .orElseGet(() -> net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()));
        if (id == null) return null;
        PotionEffectType type = PotionEffectType.getByKey(new NamespacedKey(id.getNamespace(), id.getPath()));
        if (type == null) return null;
        return new PotionEffect(type, effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon());
    }

    private static MobEffectInstance toMinecraftEffect(PotionEffect effect) {
        NamespacedKey key = effect.getType().getKey();
        ResourceKey<MobEffect> resourceKey = ResourceKey.create(Registries.MOB_EFFECT,
                ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getKey()));
        Holder<MobEffect> holder = server().registryAccess().registryOrThrow(Registries.MOB_EFFECT).getHolder(resourceKey)
                .orElseThrow(() -> new IllegalArgumentException("Unknown potion effect " + key));
        return new MobEffectInstance(holder, effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.hasParticles(), effect.hasIcon());
    }

    private static net.minecraft.server.MinecraftServer server() {
        net.minecraft.server.MinecraftServer server = io.lunararcdevs.lunararc.common.mod.server.LunarArcServer.minecraftServer();
        if (server == null) throw new IllegalStateException("MinecraftServer is not attached");
        return server;
    }
}
