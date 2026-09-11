package org.bukkit.craftbukkit.potion;

import com.google.common.base.Preconditions;
import io.lunararcdevs.lunararc.common.bridge.alchemy.PotionBrewingBridge;
import io.papermc.paper.potion.PotionMix;
import java.util.Collection;
import java.util.Objects;
import net.minecraft.server.MinecraftServer;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionBrewer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

public final class CraftPotionBrewer implements PotionBrewer {
    private final MinecraftServer server;

    public CraftPotionBrewer(MinecraftServer server) {
        this.server = Objects.requireNonNull(server, "server");
    }

    private PotionBrewingBridge bridge() {
        Object brewing = this.server.potionBrewing();
        if (!(brewing instanceof PotionBrewingBridge bridge)) {
            throw new IllegalStateException("PotionBrewingMixin is not active on the loader-owned PotionBrewing");
        }
        return bridge;
    }

    @Override
    @Deprecated(forRemoval = true)
    public @NotNull Collection<PotionEffect> getEffects(@NotNull PotionType type, boolean upgraded, boolean extended) {
        Objects.requireNonNull(type, "type");
        NamespacedKey key = type.getKey();
        Preconditions.checkArgument(!key.getKey().startsWith("strong_"),
                "Strong potion type cannot be used directly, got %s", key);
        Preconditions.checkArgument(!key.getKey().startsWith("long_"),
                "Extended potion type cannot be used directly, got %s", key);

        NamespacedKey effective = key;
        if (upgraded) {
            effective = new NamespacedKey(key.getNamespace(), "strong_" + key.getKey());
        } else if (extended) {
            effective = new NamespacedKey(key.getNamespace(), "long_" + key.getKey());
        }
        PotionType effectiveType = Registry.POTION.get(effective);
        Preconditions.checkNotNull(effectiveType, "Unknown potion type from data %s", effective);
        return effectiveType.getPotionEffects();
    }

    @Override
    public void addPotionMix(@NotNull PotionMix potionMix) {
        bridge().lunararc$addPotionMix(Objects.requireNonNull(potionMix, "potionMix"));
    }

    @Override
    public void removePotionMix(@NotNull NamespacedKey key) {
        bridge().lunararc$removePotionMix(Objects.requireNonNull(key, "key"));
    }

    @Override
    public void resetPotionMixes() {
        // Only remove Paper-added mixes. Vanilla/modloader recipes remain owned by
        // the active loader and must not be rebuilt or replaced here.
        bridge().lunararc$clearPotionMixes();
    }
}
