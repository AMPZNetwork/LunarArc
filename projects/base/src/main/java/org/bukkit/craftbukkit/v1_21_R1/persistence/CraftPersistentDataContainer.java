package org.bukkit.craftbukkit.v1_21_R1.persistence;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CraftPersistentDataContainer implements PersistentDataContainer {
    private final Map<NamespacedKey, Object> customData = new HashMap<>();

    public CraftPersistentDataContainer() {
    }

    @Override
    public <T, Z> void set(@NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type, @NotNull Z value) {
        customData.put(key, value);
    }

    @Override
    public <T, Z> boolean has(@NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type) {
        return customData.containsKey(key);
    }

    @Override
    public boolean has(@NotNull NamespacedKey key) {
        return customData.containsKey(key);
    }

    @Override
    public <T, Z> @Nullable Z get(@NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type) {
        return (Z) customData.get(key);
    }

    @Override
    public <T, Z> @NotNull Z getOrDefault(@NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type, @NotNull Z defaultValue) {
        Z value = get(key, type);
        return value != null ? value : defaultValue;
    }

    @Override
    public @NotNull Set<NamespacedKey> getKeys() {
        return customData.keySet();
    }

    @Override
    public void remove(@NotNull NamespacedKey key) {
        customData.remove(key);
    }

    @Override
    public boolean isEmpty() {
        return customData.isEmpty();
    }

    @Override
    public void copyTo(@NotNull PersistentDataContainer other, boolean replace) {
        if (other instanceof CraftPersistentDataContainer craftOther) {
            if (replace) {
                craftOther.customData.putAll(this.customData);
            } else {
                this.customData.forEach(craftOther.customData::putIfAbsent);
            }
        }
    }

    @Override
    public @NotNull PersistentDataAdapterContext getAdapterContext() {
        return new PersistentDataAdapterContext() {
            @Override
            public @NotNull PersistentDataContainer newPersistentDataContainer() {
                return new CraftPersistentDataContainer();
            }
        };
    }

    @Override
    public byte[] serializeToBytes() {
        return new byte[0]; // TODO: Implement if needed
    }

    @Override
    public void readFromBytes(byte[] bytes, boolean clear) {
        // TODO: Implement if needed
    }

    /**
     * Serializes this container to an NBT CompoundTag.
     */
    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<NamespacedKey, Object> entry : customData.entrySet()) {
            // Simplified serialization: we assume common types
            Object val = entry.getValue();
            if (val instanceof String s) tag.putString(entry.getKey().toString(), s);
            else if (val instanceof Integer i) tag.putInt(entry.getKey().toString(), i);
            else if (val instanceof Double d) tag.putDouble(entry.getKey().toString(), d);
            else if (val instanceof Boolean b) tag.putBoolean(entry.getKey().toString(), b);
            else if (val instanceof Long l) tag.putLong(entry.getKey().toString(), l);
            else if (val instanceof byte[] b) tag.putByteArray(entry.getKey().toString(), b);
            else if (val instanceof int[] i) tag.putIntArray(entry.getKey().toString(), i);
            else if (val instanceof long[] l) tag.putLongArray(entry.getKey().toString(), l);
        }
        return tag;
    }

    /**
     * Reads this container from an NBT CompoundTag.
     */
    public void fromTag(CompoundTag tag) {
        customData.clear();
        if (tag == null) return;
        for (String keyStr : tag.getAllKeys()) {
            try {
                NamespacedKey key = NamespacedKey.fromString(keyStr);
                if (key == null) continue;
                Tag value = tag.get(keyStr);
                if (value instanceof net.minecraft.nbt.StringTag st) customData.put(key, st.getAsString());
                else if (value instanceof net.minecraft.nbt.IntTag it) customData.put(key, it.getAsInt());
                else if (value instanceof net.minecraft.nbt.DoubleTag dt) customData.put(key, dt.getAsDouble());
                else if (value instanceof net.minecraft.nbt.ByteTag bt) customData.put(key, bt.getAsByte() != 0);
                else if (value instanceof net.minecraft.nbt.LongTag lt) customData.put(key, lt.getAsLong());
                else if (value instanceof net.minecraft.nbt.ByteArrayTag bat) customData.put(key, bat.getAsByteArray());
                else if (value instanceof net.minecraft.nbt.IntArrayTag iat) customData.put(key, iat.getAsIntArray());
                else if (value instanceof net.minecraft.nbt.LongArrayTag lat) customData.put(key, lat.getAsLongArray());
            } catch (Exception ignored) {}
        }
    }
}
