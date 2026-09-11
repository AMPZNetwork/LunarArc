package org.bukkit.craftbukkit;

import com.google.common.base.Preconditions;
import io.lunararcdevs.lunararc.common.LunarArcServerAccess;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.legacy.FieldRename;
import org.bukkit.craftbukkit.util.ApiVersion;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.craftbukkit.util.Handleable;
import org.bukkit.entity.EntityType;

import java.util.Optional;

public final class CraftRegistry {
    private CraftRegistry() {}

    public static RegistryAccess getMinecraftRegistry() {
        return LunarArcServerAccess.getMinecraftServer().registryAccess();
    }

    public static <E> net.minecraft.core.Registry<E> getMinecraftRegistry(
            ResourceKey<net.minecraft.core.Registry<E>> key) {
        return getMinecraftRegistry().registryOrThrow(key);
    }

    /**
     * Usage note, carried over from CraftBukkit: only use this to delegate the conversion methods
     * of the individual Craft classes. Elsewhere, call the per-type method instead.
     */
    public static <B extends Keyed, M> B minecraftToBukkit(M minecraft,
            ResourceKey<net.minecraft.core.Registry<M>> registryKey, Registry<B> bukkitRegistry) {
        Preconditions.checkArgument(minecraft != null);

        net.minecraft.core.Registry<M> registry = getMinecraftRegistry(registryKey);
        B bukkit = bukkitRegistry.get(CraftNamespacedKey.fromMinecraft(registry.getResourceKey(minecraft)
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "Cannot convert '%s' to bukkit representation, since it is not registered.",
                        minecraft))).location()));

        Preconditions.checkArgument(bukkit != null);

        return bukkit;
    }

    @SuppressWarnings("unchecked")
    public static <B extends Keyed, M> M bukkitToMinecraft(B bukkit) {
        Preconditions.checkArgument(bukkit != null);

        return ((Handleable<M>) bukkit).getHandle();
    }

    public static <B extends Keyed, M> Holder<M> bukkitToMinecraftHolder(B bukkit,
            ResourceKey<net.minecraft.core.Registry<M>> registryKey) {
        Preconditions.checkArgument(bukkit != null);

        net.minecraft.core.Registry<M> registry = getMinecraftRegistry(registryKey);

        if (registry.wrapAsHolder(CraftRegistry.<B, M>bukkitToMinecraft(bukkit)) instanceof Holder.Reference<M> holder) {
            return holder;
        }

        throw new IllegalArgumentException("No Reference holder found for " + bukkit
                + ", this can happen if a plugin creates its own registry entry with out properly registering it.");
    }

    public static <T extends Keyed, M> Optional<T> unwrapAndConvertHolder(
            io.papermc.paper.registry.RegistryKey<T> registryKey, Holder<M> value) {
        return unwrapAndConvertHolder(
                io.papermc.paper.registry.RegistryAccess.registryAccess().getRegistry(registryKey), value);
    }

    public static <T extends Keyed, M> Optional<T> unwrapAndConvertHolder(Registry<T> registry, Holder<M> value) {
        return value.unwrapKey().map(key -> registry.get(CraftNamespacedKey.fromMinecraft(key.location())));
    }

    public static <B extends Keyed> B get(Registry<B> bukkit, NamespacedKey namespacedKey, ApiVersion apiVersion) {
        if (bukkit instanceof Registry.SimpleRegistry<?> simple) {
            Class<?> type = simple.getType();

            if (type == Biome.class) {
                return bukkit.get(FieldRename.BIOME_RENAME.apply(namespacedKey, apiVersion));
            }

            if (type == EntityType.class) {
                return bukkit.get(FieldRename.ENTITY_TYPE_RENAME.apply(namespacedKey, apiVersion));
            }

            if (type == Particle.class) {
                return bukkit.get(FieldRename.PARTICLE_TYPE_RENAME.apply(namespacedKey, apiVersion));
            }

            if (type == Attribute.class) {
                return bukkit.get(FieldRename.ATTRIBUTE_RENAME.apply(namespacedKey, apiVersion));
            }
        }

        return bukkit.get(namespacedKey);
    }
}
