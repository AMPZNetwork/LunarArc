package org.bukkit.craftbukkit.potion;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeCategory;
import org.jetbrains.annotations.NotNull;

public final class CraftPotionEffectType extends PotionEffectType implements org.bukkit.craftbukkit.util.Handleable<MobEffect> {
    private final NamespacedKey key;
    private final MobEffect handle;
    private final int id;

    public CraftPotionEffectType(NamespacedKey key, MobEffect handle) {
        this.key = java.util.Objects.requireNonNull(key, "key");
        this.handle = java.util.Objects.requireNonNull(handle, "handle");
        this.id = BuiltInRegistries.MOB_EFFECT.getId(handle) + 1;
    }

    public MobEffect getHandle() {
        return this.handle;
    }

    public static PotionEffectType minecraftHolderToBukkit(Holder<MobEffect> holder) {
        return minecraftToBukkit(holder.value());
    }

    public static PotionEffectType minecraftToBukkit(MobEffect effect) {
        var location = BuiltInRegistries.MOB_EFFECT.getKey(effect);
        if (location == null) return null;
        return Registry.EFFECT.get(new NamespacedKey(location.getNamespace(), location.getPath()));
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return this.key;
    }

    @Override
    public PotionEffect createEffect(int duration, int amplifier) {
        return new PotionEffect(this, this.isInstant() ? 1 : duration, amplifier);
    }

    @Override
    public boolean isInstant() {
        return this.handle.isInstantenous();
    }

    @Override
    public @NotNull PotionEffectTypeCategory getCategory() {
        return switch (this.handle.getCategory()) {
            case BENEFICIAL -> PotionEffectTypeCategory.BENEFICIAL;
            case HARMFUL -> PotionEffectTypeCategory.HARMFUL;
            case NEUTRAL -> PotionEffectTypeCategory.NEUTRAL;
        };
    }

    @Override
    public @NotNull Color getColor() {
        return Color.fromRGB(this.handle.getColor());
    }

    @Override
    public double getDurationModifier() {
        return 1.0D;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public @NotNull String getName() {
        return switch (this.id) {
            case 1 -> "SPEED";
            case 2 -> "SLOW";
            case 3 -> "FAST_DIGGING";
            case 4 -> "SLOW_DIGGING";
            case 5 -> "INCREASE_DAMAGE";
            case 6 -> "HEAL";
            case 7 -> "HARM";
            case 8 -> "JUMP";
            case 9 -> "CONFUSION";
            case 10 -> "REGENERATION";
            case 11 -> "DAMAGE_RESISTANCE";
            case 12 -> "FIRE_RESISTANCE";
            case 13 -> "WATER_BREATHING";
            case 14 -> "INVISIBILITY";
            case 15 -> "BLINDNESS";
            case 16 -> "NIGHT_VISION";
            case 17 -> "HUNGER";
            case 18 -> "WEAKNESS";
            case 19 -> "POISON";
            case 20 -> "WITHER";
            case 21 -> "HEALTH_BOOST";
            case 22 -> "ABSORPTION";
            case 23 -> "SATURATION";
            case 24 -> "GLOWING";
            case 25 -> "LEVITATION";
            case 26 -> "LUCK";
            case 27 -> "UNLUCK";
            case 28 -> "SLOW_FALLING";
            case 29 -> "CONDUIT_POWER";
            case 30 -> "DOLPHINS_GRACE";
            case 31 -> "BAD_OMEN";
            case 32 -> "HERO_OF_THE_VILLAGE";
            case 33 -> "DARKNESS";
            default -> this.key.toString();
        };
    }

    @Override
    public @NotNull String getTranslationKey() {
        return this.handle.getDescriptionId();
    }

    @Override
    public @NotNull String translationKey() {
        return this.handle.getDescriptionId();
    }

    @Override
    public @NotNull Map<Attribute, org.bukkit.attribute.AttributeModifier> getEffectAttributes() {
        Map<Attribute, org.bukkit.attribute.AttributeModifier> result = new HashMap<>();
        for (var entry : this.handle.attributeModifiers.entrySet()) {
            var attributeKey = entry.getKey().unwrapKey().orElse(null);
            if (attributeKey == null) continue;
            var location = attributeKey.location();
            Attribute bukkit = Registry.ATTRIBUTE.get(new NamespacedKey(location.getNamespace(), location.getPath()));
            if (bukkit == null) continue;
            result.put(bukkit, toBukkit(entry.getValue().create(0)));
        }
        return Map.copyOf(result);
    }

    @Override
    public double getAttributeModifierAmount(@NotNull Attribute attribute, int effectAmplifier) {
        Preconditions.checkArgument(effectAmplifier >= 0, "effectAmplifier must be greater than or equal to 0");
        for (var entry : this.handle.attributeModifiers.entrySet()) {
            var attributeKey = entry.getKey().unwrapKey().orElse(null);
            if (attributeKey == null) continue;
            NamespacedKey key = new NamespacedKey(attributeKey.location().getNamespace(), attributeKey.location().getPath());
            if (key.equals(attribute.getKey())) return entry.getValue().create(effectAmplifier).amount();
        }
        throw new IllegalArgumentException(attribute + " is not present on " + this.getKey());
    }

    @Override
    public PotionEffectType.@NotNull Category getEffectCategory() {
        return switch (this.handle.getCategory()) {
            case BENEFICIAL -> PotionEffectType.Category.BENEFICIAL;
            case HARMFUL -> PotionEffectType.Category.HARMFUL;
            case NEUTRAL -> PotionEffectType.Category.NEUTRAL;
        };
    }

    private static org.bukkit.attribute.AttributeModifier toBukkit(AttributeModifier modifier) {
        NamespacedKey key = new NamespacedKey(modifier.id().getNamespace(), modifier.id().getPath());
        org.bukkit.attribute.AttributeModifier.Operation operation = switch (modifier.operation()) {
            case ADD_VALUE -> org.bukkit.attribute.AttributeModifier.Operation.ADD_NUMBER;
            case ADD_MULTIPLIED_BASE -> org.bukkit.attribute.AttributeModifier.Operation.ADD_SCALAR;
            case ADD_MULTIPLIED_TOTAL -> org.bukkit.attribute.AttributeModifier.Operation.MULTIPLY_SCALAR_1;
        };
        return new org.bukkit.attribute.AttributeModifier(key, modifier.amount(), operation, EquipmentSlotGroup.ANY);
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof PotionEffectType type && this.key.equals(type.getKey());
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }

    @Override
    public String toString() {
        return "CraftPotionEffectType[" + this.key + "]";
    }

    public static MobEffect bukkitToMinecraft(PotionEffectType bukkit) {
        return org.bukkit.craftbukkit.CraftRegistry.bukkitToMinecraft(bukkit);
    }

    public static net.minecraft.core.Holder<MobEffect> bukkitToMinecraftHolder(PotionEffectType bukkit) {
        return org.bukkit.craftbukkit.CraftRegistry.bukkitToMinecraftHolder(bukkit, net.minecraft.core.registries.Registries.MOB_EFFECT);
    }

    public static PotionEffectType.Category fromNMS(MobEffectCategory category) {
        return switch (category) {
            case BENEFICIAL -> PotionEffectType.Category.BENEFICIAL;
            case HARMFUL -> PotionEffectType.Category.HARMFUL;
            case NEUTRAL -> PotionEffectType.Category.NEUTRAL;
        };
    }
}
