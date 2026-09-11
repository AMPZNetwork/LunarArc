package io.lunararcdevs.lunararc.common.server;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import java.util.stream.Stream;

final class LunarArcBukkitRegistry<T extends Keyed> implements Registry<T> {
    private final Map<NamespacedKey, T> byKey;
    private final List<T> values;

    private LunarArcBukkitRegistry(Map<NamespacedKey, T> byKey, List<T> values) {
        this.byKey = byKey;
        this.values = values;
    }

    static <T extends Keyed> Registry<T> forType(Class<T> type) {
        if (type == null) return empty();

        Map<NamespacedKey, T> byKey = new LinkedHashMap<>();
        List<T> values = new ArrayList<>();

        if (type.isEnum()) {
            T[] constants = type.getEnumConstants();
            if (constants != null) {
                for (T value : constants) add(value, byKey, values);
            }
        }

        return new LunarArcBukkitRegistry<>(
                Collections.unmodifiableMap(byKey),
                Collections.unmodifiableList(values));
    }

    static <T extends Keyed> Registry<T> fromValues(java.util.Collection<T> source) {
        Map<NamespacedKey, T> byKey = new LinkedHashMap<>();
        List<T> values = new ArrayList<>();
        for (T value : source) {
            add(value, byKey, values);
        }
        return new LunarArcBukkitRegistry<>(
                Collections.unmodifiableMap(byKey),
                Collections.unmodifiableList(values));
    }

    static <T extends Keyed> Registry<T> empty() {
        return new LunarArcBukkitRegistry<>(Collections.emptyMap(), Collections.emptyList());
    }


    static <T extends Keyed> Registry<T> lazy(Supplier<Collection<T>> valuesSupplier) {
        return new LazyRegistry<>(valuesSupplier);
    }

    private static final class LazyRegistry<T extends Keyed> implements Registry<T> {
        private final Supplier<Collection<T>> valuesSupplier;
        private volatile Registry<T> delegate;

        LazyRegistry(Supplier<Collection<T>> valuesSupplier) {
            this.valuesSupplier = valuesSupplier;
        }

        private Registry<T> delegate() {
            Registry<T> current = delegate;
            if (current != null) return current;
            synchronized (this) {
                current = delegate;
                if (current == null) {
                    current = delegate = fromValues(valuesSupplier.get());
                }
            }
            return current;
        }

        @Override public @Nullable T get(@NotNull NamespacedKey key) { return delegate().get(key); }

        @Override
        public @NotNull T getOrThrow(@NotNull NamespacedKey key) {
            return delegate().getOrThrow(key);
        }

        @Override public @Nullable NamespacedKey getKey(@NotNull T value) { return delegate().getKey(value); }

        @Override public @NotNull Iterator<T> iterator() { return delegate().iterator(); }

        @Override public @NotNull Stream<T> stream() { return delegate().stream(); }
    }

    private static <T extends Keyed> void add(T value, Map<NamespacedKey, T> byKey, List<T> values) {
        if (value == null) return;
        try {
            NamespacedKey key = value.getKey();
            if (key == null) return;
            byKey.putIfAbsent(key, value);
            values.add(value);
        } catch (Throwable ignored) {
        }
    }

    @Override public @Nullable T get(@NotNull NamespacedKey key) { return byKey.get(key); }

    @Override
    public @NotNull T getOrThrow(@NotNull NamespacedKey key) {
        T value = get(key);
        if (value == null) throw new NoSuchElementException(key.toString());
        return value;
    }

    @Override
    public @Nullable NamespacedKey getKey(@NotNull T value) {
        try {
            return value.getKey();
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Override public @NotNull Iterator<T> iterator() { return values.iterator(); }
    @Override public @NotNull Stream<T> stream() { return values.stream(); }
}
