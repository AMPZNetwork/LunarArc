package org.bukkit.craftbukkit.damage;

import net.minecraft.world.damagesource.DamageEffects;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.damage.DamageEffect;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class CraftDamageEffect implements DamageEffect {
    private final DamageEffects handle;

    public CraftDamageEffect(@NotNull DamageEffects handle) { this.handle = Objects.requireNonNull(handle, "handle"); }
    public @NotNull DamageEffects getHandle() { return this.handle; }

    @Override
    public @NotNull Sound getSound() {
        net.minecraft.sounds.SoundEvent event = this.handle.sound();
        net.minecraft.resources.ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.getKey(event);
        if (id == null) throw new IllegalStateException("Unregistered sound for damage effect " + this.handle);
        Sound sound = org.bukkit.Registry.SOUNDS.get(new NamespacedKey(id.getNamespace(), id.getPath()));
        if (sound == null) throw new IllegalStateException("No Bukkit Sound for NMS sound " + id);
        return sound;
    }

    public static DamageEffect getById(String id) {
        for (DamageEffects effects : DamageEffects.values()) {
            if (effects.getSerializedName().equalsIgnoreCase(id)) {
                return toBukkit(effects);
            }
        }
        return null;
    }

    public static DamageEffect toBukkit(DamageEffects damageEffects) {
        return new CraftDamageEffect(damageEffects);
    }
}
