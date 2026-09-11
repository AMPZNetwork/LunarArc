package io.lunararcdevs.lunararc.common.server.registry;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/** Concrete Paper/Bukkit tag backed by the running server's real named holder set. */
public final class LunarArcNamedRegistryTag<T extends Keyed, M> implements Tag<T>, org.bukkit.Tag<T> {
    private final TagKey<T> tagKey;
    private final HolderSet.Named<M> namedSet;

    public LunarArcNamedRegistryTag(TagKey<T> tagKey, HolderSet.Named<M> namedSet) {
        this.tagKey = tagKey;
        this.namedSet = namedSet;
    }

    @Override
    public TagKey<T> tagKey() {
        return this.tagKey;
    }

    @Override
    public RegistryKey<T> registryKey() {
        return this.tagKey.registryKey();
    }

    @Override
    public @Unmodifiable Collection<TypedKey<T>> values() {
        ArrayList<TypedKey<T>> values = new ArrayList<>();
        for (Holder<M> holder : this.namedSet) {
            if (!(holder instanceof Holder.Reference<M> reference)) continue;
            net.minecraft.resources.ResourceLocation location = reference.key().location();
            values.add(TypedKey.create(this.registryKey(), Key.key(location.getNamespace(), location.getPath())));
        }
        return java.util.List.copyOf(values);
    }

    @Override
    public boolean contains(TypedKey<T> valueKey) {
        if (!this.registryKey().equals(valueKey.registryKey())) return false;
        Key wanted = valueKey.key();
        for (Holder<M> holder : this.namedSet) {
            if (!(holder instanceof Holder.Reference<M> reference)) continue;
            net.minecraft.resources.ResourceLocation location = reference.key().location();
            if (location.getNamespace().equals(wanted.namespace()) && location.getPath().equals(wanted.value())) return true;
        }
        return false;
    }

    @Override
    public @Unmodifiable Collection<T> resolve(Registry<T> registry) {
        ArrayList<T> values = new ArrayList<>();
        for (Holder<M> holder : this.namedSet) {
            if (!(holder instanceof Holder.Reference<M> reference)) continue;
            net.minecraft.resources.ResourceLocation location = reference.key().location();
            T value = registry.get(new NamespacedKey(location.getNamespace(), location.getPath()));
            if (value != null) values.add(value);
        }
        return java.util.List.copyOf(values);
    }

    @Override
    public boolean isTagged(T item) {
        return item != null && this.getValues().contains(item);
    }

    @Override
    public Set<T> getValues() {
        Registry<T> registry = RegistryAccess.registryAccess().getRegistry(this.registryKey());
        return Set.copyOf(new LinkedHashSet<>(this.resolve(registry)));
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        Key key = this.tagKey.key();
        return new NamespacedKey(key.namespace(), key.value());
    }
}
