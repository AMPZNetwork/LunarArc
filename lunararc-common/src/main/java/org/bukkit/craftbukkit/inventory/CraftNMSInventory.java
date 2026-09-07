package org.bukkit.craftbukkit.inventory;

import net.minecraft.world.Container;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;


public class CraftNMSInventory implements Inventory {

    private final Container handle;
    private final InventoryHolder owner;
    private final InventoryType type;

    public CraftNMSInventory(Container handle, InventoryHolder owner) {
        this(handle, owner, InventoryType.CHEST);
    }

    public CraftNMSInventory(Container handle, InventoryHolder owner, InventoryType type) {
        this.handle = java.util.Objects.requireNonNull(handle, "handle");
        this.owner = owner;
        this.type = java.util.Objects.requireNonNull(type, "type");
    }

    public Container getHandle() { return handle; }

    @Override public int getSize() { return handle.getContainerSize(); }
    @Override public int getMaxStackSize() {
        if (handle instanceof io.ampznetwork.lunararc.common.bridge.SimpleContainerBridge bridge) {
            return bridge.lunararc$getMaxStackSize();
        }
        return handle.getMaxStackSize();
    }
    @Override public void setMaxStackSize(int size) {
        if (handle instanceof io.ampznetwork.lunararc.common.bridge.SimpleContainerBridge bridge) {
            bridge.lunararc$setMaxStackSize(size);
            handle.setChanged();
            return;
        }
        throw new UnsupportedOperationException(
                "Container does not expose mutable max-stack state: " + handle.getClass().getName());
    }

    @Override public @Nullable ItemStack getItem(int index) {
        if (index < 0 || index >= getSize()) return null;
        net.minecraft.world.item.ItemStack nms = handle.getItem(index);
        return nms.isEmpty() ? null : CraftItemStack.asBukkitCopy(nms);
    }

    @Override public void setItem(int index, @Nullable ItemStack item) {
        if (index < 0 || index >= getSize()) return;
        handle.setItem(index, CraftItemStack.asNMSCopy(item));
        handle.setChanged();
    }

