package org.bukkit.craftbukkit.damage;

import org.bukkit.NamespacedKey;
import org.bukkit.damage.DamageEffect;
import org.bukkit.damage.DamageScaling;
import org.bukkit.damage.DamageType;
import org.bukkit.damage.DeathMessageType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class CraftDamageType implements DamageType, org.bukkit.craftbukkit.util.Handleable<net.minecraft.world.damagesource.DamageType> {
    private final NamespacedKey key;
    private final net.minecraft.world.damagesource.DamageType handle;

    public CraftDamageType(@NotNull NamespacedKey key, @NotNull net.minecraft.world.damagesource.DamageType handle) {
        this.key = Objects.requireNonNull(key, "key");
        this.handle = Objects.requireNonNull(handle, "handle");
    }

    public @NotNull net.minecraft.world.damagesource.DamageType getHandle() { return this.handle; }
    @Override public @NotNull NamespacedKey getKey() { return this.key; }
    @Override public @NotNull String getTranslationKey() { return this.handle.msgId(); }
    public @NotNull String translationKey() { return this.getTranslationKey(); }
    @Override public @NotNull DamageScaling getDamageScaling() {
        return switch (this.handle.scaling()) {
            case ALWAYS -> DamageScaling.ALWAYS;
            case WHEN_CAUSED_BY_LIVING_NON_PLAYER -> DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER;
            case NEVER -> DamageScaling.NEVER;
        };
    }
    @Override public @NotNull DamageEffect getDamageEffect() { return new CraftDamageEffect(this.handle.effects()); }
    @Override public @NotNull DeathMessageType getDeathMessageType() {
        return switch (this.handle.deathMessageType()) {
            case DEFAULT -> DeathMessageType.DEFAULT;
            case FALL_VARIANTS -> DeathMessageType.FALL_VARIANTS;
            case INTENTIONAL_GAME_DESIGN -> DeathMessageType.INTENTIONAL_GAME_DESIGN;
        };
    }
    @Override public float getExhaustion() { return this.handle.exhaustion(); }
    @Override public String toString() { return "CraftDamageType[" + this.key + "]"; }

    // CraftBukkit's conversion pair for this registry type. Plugins call these directly to cross
    // between the Bukkit handle and the NMS object, so they carry CraftBukkit's names verbatim.
    public static DamageType minecraftToBukkit(net.minecraft.world.damagesource.DamageType minecraft) {
        return org.bukkit.craftbukkit.CraftRegistry.minecraftToBukkit(minecraft, net.minecraft.core.registries.Registries.DAMAGE_TYPE, org.bukkit.Registry.DAMAGE_TYPE);
    }

    public static DamageType minecraftHolderToBukkit(net.minecraft.core.Holder<net.minecraft.world.damagesource.DamageType> minecraft) {
        return CraftDamageType.minecraftToBukkit(minecraft.value());
    }

    public static net.minecraft.world.damagesource.DamageType bukkitToMinecraft(DamageType bukkit) {
        return org.bukkit.craftbukkit.CraftRegistry.bukkitToMinecraft(bukkit);
    }

    public static net.minecraft.core.Holder<net.minecraft.world.damagesource.DamageType> bukkitToMinecraftHolder(DamageType bukkit) {
        com.google.common.base.Preconditions.checkArgument(bukkit != null);

        net.minecraft.core.Registry<net.minecraft.world.damagesource.DamageType> registry = org.bukkit.craftbukkit.CraftRegistry.getMinecraftRegistry(net.minecraft.core.registries.Registries.DAMAGE_TYPE);

        if (registry.wrapAsHolder(CraftDamageType.bukkitToMinecraft(bukkit)) instanceof net.minecraft.core.Holder.Reference<net.minecraft.world.damagesource.DamageType> holder) {
            return holder;
        }

        throw new IllegalArgumentException("No Reference holder found for " + bukkit
                + ", this can happen if a plugin creates its own damage type without properly registering it.");
    }

    public static DeathMessageType deathMessageTypeToBukkit(
            net.minecraft.world.damagesource.DeathMessageType deathMessageType) {
        return switch (deathMessageType) {
            case DEFAULT -> DeathMessageType.DEFAULT;
            case FALL_VARIANTS -> DeathMessageType.FALL_VARIANTS;
            case INTENTIONAL_GAME_DESIGN -> DeathMessageType.INTENTIONAL_GAME_DESIGN;
        };
    }

    public static net.minecraft.world.damagesource.DeathMessageType deathMessageTypeToNMS(
            DeathMessageType deathMessageType) {
        return switch (deathMessageType) {
            case DEFAULT -> net.minecraft.world.damagesource.DeathMessageType.DEFAULT;
            case FALL_VARIANTS -> net.minecraft.world.damagesource.DeathMessageType.FALL_VARIANTS;
            case INTENTIONAL_GAME_DESIGN -> net.minecraft.world.damagesource.DeathMessageType.INTENTIONAL_GAME_DESIGN;
        };
    }

    public static DamageScaling damageScalingToBukkit(net.minecraft.world.damagesource.DamageScaling damageScaling) {
        return switch (damageScaling) {
            case ALWAYS -> DamageScaling.ALWAYS;
            case WHEN_CAUSED_BY_LIVING_NON_PLAYER -> DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER;
            case NEVER -> DamageScaling.NEVER;
        };
    }

    public static net.minecraft.world.damagesource.DamageScaling damageScalingToNMS(DamageScaling damageScaling) {
        return switch (damageScaling) {
            case ALWAYS -> net.minecraft.world.damagesource.DamageScaling.ALWAYS;
            case WHEN_CAUSED_BY_LIVING_NON_PLAYER -> net.minecraft.world.damagesource.DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER;
            case NEVER -> net.minecraft.world.damagesource.DamageScaling.NEVER;
        };
    }
}
