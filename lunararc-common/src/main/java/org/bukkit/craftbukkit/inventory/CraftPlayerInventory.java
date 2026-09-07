package org.bukkit.craftbukkit.inventory;

import net.minecraft.world.entity.player.Inventory;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;


public class CraftPlayerInventory implements PlayerInventory, org.bukkit.inventory.EntityEquipment {

    private static final int SIZE = 41;
    private final Inventory handle;
    private final Player owner;

    public CraftPlayerInventory(Inventory handle, Player owner) {
        this.handle = handle;
        this.owner = owner;
    }


    private net.minecraft.world.item.ItemStack nms(int slot) {
        if (slot < 36) return handle.items.get(slot);
        if (slot == 36) return handle.armor.get(0);
        if (slot == 37) return handle.armor.get(1);
        if (slot == 38) return handle.armor.get(2);
        if (slot == 39) return handle.armor.get(3);
        if (slot == 40) return handle.offhand.get(0);
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

    private void setNms(int slot, net.minecraft.world.item.ItemStack item) {
        setNmsNoSync(slot, item);
        finishMutation();
    }

    private void setNmsNoSync(int slot, net.minecraft.world.item.ItemStack item) {
        if (item == null) item = net.minecraft.world.item.ItemStack.EMPTY;
        if (slot >= 0 && slot < 36) { handle.items.set(slot, item); }
        else if (slot == 36) { handle.armor.set(0, item); }
        else if (slot == 37) { handle.armor.set(1, item); }
        else if (slot == 38) { handle.armor.set(2, item); }
        else if (slot == 39) { handle.armor.set(3, item); }
        else if (slot == 40) { handle.offhand.set(0, item); }
    }

    private void finishMutation() {
        handle.setChanged();
        syncToClient();
    }


    private void syncToClient() {
        try {
            if (owner instanceof org.bukkit.craftbukkit.entity.CraftPlayer cp) {
                net.minecraft.server.level.ServerPlayer sp = cp.getHandle();
                sp.inventoryMenu.broadcastChanges();
            }
        } catch (Throwable ignored) {}
    }


    @Override public @Nullable ItemStack getHelmet()      { return CraftItemStack.asBukkitCopy(nms(39)); }
    @Override public @Nullable ItemStack getChestplate()  { return CraftItemStack.asBukkitCopy(nms(38)); }
    @Override public @Nullable ItemStack getLeggings()    { return CraftItemStack.asBukkitCopy(nms(37)); }
    @Override public @Nullable ItemStack getBoots()       { return CraftItemStack.asBukkitCopy(nms(36)); }

    @Override public void setHelmet(@Nullable ItemStack helmet)         { setNms(39, CraftItemStack.asNMSCopy(helmet)); }
    public void setHelmet(@Nullable ItemStack helmet, boolean silent) { setHelmet(helmet); }
    @Override public void setChestplate(@Nullable ItemStack chestplate) { setNms(38, CraftItemStack.asNMSCopy(chestplate)); }
    public void setChestplate(@Nullable ItemStack chestplate, boolean silent) { setChestplate(chestplate); }
    @Override public void setLeggings(@Nullable ItemStack leggings)     { setNms(37, CraftItemStack.asNMSCopy(leggings)); }
    public void setLeggings(@Nullable ItemStack leggings, boolean silent) { setLeggings(leggings); }
    @Override public void setBoots(@Nullable ItemStack boots)           { setNms(36, CraftItemStack.asNMSCopy(boots)); }
    public void setBoots(@Nullable ItemStack boots, boolean silent) { setBoots(boots); }

    @Override
    public @NotNull ItemStack[] getArmorContents() {
        return new ItemStack[] { getBoots(), getLeggings(), getChestplate(), getHelmet() };
    }

    @Override
    public void setArmorContents(@NotNull ItemStack[] items) {
        if (items.length > 0) setBoots(items[0]);
        if (items.length > 1) setLeggings(items[1]);
        if (items.length > 2) setChestplate(items[2]);
        if (items.length > 3) setHelmet(items[3]);
    }


    @Override public @NotNull ItemStack getItemInMainHand() {
        net.minecraft.world.item.ItemStack nmsItem = handle.getSelected();
        if (nmsItem == null || nmsItem.isEmpty())
            return new CraftItemStack(net.minecraft.world.item.ItemStack.EMPTY);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override public void setItemInMainHand(@Nullable ItemStack item) {
        setNms(handle.selected, CraftItemStack.asNMSCopy(item));
    }
    public void setItemInMainHand(@Nullable ItemStack item, boolean silent) { setItemInMainHand(item); }

    @Override public @NotNull ItemStack getItemInOffHand() {
        net.minecraft.world.item.ItemStack nmsItem = nms(40);
        if (nmsItem == null || nmsItem.isEmpty())
            return new CraftItemStack(net.minecraft.world.item.ItemStack.EMPTY);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override public void setItemInOffHand(@Nullable ItemStack item) {
        setNms(40, CraftItemStack.asNMSCopy(item));
    }
    public void setItemInOffHand(@Nullable ItemStack item, boolean silent) { setItemInOffHand(item); }

    @Override public @NotNull ItemStack getItemInHand() { return getItemInMainHand(); }
    @Override public void setItemInHand(@Nullable ItemStack stack) { setItemInMainHand(stack); }

    @Override public int getHeldItemSlot() { return handle.selected; }
    @Override public void setHeldItemSlot(int slot) {
        if (slot < 0 || slot > 8) throw new IllegalArgumentException("Held item slot must be between 0 and 8");
        handle.selected = slot;
        if (owner instanceof org.bukkit.craftbukkit.entity.CraftPlayer cp && cp.getHandle().connection != null) {
            cp.getHandle().connection.send(new net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket(slot));
        }
        syncToClient();
    }

    @Override public @Nullable Player getHolder() { return owner; }


    @Override public float getDropChance(@NotNull EquipmentSlot slot) { Objects.requireNonNull(slot, "slot"); return 1.0F; }
    @Override public void setDropChance(@NotNull EquipmentSlot slot, float chance) { throw new UnsupportedOperationException("Cannot set equipment drop chance for players"); }
    @Override public float getItemInMainHandDropChance() { return getDropChance(EquipmentSlot.HAND); }
    @Override public void setItemInMainHandDropChance(float chance) { setDropChance(EquipmentSlot.HAND, chance); }
    @Override public float getItemInOffHandDropChance() { return getDropChance(EquipmentSlot.OFF_HAND); }
    @Override public void setItemInOffHandDropChance(float chance) { setDropChance(EquipmentSlot.OFF_HAND, chance); }
    @Override public float getItemInHandDropChance() { return getItemInMainHandDropChance(); }
    @Override public void setItemInHandDropChance(float chance) { setItemInMainHandDropChance(chance); }
    @Override public float getHelmetDropChance() { return getDropChance(EquipmentSlot.HEAD); }
    @Override public void setHelmetDropChance(float chance) { setDropChance(EquipmentSlot.HEAD, chance); }
    @Override public float getChestplateDropChance() { return getDropChance(EquipmentSlot.CHEST); }
    @Override public void setChestplateDropChance(float chance) { setDropChance(EquipmentSlot.CHEST, chance); }
    @Override public float getLeggingsDropChance() { return getDropChance(EquipmentSlot.LEGS); }
    @Override public void setLeggingsDropChance(float chance) { setDropChance(EquipmentSlot.LEGS, chance); }
    @Override public float getBootsDropChance() { return getDropChance(EquipmentSlot.FEET); }
    @Override public void setBootsDropChance(float chance) { setDropChance(EquipmentSlot.FEET, chance); }


    public @Nullable org.bukkit.entity.HumanEntity getHolder(boolean useSnapshot) { return owner; }

    public @NotNull EquipmentSlot getEquipmentSlotForItem(@NotNull ItemStack stack) {
        return EquipmentSlot.HAND;
    }


    @Override public @NotNull ItemStack[] getExtraContents() {
        return new ItemStack[]{ getItemInOffHand() };
    }

    @Override public void setExtraContents(@NotNull ItemStack[] items) {
        if (items != null && items.length > 0) setItemInOffHand(items[0]);
    }

    @Override public @NotNull ItemStack getItem(@NotNull EquipmentSlot slot) {
        if (slot == null) throw new IllegalArgumentException("slot must not be null");
        ItemStack result = switch (slot) {
            case HEAD -> getHelmet();
            case CHEST -> getChestplate();
            case LEGS -> getLeggings();
            case FEET -> getBoots();
            case OFF_HAND -> getItemInOffHand();
            case HAND -> getItemInMainHand();
            case BODY -> throw new IllegalArgumentException("BODY is not valid for players!");
        };
        return result == null ? new CraftItemStack(net.minecraft.world.item.ItemStack.EMPTY) : result;
    }

    @Override public void setItem(@NotNull EquipmentSlot slot, @Nullable ItemStack item) {
        if (slot == null) throw new IllegalArgumentException("slot must not be null");
        switch (slot) {
            case HEAD -> setHelmet(item);
            case CHEST -> setChestplate(item);
            case LEGS -> setLeggings(item);
            case FEET -> setBoots(item);
            case OFF_HAND -> setItemInOffHand(item);
            case HAND -> setItemInMainHand(item);
            case BODY -> throw new IllegalArgumentException("BODY is not valid for players!");
        }
    }
    public void setItem(@NotNull EquipmentSlot slot, @Nullable ItemStack item, boolean silent) { setItem(slot, item); }


    @Override public int getSize() { return SIZE; }

    @Override public int getMaxStackSize() {
        return ((io.ampznetwork.lunararc.common.bridge.PlayerInventoryBridge) handle).lunararc$getMaxStackSize();
    }
    @Override public void setMaxStackSize(int size) {
        ((io.ampznetwork.lunararc.common.bridge.PlayerInventoryBridge) handle).lunararc$setMaxStackSize(size);
    }

    @Override public @Nullable ItemStack getItem(int index) {
        if (index < 0 || index >= SIZE) return null;
        net.minecraft.world.item.ItemStack nms = nms(index);
        return nms.isEmpty() ? null : CraftItemStack.asBukkitCopy(nms);
    }

    @Override public void setItem(int index, @Nullable ItemStack item) {
        if (index < 0 || index >= SIZE) return;
        setNms(index, CraftItemStack.asNMSCopy(item));
    }

    @Override public @NotNull HashMap<Integer, ItemStack> addItem(@NotNull ItemStack... items) {
        HashMap<Integer, ItemStack> leftover = new HashMap<>();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item == null || item.getType() == Material.AIR) continue;
            int remaining = item.getAmount();
            for (int slot = 0; slot < 36 && remaining > 0; slot++) {
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

    @Override public @NotNull HashMap<Integer, ItemStack> removeItem(@NotNull ItemStack... items) {
        HashMap<Integer, ItemStack> leftover = new HashMap<>();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item == null) continue;
            int remaining = item.getAmount();
            for (int slot = 0; slot < 36 && remaining > 0; slot++) {
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
        ItemStack[] contents = new ItemStack[SIZE];
        for (int i = 0; i < SIZE; i++) contents[i] = getItem(i);
        return contents;
    }

    @Override public void setContents(@NotNull ItemStack[] items) {
        if (items.length > SIZE) {
            throw new IllegalArgumentException("Invalid inventory size; expected " + SIZE + " or less");
        }

        for (int i = 0; i < SIZE; i++) {
            net.minecraft.world.item.ItemStack value = i < items.length
                    ? CraftItemStack.asNMSCopy(items[i])
                    : net.minecraft.world.item.ItemStack.EMPTY;
            setNmsNoSync(i, value);
        }
        finishMutation();
    }

    @Override public @NotNull ItemStack[] getStorageContents() {
        ItemStack[] storage = new ItemStack[36];
        for (int i = 0; i < 36; i++) storage[i] = getItem(i);
        return storage;
    }

    @Override public void setStorageContents(@NotNull ItemStack[] items) {
        if (items.length > 36) {
            throw new IllegalArgumentException("Invalid inventory size; expected 36 or less");
        }
        for (int i = 0; i < 36; i++) {
            setNmsNoSync(i, i < items.length
                    ? CraftItemStack.asNMSCopy(items[i])
                    : net.minecraft.world.item.ItemStack.EMPTY);
        }
        finishMutation();
    }

    @Override public boolean contains(@NotNull Material material) {
        for (int i = 0; i < SIZE; i++) {
            ItemStack item = getItem(i);
            if (item != null && item.getType() == material) return true;
        }
        return false;
    }

    @Override public boolean contains(@Nullable ItemStack item) {
        if (item == null) return false;
        for (int i = 0; i < SIZE; i++) {
            ItemStack slot = getItem(i);
            if (item.equals(slot)) return true;
        }
        return false;
    }

    @Override public boolean contains(@NotNull Material material, int amount) {
        int found = 0;
        for (int i = 0; i < SIZE; i++) {
            ItemStack item = getItem(i);
            if (item != null && item.getType() == material) found += item.getAmount();
            if (found >= amount) return true;
        }
        return false;
    }

    @Override public boolean contains(@Nullable ItemStack item, int amount) {
        if (item == null) return false;
        if (amount < 1) return true;
        int found = 0;
        for (int i = 0; i < SIZE; i++) {
            ItemStack slot = getItem(i);
            if (item.equals(slot)) found++;
            if (found >= amount) return true;
        }
        return false;
    }

    @Override public boolean containsAtLeast(@Nullable ItemStack item, int amount) {
        if (item == null) return false;
        int found = 0;
        for (int i = 0; i < SIZE; i++) {
            ItemStack slot = getItem(i);
            if (slot != null && item.isSimilar(slot)) found += slot.getAmount();
            if (found >= amount) return true;
        }
        return false;
    }

    @Override public @NotNull HashMap<Integer, ? extends ItemStack> all(@NotNull Material material) {
        HashMap<Integer, ItemStack> result = new HashMap<>();
        for (int i = 0; i < SIZE; i++) {
            ItemStack item = getItem(i);
            if (item != null && item.getType() == material) result.put(i, item);
        }
        return result;
    }

    @Override public @NotNull HashMap<Integer, ? extends ItemStack> all(@Nullable ItemStack item) {
        HashMap<Integer, ItemStack> result = new HashMap<>();
        if (item == null) return result;
        for (int i = 0; i < SIZE; i++) {
            ItemStack slot = getItem(i);
            if (item.equals(slot)) result.put(i, slot);
        }
        return result;
    }

    @Override public int first(@NotNull Material material) {
        for (int i = 0; i < SIZE; i++) {
            ItemStack item = getItem(i);
            if (item != null && item.getType() == material) return i;
        }
        return -1;
    }

    @Override public int first(@NotNull ItemStack item) {
        for (int i = 0; i < SIZE; i++) {
            ItemStack slot = getItem(i);
            if (item.equals(slot)) return i;
        }
        return -1;
    }

    @Override public int firstEmpty() {
        for (int i = 0; i < 36; i++) {
            ItemStack item = getItem(i);
            if (item == null || item.getType() == Material.AIR) return i;
        }
        return -1;
    }

    @Override public boolean isEmpty() {
        for (int i = 0; i < SIZE; i++) {
            ItemStack item = getItem(i);
            if (item != null && item.getType() != Material.AIR) return false;
        }
        return true;
    }

    @Override public void remove(@NotNull Material material) {
        for (int i = 0; i < SIZE; i++) {
            ItemStack item = getItem(i);
            if (item != null && item.getType() == material) setItem(i, null);
        }
    }

    @Override public void remove(@NotNull ItemStack item) {
        for (int i = 0; i < SIZE; i++) {
            ItemStack slot = getItem(i);
            if (item.equals(slot)) setItem(i, null);
        }
    }

    @Override public void clear(int index) { setItem(index, null); }

    @Override public void clear() {
        for (int i = 0; i < SIZE; i++) {
            setNmsNoSync(i, net.minecraft.world.item.ItemStack.EMPTY);
        }
        finishMutation();
    }

    @Override public @NotNull List<HumanEntity> getViewers() {
        return java.util.Collections.singletonList(owner);
    }

    @Override public @NotNull InventoryType getType() { return InventoryType.PLAYER; }

    @Override public @NotNull ListIterator<ItemStack> iterator() {
        return new ListIterator<>() {
            int cursor = 0;
            @Override public boolean hasNext() { return cursor < SIZE; }
            @Override public ItemStack next() { return getItem(cursor++); }
            @Override public boolean hasPrevious() { return cursor > 0; }
            @Override public ItemStack previous() { return getItem(--cursor); }
            @Override public int nextIndex() { return cursor; }
            @Override public int previousIndex() { return cursor - 1; }
            @Override public void remove() { setItem(cursor - 1, null); }
            @Override public void set(ItemStack item) { setItem(cursor - 1, item); }
            @Override public void add(ItemStack item) {
                if (cursor >= SIZE) throw new IllegalStateException("No inventory slot available at iterator cursor");
                setItem(cursor, item);
                cursor++;
            }
        };
    }

    @Override public @NotNull ListIterator<ItemStack> iterator(int index) {
        ListIterator<ItemStack> it = iterator();
        for (int i = 0; i < index; i++) if (it.hasNext()) it.next();
        return it;
    }

    @Override public @Nullable org.bukkit.Location getLocation() {
        return owner != null ? owner.getLocation() : null;
    }


    public @NotNull HashMap<Integer, ItemStack> removeItemAnySlot(@NotNull ItemStack... items) {
        return removeItem(items);
    }

    public int close() {

        return 0;
    }
}