    @Override public @NotNull HashMap<Integer, ItemStack> addItem(@NotNull ItemStack... items) {
        HashMap<Integer, ItemStack> leftover = new HashMap<>();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item == null || item.getType() == Material.AIR) continue;
            int remaining = item.getAmount();
            for (int slot = 0; slot < getSize() && remaining > 0; slot++) {
                ItemStack existing = getItem(slot);
                if (existing == null || existing.getType() == Material.AIR) {
                    int toPlace = Math.min(remaining, Math.min(item.getMaxStackSize(), getMaxStackSize()));
                    ItemStack placed = item.clone();
                    placed.setAmount(toPlace);
                    setItem(slot, placed);
                    remaining -= toPlace;
                } else if (existing.isSimilar(item)) {
                    int space = Math.min(existing.getMaxStackSize(), getMaxStackSize()) - existing.getAmount();
                    if (space > 0) {
                        int toAdd = Math.min(remaining, space);
                        existing.setAmount(existing.getAmount() + toAdd);
                        setItem(slot, existing);
                        remaining -= toAdd;
                    }
                }
            }
            if (remaining > 0) {
                ItemStack leftoverItem = item.clone();
                leftoverItem.setAmount(remaining);
                leftover.put(i, leftoverItem);
            }
        }
        return leftover;
    }

    @Override public @NotNull HashMap<Integer, ItemStack> removeItemAnySlot(@NotNull ItemStack... items) throws IllegalArgumentException { return removeItem(items); }

    @Override public @NotNull HashMap<Integer, ItemStack> removeItem(@NotNull ItemStack... items) {
        HashMap<Integer, ItemStack> leftover = new HashMap<>();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item == null) continue;
            int remaining = item.getAmount();
            for (int slot = 0; slot < getSize() && remaining > 0; slot++) {
                ItemStack existing = getItem(slot);
                if (existing != null && existing.isSimilar(item)) {
                    int toRemove = Math.min(remaining, existing.getAmount());
                    existing.setAmount(existing.getAmount() - toRemove);
                    setItem(slot, existing.getAmount() == 0 ? null : existing);
                    remaining -= toRemove;
                }
            }
            if (remaining > 0) {
                ItemStack leftoverItem = item.clone();
                leftoverItem.setAmount(remaining);
                leftover.put(i, leftoverItem);
            }
        }
        return leftover;
    }

    @Override public @NotNull ItemStack[] getContents() {
        ItemStack[] contents = new ItemStack[getSize()];
        for (int i = 0; i < getSize(); i++) contents[i] = getItem(i);
        return contents;
    }

    @Override public void setContents(@NotNull ItemStack[] items) {
        for (int i = 0; i < Math.min(items.length, getSize()); i++) setItem(i, items[i]);
    }

    @Override public @NotNull ItemStack[] getStorageContents() { return getContents(); }
    @Override public void setStorageContents(@NotNull ItemStack[] items) { setContents(items); }

    @Override public boolean contains(@NotNull Material material) {
        for (int i = 0; i < getSize(); i++) {
            ItemStack item = getItem(i);
            if (item != null && item.getType() == material) return true;
        }
        return false;
    }

    @Override public boolean contains(@Nullable ItemStack item) {
        if (item == null) return false;
        for (int i = 0; i < getSize(); i++) {
            ItemStack slot = getItem(i);
            if (item.equals(slot)) return true;
        }
        return false;
    }

    @Override public boolean contains(@NotNull Material material, int amount) {
        int found = 0;
        for (int i = 0; i < getSize(); i++) {
            ItemStack item = getItem(i);
            if (item != null && item.getType() == material) {
                found += item.getAmount();
                if (found >= amount) return true;
            }
        }
        return false;
    }

    @Override public boolean contains(@Nullable ItemStack item, int amount) {
        if (item == null) return false;
        if (amount < 1) return true;
        int found = 0;
        for (int i = 0; i < getSize(); i++) {
            ItemStack slot = getItem(i);
            if (item.equals(slot)) found++;
            if (found >= amount) return true;
        }
        return false;
    }

    @Override public boolean containsAtLeast(@Nullable ItemStack item, int amount) {
        if (item == null) return false;
        int found = 0;
        for (int i = 0; i < getSize(); i++) {
            ItemStack slot = getItem(i);
            if (slot != null && item.isSimilar(slot)) found += slot.getAmount();
            if (found >= amount) return true;
        }
        return false;
    }

    @Override public @NotNull HashMap<Integer, ? extends ItemStack> all(@NotNull Material material) {
        HashMap<Integer, ItemStack> result = new HashMap<>();
        for (int i = 0; i < getSize(); i++) {
            ItemStack item = getItem(i);
            if (item != null && item.getType() == material) result.put(i, item);
        }
        return result;
    }

    @Override public @NotNull HashMap<Integer, ? extends ItemStack> all(@Nullable ItemStack item) {
        HashMap<Integer, ItemStack> result = new HashMap<>();
        if (item == null) return result;
        for (int i = 0; i < getSize(); i++) {
            ItemStack slot = getItem(i);
            if (item.equals(slot)) result.put(i, slot);
        }
        return result;
    }

    @Override public int first(@NotNull Material material) {
        for (int i = 0; i < getSize(); i++) {
            ItemStack item = getItem(i);
            if (item != null && item.getType() == material) return i;
        }
        return -1;
    }

    @Override public int first(@NotNull ItemStack item) {
        for (int i = 0; i < getSize(); i++) {
            ItemStack slot = getItem(i);
            if (item.equals(slot)) return i;
        }
        return -1;
    }

    @Override public int firstEmpty() {
        for (int i = 0; i < getSize(); i++) {
            ItemStack item = getItem(i);
            if (item == null || item.getType() == Material.AIR) return i;
        }
        return -1;
    }

    @Override public boolean isEmpty() {
        for (int i = 0; i < getSize(); i++) {
            ItemStack item = getItem(i);
            if (item != null && item.getType() != Material.AIR) return false;
        }
        return true;
    }

    @Override public void remove(@NotNull Material material) {
        for (int i = 0; i < getSize(); i++) {
            ItemStack item = getItem(i);
            if (item != null && item.getType() == material) setItem(i, null);
        }
    }

    @Override public void remove(@NotNull ItemStack item) {
        for (int i = 0; i < getSize(); i++) {
            ItemStack slot = getItem(i);
            if (item.equals(slot)) setItem(i, null);
        }
    }

    @Override public void clear(int index) { setItem(index, null); }

    @Override public void clear() {
        handle.clearContent();
        handle.setChanged();
    }

    @Override public @NotNull List<HumanEntity> getViewers() {
        return owner instanceof HumanEntity h ? java.util.Collections.singletonList(h) : java.util.Collections.emptyList();
    }

    @Override public @NotNull InventoryType getType() { return type; }

    @Override public @Nullable InventoryHolder getHolder() { return owner; }

    @Override public @Nullable InventoryHolder getHolder(boolean useSnapshot) { return owner; }

    @Override public @NotNull ListIterator<ItemStack> iterator() {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < getSize(); i++) list.add(getItem(i));
        return list.listIterator();
    }

    @Override public @NotNull ListIterator<ItemStack> iterator(int index) {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < getSize(); i++) list.add(getItem(i));
        return list.listIterator(index);
    }

    @Override public @Nullable Location getLocation() {
        return owner instanceof org.bukkit.entity.Entity e ? e.getLocation() : null;
    }

    public int close() { return 0; }
}
