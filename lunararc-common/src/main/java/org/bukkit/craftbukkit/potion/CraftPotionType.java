package org.bukkit.craftbukkit.potion;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.List;
import java.util.Objects;

public final class CraftPotionType implements PotionType.InternalPotionData {
    private final NamespacedKey key;
    private final Potion handle;

    public CraftPotionType(NamespacedKey key) {
        this.key = Objects.requireNonNull(key, "key");
        var server = io.lunararcdevs.lunararc.common.mod.server.LunarArcServer.minecraftServer();
        if (server == null) throw new IllegalStateException("MinecraftServer has not been attached to LunarArc yet");
        var registry = server.registryAccess().registryOrThrow(Registries.POTION);
        ResourceLocation id = ResourceLocation.parse(key.toString());
        this.handle = registry.getOptional(id)
                .orElseThrow(() -> new IllegalArgumentException("Unknown potion type " + key));
    }

    public Potion getHandle() {
        return this.handle;
    }

    @Override
    public PotionEffectType getEffectType() {
        List<PotionEffect> effects = getPotionEffects();
        return effects.isEmpty() ? null : effects.getFirst().getType();
    }

    @Override
    public List<PotionEffect> getPotionEffects() {
        return this.handle.getEffects().stream().map(CraftPotionType::toBukkit).toList();
    }

    @Override
    public boolean isInstant() {
        return this.handle.hasInstantEffects();
    }

    @Override
    public boolean isUpgradeable() {
        return hasSibling("strong_");
    }

    @Override
    public boolean isExtendable() {
        return hasSibling("long_");
    }

    @Override
    public int getMaxLevel() {
        return isUpgradeable() ? 2 : 1;
    }

    private boolean hasSibling(String prefix) {
        var server = io.lunararcdevs.lunararc.common.mod.server.LunarArcServer.minecraftServer();
        if (server == null) return false;
        var registry = server.registryAccess().registryOrThrow(Registries.POTION);
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(this.key.getNamespace(), prefix + this.key.getKey());
        return registry.containsKey(id);
    }

    private static PotionEffect toBukkit(MobEffectInstance effect) {
        ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value());
        if (id == null) throw new IllegalStateException("Unregistered potion effect " + effect.getEffect());
        PotionEffectType type = PotionEffectType.getByKey(new NamespacedKey(id.getNamespace(), id.getPath()));
        if (type == null) throw new IllegalStateException("No Bukkit PotionEffectType for " + id);
        return new PotionEffect(type, effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.isVisible(), effect.showIcon());
    }

    public static PotionType minecraftToBukkit(Potion minecraft) {
        return org.bukkit.craftbukkit.CraftRegistry.minecraftToBukkit(minecraft, net.minecraft.core.registries.Registries.POTION, org.bukkit.Registry.POTION);
    }

    public static PotionType minecraftHolderToBukkit(net.minecraft.core.Holder<Potion> minecraft) {
        return CraftPotionType.minecraftToBukkit(minecraft.value());
    }

    public static Potion bukkitToMinecraft(PotionType bukkit) {
        com.google.common.base.Preconditions.checkArgument(bukkit != null);

        return org.bukkit.craftbukkit.CraftRegistry.getMinecraftRegistry(net.minecraft.core.registries.Registries.POTION)
                .getOptional(org.bukkit.craftbukkit.util.CraftNamespacedKey.toMinecraft(bukkit.getKey()))
                .orElseThrow();
    }

    public static net.minecraft.core.Holder<Potion> bukkitToMinecraftHolder(PotionType bukkit) {
        com.google.common.base.Preconditions.checkArgument(bukkit != null);

        net.minecraft.core.Registry<Potion> registry = org.bukkit.craftbukkit.CraftRegistry.getMinecraftRegistry(net.minecraft.core.registries.Registries.POTION);

        if (registry.wrapAsHolder(CraftPotionType.bukkitToMinecraft(bukkit)) instanceof net.minecraft.core.Holder.Reference<Potion> holder) {
            return holder;
        }

        throw new IllegalArgumentException("No Reference holder found for " + bukkit
                + ", this can happen if a plugin creates its own potion type without properly registering it.");
    }

    public static String bukkitToString(PotionType bukkit) {
        com.google.common.base.Preconditions.checkArgument(bukkit != null);

        return bukkit.getKey().toString();
    }

    public static PotionType stringToBukkit(String string) {
        com.google.common.base.Preconditions.checkArgument(string != null);

        // Names were serialized before keys were, so replay the rename first, then the key rename.
        string = org.bukkit.craftbukkit.legacy.FieldRename.convertPotionTypeName(org.bukkit.craftbukkit.util.ApiVersion.CURRENT, string);
        string = string.toLowerCase(java.util.Locale.ROOT);
        org.bukkit.NamespacedKey key = org.bukkit.NamespacedKey.fromString(string);
        if (key == null) return null;

        return org.bukkit.craftbukkit.CraftRegistry.get(org.bukkit.Registry.POTION, key, org.bukkit.craftbukkit.util.ApiVersion.CURRENT);
    }
}
