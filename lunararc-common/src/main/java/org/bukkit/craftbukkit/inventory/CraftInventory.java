package org.bukkit.craftbukkit.inventory;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;


public class CraftInventory implements Inventory {

    protected final ItemStack[] contents;
    protected final InventoryType type;
    protected final InventoryHolder holder;
    protected net.kyori.adventure.text.Component title;
    protected int maxStackSize = 64;
    private final java.util.Set<HumanEntity> viewers = java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());

    public CraftInventory(@Nullable InventoryHolder holder, @NotNull InventoryType type) {
        this(holder, type.getDefaultSize(), type, net.kyori.adventure.text.Component.text(type.name()));
    }

    public CraftInventory(@Nullable InventoryHolder holder, int size, @NotNull InventoryType type,
            @NotNull net.kyori.adventure.text.Component title) {
        this.holder = holder;
        this.type = type;
        this.title = title;
        this.contents = new ItemStack[Math.max(1, size)];
    }

    public CraftInventory(@Nullable InventoryHolder holder, int size,
            @NotNull net.kyori.adventure.text.Component title) {
        this(holder, size, InventoryType.CHEST, title);
    }

    @Override
    public int getSize() {
        return contents.length;
    }

    @Override
    public int getMaxStackSize() {
        return maxStackSize;
    }

    @Override
    public void setMaxStackSize(int size) {
        this.maxStackSize = size;
    }

    @Override
    public @Nullable ItemStack getItem(int index) {
        if (index < 0 || index >= contents.length) return null;
        return contents[index];
    }

    @Override
    public void setItem(int index, @Nullable ItemStack item) {
        if (index < 0 || index >= contents.length) return;
        contents[index] = item;
    }

    @Override
    public @NotNull HashMap<Integer, ItemStack> addItem(@NotNull ItemStack... items) {
        HashMap<Integer, ItemStack> leftover = new HashMap<>();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item == null || item.getType() == Material.AIR) continue;
            int remaining = item.getAmount();
            for (int slot = 0; slot < contents.length && remaining > 0; slot++) {
                ItemStack existing = contents[slot];
                if (existing == null || existing.getType() == Material.AIR) {
                    int toPlace = Math.min(remaining, Math.min(item.getMaxStackSize(), getMaxStackSize()));
                    ItemStack placed = item.clone();
                    placed.setAmount(toPlace);
                    contents[slot] = placed;
                    remaining -= toPlace;
                } else if (existing.isSimilar(item)) {
                    int space = Math.min(existing.getMaxStackSize(), getMaxStackSize()) - existing.getAmount();
                    if (space > 0) {
                        int toAdd = Math.min(remaining, space);
                        existing.setAmount(existing.getAmount() + toAdd);
                        remaining -= toAdd;
                    }
                }
            }
            if (remaining > 0) {
                ItemStack lr = item.clone();
                lr.setAmount(remaining);
                leftover.put(i, lr);
            }
        }
        return leftover;
    }

    @Override
    public @NotNull HashMap<Integer, ItemStack> removeItem(@NotNull ItemStack... items) {
        HashMap<Integer, ItemStack> leftover = new HashMap<>();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item == null) continue;
            int remaining = item.getAmount();
            for (int slot = 0; slot < contents.length && remaining > 0; slot++) {
                ItemStack existing = contents[slot];
                if (existing != null && existing.isSimilar(item)) {
                    int toRemove = Math.min(remaining, existing.getAmount());
                    existing.setAmount(existing.getAmount() - toRemove);
                    contents[slot] = existing.getAmount() == 0 ? null : existing;
                    remaining -= toRemove;
                }
            }
            if (remaining > 0) {
                ItemStack lr = item.clone();
                lr.setAmount(remaining);
                leftover.put(i, lr);
            }
        }
        return leftover;
    }

    @Override
    public @NotNull ItemStack[] getContents() {
        ItemStack[] copy = new ItemStack[contents.length];
        System.arraycopy(contents, 0, copy, 0, contents.length);
        return copy;
    }

    @Override
    public void setContents(@NotNull ItemStack[] items) throws IllegalArgumentException {
        if (items.length > contents.length)
            throw new IllegalArgumentException("items array too large");
        for (int i = 0; i < items.length; i++) contents[i] = items[i];
        for (int i = items.length; i < contents.length; i++) contents[i] = null;
    }

    @Override
    public @NotNull ItemStack[] getStorageContents() {
        return getContents();
    }

    @Override
    public void setStorageContents(@NotNull ItemStack[] items) throws IllegalArgumentException {
        setContents(items);
    }

    @Override
    public boolean contains(@NotNull Material material) {
        for (ItemStack item : contents)
            if (item != null && item.getType() == material) return true;
        return false;
    }

    @Override
    public boolean contains(@Nullable ItemStack item) {
        if (item == null) return false;
        for (ItemStack slot : contents)
            if (item.equals(slot)) return true;
        return false;
    }

    @Override
    public boolean contains(@NotNull Material material, int amount) {
        int found = 0;
        for (ItemStack item : contents) {
            if (item != null && item.getType() == material) found += item.getAmount();
            if (found >= amount) return true;
        }
        return false;
    }

    @Override
    public boolean contains(@Nullable ItemStack item, int amount) {
        if (item == null) return false;
        if (amount < 1) return true;
        int found = 0;
        for (ItemStack slot : contents) {
            if (item.equals(slot)) found++;
            if (found >= amount) return true;
        }
        return false;
    }

    @Override
    public boolean containsAtLeast(@Nullable ItemStack item, int amount) {
        if (item == null) return false;
        int found = 0;
        for (ItemStack slot : contents) {
            if (slot != null && item.isSimilar(slot)) found += slot.getAmount();
            if (found >= amount) return true;
        }
        return false;
    }

    @Override
    public @NotNull HashMap<Integer, ? extends ItemStack> all(@NotNull Material material) {
        HashMap<Integer, ItemStack> result = new HashMap<>();
        for (int i = 0; i < contents.length; i++)
            if (contents[i] != null && contents[i].getType() == material) result.put(i, contents[i]);
        return result;
    }

    @Override
    public @NotNull HashMap<Integer, ? extends ItemStack> all(@Nullable ItemStack item) {
        HashMap<Integer, ItemStack> result = new HashMap<>();
        if (item == null) return result;
        for (int i = 0; i < contents.length; i++)
            if (item.equals(contents[i])) result.put(i, contents[i]);
        return result;
    }

    @Override
    public int first(@NotNull Material material) {
        for (int i = 0; i < contents.length; i++)
            if (contents[i] != null && contents[i].getType() == material) return i;
        return -1;
    }

    @Override
    public int first(@NotNull ItemStack item) {
        for (int i = 0; i < contents.length; i++)
            if (item.equals(contents[i])) return i;
        return -1;
    }

    @Override
    public int firstEmpty() {
        for (int i = 0; i < contents.length; i++)
            if (contents[i] == null || contents[i].getType() == Material.AIR) return i;
        return -1;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack item : contents)
            if (item != null && item.getType() != Material.AIR) return false;
        return true;
    }

    @Override
    public void remove(@NotNull Material material) {
        for (int i = 0; i < contents.length; i++)
            if (contents[i] != null && contents[i].getType() == material) contents[i] = null;
    }

    @Override
    public void remove(@NotNull ItemStack item) {
        for (int i = 0; i < contents.length; i++)
            if (item.equals(contents[i])) contents[i] = null;
    }

    @Override
    public void clear(int index) {
        if (index >= 0 && index < contents.length) contents[index] = null;
    }

    @Override
    public void clear() {
        java.util.Arrays.fill(contents, null);
    }

    public int close() {
        return 0;
    }

    @Override
    public @NotNull List<HumanEntity> getViewers() {
        return java.util.List.copyOf(viewers);
    }

    public void onOpen(@NotNull HumanEntity viewer) { viewers.add(viewer); }
    public void onClose(@NotNull HumanEntity viewer) { viewers.remove(viewer); }
    public @NotNull net.kyori.adventure.text.Component title() { return title; }


    @Override
    public @NotNull InventoryType getType() {
        return type;
    }

    @Override
    public @Nullable InventoryHolder getHolder() {
        return holder;
    }

    public @Nullable InventoryHolder getHolder(boolean useSnapshot) {
        return holder;
    }

    @Override
    public @NotNull HashMap<Integer, ItemStack> removeItemAnySlot(@NotNull ItemStack... items) {
        return removeItem(items);
    }

    @Override
    public @NotNull ListIterator<ItemStack> iterator() {
        return java.util.Arrays.asList(contents).listIterator();
    }

    @Override
    public @NotNull ListIterator<ItemStack> iterator(int index) {
        return java.util.Arrays.asList(contents).listIterator(index);
    }

    @Override
    public @Nullable Location getLocation() {
        return null;
    }

}
