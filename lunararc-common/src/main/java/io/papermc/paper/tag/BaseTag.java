package io.papermc.paper.tag;

import com.google.common.collect.Lists;
import java.util.Collections;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class BaseTag<T extends Keyed, C extends BaseTag<T, C>> implements Tag<T> {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("LunarArc");
    private static final java.util.concurrent.atomic.AtomicBoolean EXTRA_VALUES_EXPLAINED =
            new java.util.concurrent.atomic.AtomicBoolean();

    protected final NamespacedKey key;
    protected final Set<T> tagged;
    private final List<Predicate<T>> globalPredicates;
    private boolean locked = false;

    public BaseTag(@NotNull Class<T> clazz, @NotNull NamespacedKey key, @NotNull Predicate<T> filter) {
        this(clazz, key);
        add(filter);
    }

    public BaseTag(@NotNull Class<T> clazz, @NotNull NamespacedKey key, @NotNull T...values) {
        this(clazz, key, Lists.newArrayList(values));
    }

    public BaseTag(@NotNull Class<T> clazz, @NotNull NamespacedKey key, @NotNull Collection<T> values) {
        this(clazz, key, values, o -> true);
    }

    public BaseTag(@NotNull Class<T> clazz, @NotNull NamespacedKey key, @NotNull Collection<T> values, @NotNull Predicate<T>... globalPredicates) {
        this.key = key;
        this.tagged = clazz.isEnum() ? createEnumSet(clazz) : new HashSet<>();
        this.tagged.addAll(values);
        this.globalPredicates = Lists.newArrayList(globalPredicates);
    }

    private <E> Set<E> createEnumSet(Class<E> enumClass) {
        assert enumClass.isEnum();
        return (Set<E>) EnumSet.noneOf((Class<Enum>) enumClass);
    }

    public @NotNull C lock() {
        this.locked = true;
        return (C) this;
    }

    public boolean isLocked() {
        return this.locked;
    }

    private void checkLock() {
        if (this.locked) {
            throw new UnsupportedOperationException("Tag (" + this.key + ") is locked");
        }
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @NotNull
    @Override
    public Set<T> getValues() {
        return Collections.unmodifiableSet(tagged);
    }

    @Override
    public boolean isTagged(@NotNull T item) {
        return tagged.contains(item);
    }

    @NotNull
    public C add(@NotNull Tag<T>...tags) {
        for (Tag<T> tag : tags) {
            add(tag.getValues());
        }
        return (C) this;
    }

    @NotNull
    public C add(@NotNull T...values) {
        this.checkLock();
        this.tagged.addAll(Lists.newArrayList(values));
        return (C) this;
    }

    @NotNull
    public C add(@NotNull Collection<T> collection) {
        this.checkLock();
        this.tagged.addAll(collection);
        return (C) this;
    }

    @NotNull
    public C add(@NotNull Predicate<T> filter) {
        return add(getAllPossibleValues().stream().filter(globalPredicates.stream().reduce(Predicate::or).orElse(t -> true)).filter(filter).collect(Collectors.toSet()));
    }

    @NotNull
    public C contains(@NotNull String with) {
        return add(value -> getName(value).contains(with));
    }

    @NotNull
    public C endsWith(@NotNull String with) {
        return add(value -> getName(value).endsWith(with));
    }

    @NotNull
    public C startsWith(@NotNull String with) {
        return add(value -> getName(value).startsWith(with));
    }

    @NotNull
    public C not(@NotNull Tag<T>...tags) {
        for (Tag<T> tag : tags) {
            not(tag.getValues());
        }
        return (C) this;
    }

    @NotNull
    public C not(@NotNull T...values) {
        this.checkLock();
        this.tagged.removeAll(Lists.newArrayList(values));
        return (C) this;
    }

    @NotNull
    public C not(@NotNull Collection<T> values) {
        this.checkLock();
        this.tagged.removeAll(values);
        return (C) this;
    }

    @NotNull
    public C not(@NotNull Predicate<T> filter) {
        not(getAllPossibleValues().stream().filter(globalPredicates.stream().reduce(Predicate::or).orElse(t -> true)).filter(filter).collect(Collectors.toSet()));
        return (C) this;
    }

    @NotNull
    public C notContains(@NotNull String with) {
        return not(value -> getName(value).contains(with));
    }

    @NotNull
    public C notEndsWith(@NotNull String with) {
        return not(value -> getName(value).endsWith(with));
    }

    @NotNull
    public C notStartsWith(@NotNull String with) {
        return not(value -> getName(value).startsWith(with));
    }

    @NotNull
    public C ensureSize(@NotNull String label, int size) {
        long actual = this.tagged.stream().filter(globalPredicates.stream().reduce(Predicate::or).orElse(t -> true)).count();
        if (size != actual) {
            reportSizeMismatch(label, size, actual);
        }
        return (C) this;
    }

    private void reportSizeMismatch(String label, int expected, long actual) {
        String detail = key.toString() + ": " + label + " - Expected " + expected + " values, got " + actual;
        if (actual < expected) {
            // Fewer than vanilla has: a tag this server should have filled and did not.
            LOGGER.warn("{} - fewer values than vanilla defines, which is not something mods add. "
                    + "The tag is being used as built.", detail);
            return;
        }
        if (EXTRA_VALUES_EXPLAINED.compareAndSet(false, true)) {
            LOGGER.info("{} - extra values come from modded items whose names match the tag's "
                    + "filter, which is normal on a hybrid server. The tag is being used as built; "
                    + "further size differences are logged at debug level.", detail);
            return;
        }
        LOGGER.debug("{} - extra values from modded items.", detail);
    }

    @NotNull
    @ApiStatus.Internal
    protected abstract Set<T> getAllPossibleValues();

    @NotNull
    @ApiStatus.Internal
    protected abstract String getName(@NotNull T value);
}
