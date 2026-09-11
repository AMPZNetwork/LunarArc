package org.bukkit.craftbukkit.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class CraftInventoryView implements InventoryView {
    private final org.bukkit.craftbukkit.entity.CraftPlayer player;
    private final AbstractContainerMenu handle;
    private final Inventory top;
    private final Inventory bottom;
    private final InventoryType type;
    private final net.kyori.adventure.text.Component title;
    private final String originalTitle;
    private String legacyTitle;

    public CraftInventoryView(org.bukkit.craftbukkit.entity.CraftPlayer player,
                              AbstractContainerMenu handle,
                              @Nullable Inventory top,
                              @Nullable Inventory bottom,
                              @NotNull InventoryType type,
                              @NotNull net.kyori.adventure.text.Component title) {
        this.player = player;
        this.handle = handle;
        this.bottom = bottom != null ? bottom : player.getInventory();


        int inferredTop = lunararc$inferTopSlotCount(player, handle, type);
        this.top = top != null ? top : new MenuSlotInventory(handle, inferredTop, type, player);
        this.type = type;
        this.title = title;
        this.originalTitle = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(title);
        this.legacyTitle = this.originalTitle;
    }

    public AbstractContainerMenu getHandle() { return handle; }

    /**
     * Finds the start of the real NMS player inventory instead of assuming every menu
     * appends exactly 36 vanilla player slots. Modded menus may expose additional or
     * differently-arranged slots; their own slot/container graph remains authoritative.
     */
    private static int lunararc$inferTopSlotCount(org.bukkit.craftbukkit.entity.CraftPlayer player,
                                                   AbstractContainerMenu handle, InventoryType type) {
        net.minecraft.world.entity.player.Inventory playerInventory = player.getHandle().getInventory();
        for (int raw = 0; raw < handle.slots.size(); raw++) {
            Slot slot = handle.slots.get(raw);
            if (slot.container == playerInventory) return raw;
        }

        // InventoryMenu has crafting/armour/offhand slots backed by the same player object
        // in non-contiguous ranges, so preserve its explicit Bukkit top layout.
        if (type == InventoryType.CRAFTING) return Math.min(5, handle.slots.size());

        // Last-resort compatibility for menus that wrap the player inventory in another
        // Container implementation and therefore cannot be identified by identity.
        return Math.max(0, handle.slots.size() - Math.min(36, handle.slots.size()));
    }

    @Override public @NotNull Inventory getTopInventory() { return top; }
    @Override public @NotNull Inventory getBottomInventory() { return bottom; }
    @Override public @NotNull HumanEntity getPlayer() { return player; }
    @Override public @NotNull InventoryType getType() { return type; }

    @Override
    public void setItem(int slot, @Nullable ItemStack item) {
        if (slot == OUTSIDE || slot < 0 || slot >= handle.slots.size()) return;
        handle.getSlot(slot).set(CraftItemStack.asNMSCopy(item));
        handle.broadcastChanges();
    }

    @Override
    public @Nullable ItemStack getItem(int slot) {
        if (slot == OUTSIDE || slot < 0 || slot >= handle.slots.size()) return null;
        return CraftItemStack.asBukkitCopy(handle.getSlot(slot).getItem());
    }

    @Override public void setCursor(@Nullable ItemStack item) {
        handle.setCarried(CraftItemStack.asNMSCopy(item));
        handle.broadcastChanges();
    }
    @Override public @NotNull ItemStack getCursor() { return CraftItemStack.asBukkitCopy(handle.getCarried()); }

    @Override
    public @Nullable Inventory getInventory(int rawSlot) {
        if (rawSlot == OUTSIDE || rawSlot < 0 || rawSlot >= countSlots()) return null;
        return rawSlot < top.getSize() ? top : bottom;
    }

    @Override
    public int convertSlot(int rawSlot) {
        if (rawSlot < top.getSize()) return rawSlot;


        if (type == InventoryType.CRAFTING && bottom.getType() == InventoryType.PLAYER) {
            if (rawSlot >= 5 && rawSlot <= 8) return 39 - (rawSlot - 5);
            if (rawSlot >= 9 && rawSlot <= 35) return rawSlot;
            if (rawSlot >= 36 && rawSlot <= 44) return rawSlot - 36;
            if (rawSlot == 45) return 40;
            return rawSlot - top.getSize();
        }

        int slot = rawSlot - top.getSize();


        if (bottom.getType() == InventoryType.PLAYER && slot >= 0 && slot < 36) {
            if (slot < 27) return slot + 9;
            return slot - 27;
        }
        return slot;
    }

    @Override
    public InventoryType.@NotNull SlotType getSlotType(int slot) {
        if (slot < 0 || slot >= handle.slots.size()) return InventoryType.SlotType.OUTSIDE;
        if (slot >= top.getSize()) return InventoryType.SlotType.CONTAINER;
        return switch (type) {
            case FURNACE, BLAST_FURNACE, SMOKER -> slot == 2 ? InventoryType.SlotType.RESULT : (slot == 1 ? InventoryType.SlotType.FUEL : InventoryType.SlotType.CRAFTING);
            case CRAFTING, WORKBENCH -> slot == 0 ? InventoryType.SlotType.RESULT : InventoryType.SlotType.CRAFTING;
            case MERCHANT -> slot == 2 ? InventoryType.SlotType.RESULT : InventoryType.SlotType.CRAFTING;
            case ANVIL, SMITHING -> slot == top.getSize() - 1 ? InventoryType.SlotType.RESULT : InventoryType.SlotType.CRAFTING;
            case GRINDSTONE -> slot == 2 ? InventoryType.SlotType.RESULT : InventoryType.SlotType.CRAFTING;
            default -> InventoryType.SlotType.CONTAINER;
        };
    }

    @Override public void close() { player.closeInventory(); }
    @Override public int countSlots() { return handle.slots.size(); }

    @Override
    public boolean setProperty(@NotNull Property prop, int value) {
        if (prop.getType() != type) return false;
        try {
            handle.setData(prop.getId(), value);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override public @NotNull net.kyori.adventure.text.Component title() { return title; }
    @Override @Deprecated public @NotNull String getTitle() { return this.legacyTitle; }
    @Override @Deprecated public @NotNull String getOriginalTitle() { return this.originalTitle; }
    @Override @Deprecated public void setTitle(@NotNull String title) {
        sendInventoryTitleChange(this, title);
        this.legacyTitle = title;
    }

    /**
     * Mirrors CraftBukkit/Paper's live title update: the open menu remains the same
     * server-side container and the client receives a replacement open-screen packet
     * for the existing container id. Modded menu types therefore stay loader-owned.
     */
    public static void sendInventoryTitleChange(@NotNull InventoryView view, @NotNull String title) {
        java.util.Objects.requireNonNull(view, "InventoryView cannot be null");
        java.util.Objects.requireNonNull(title, "Title cannot be null");
        if (!(view instanceof CraftInventoryView craftView)) {
            throw new IllegalArgumentException("InventoryView must be a LunarArc/CraftInventoryView");
        }
        if (!view.getTopInventory().getType().isCreatable()) {
            throw new IllegalArgumentException("Only creatable inventories can have their title changed");
        }

        ServerPlayer handle = craftView.player.getHandle();
        if (handle.connection == null) return;
        handle.connection.send(new net.minecraft.network.protocol.game.ClientboundOpenScreenPacket(
                craftView.handle.containerId, craftView.handle.getType(),
                org.bukkit.craftbukkit.util.CraftChatMessage.fromString(title)[0]));
        craftView.player.updateInventory();
    }


    protected static class MenuSlotInventory extends CraftInventory {
        protected final AbstractContainerMenu menu;
        protected final int size;
        protected final org.bukkit.inventory.InventoryHolder owner;

        MenuSlotInventory(AbstractContainerMenu menu, int size, InventoryType type,
                          org.bukkit.inventory.InventoryHolder owner) {
            super(owner, Math.max(1, size), type, net.kyori.adventure.text.Component.text(type.name()));
            this.menu = menu;
            this.size = Math.max(0, size);
            this.owner = owner;
        }
        @Override public int getSize() { return size; }
        @Override public @Nullable ItemStack getItem(int index) {
            if (index < 0 || index >= size || index >= menu.slots.size()) return null;
            return CraftItemStack.asBukkitCopy(menu.getSlot(index).getItem());
        }
        @Override public void setItem(int index, @Nullable ItemStack item) {
            if (index < 0 || index >= size || index >= menu.slots.size()) return;
            menu.getSlot(index).set(CraftItemStack.asNMSCopy(item));
            menu.broadcastChanges();
        }
        @Override public @NotNull ItemStack[] getContents() {
            ItemStack[] out = new ItemStack[size];
            for (int i = 0; i < size; i++) out[i] = getItem(i);
            return out;
        }
        @Override public void setContents(@NotNull ItemStack[] items) {
            if (items.length > size) throw new IllegalArgumentException("items array too large");
            for (int i = 0; i < size; i++) setItem(i, i < items.length ? items[i] : null);
        }
        @Override public @NotNull ItemStack[] getStorageContents() { return getContents(); }
        @Override public void setStorageContents(@NotNull ItemStack[] items) { setContents(items); }

        @Override public @NotNull java.util.HashMap<Integer, ItemStack> addItem(@NotNull ItemStack... items) {
            java.util.HashMap<Integer, ItemStack> leftover = new java.util.HashMap<>();
            for (int input = 0; input < items.length; input++) {
                ItemStack item = items[input];
                if (item == null || item.getType() == org.bukkit.Material.AIR) continue;
                int remaining = item.getAmount();
                for (int slot = 0; slot < size && remaining > 0; slot++) {
                    ItemStack existing = getItem(slot);
                    if (existing == null || existing.getType() == org.bukkit.Material.AIR) {
                        int placedAmount = Math.min(remaining, Math.min(item.getMaxStackSize(), getMaxStackSize()));
                        ItemStack placed = item.clone(); placed.setAmount(placedAmount); setItem(slot, placed); remaining -= placedAmount;
                    } else if (existing.isSimilar(item)) {
                        int space = Math.min(existing.getMaxStackSize(), getMaxStackSize()) - existing.getAmount();
                        if (space > 0) { int add = Math.min(remaining, space); existing.setAmount(existing.getAmount() + add); setItem(slot, existing); remaining -= add; }
                    }
                }
                if (remaining > 0) { ItemStack left = item.clone(); left.setAmount(remaining); leftover.put(input, left); }
            }
            return leftover;
        }
        @Override public @NotNull java.util.HashMap<Integer, ItemStack> removeItem(@NotNull ItemStack... items) {
            java.util.HashMap<Integer, ItemStack> leftover = new java.util.HashMap<>();
            for (int input = 0; input < items.length; input++) {
                ItemStack item = items[input]; if (item == null) continue; int remaining = item.getAmount();
                for (int slot = 0; slot < size && remaining > 0; slot++) {
                    ItemStack existing = getItem(slot);
                    if (existing != null && existing.isSimilar(item)) {
                        int remove = Math.min(remaining, existing.getAmount()); existing.setAmount(existing.getAmount() - remove);
                        setItem(slot, existing.getAmount() <= 0 ? null : existing); remaining -= remove;
                    }
                }
                if (remaining > 0) { ItemStack left = item.clone(); left.setAmount(remaining); leftover.put(input, left); }
            }
            return leftover;
        }
        @Override public @NotNull java.util.HashMap<Integer, ItemStack> removeItemAnySlot(@NotNull ItemStack... items) { return removeItem(items); }
        @Override public boolean contains(@NotNull org.bukkit.Material material) { return first(material) >= 0; }
        @Override public boolean contains(@Nullable ItemStack item) { return item != null && first(item) >= 0; }
        @Override public boolean contains(@NotNull org.bukkit.Material material, int amount) { int found=0; for(int i=0;i<size;i++){ItemStack s=getItem(i); if(s!=null&&s.getType()==material) found+=s.getAmount(); if(found>=amount)return true;} return false; }
        @Override public boolean contains(@Nullable ItemStack item, int amount) { int found=0; if(item==null)return false; for(int i=0;i<size;i++){ItemStack s=getItem(i); if(item.equals(s)) found++; if(found>=amount)return true;} return amount < 1; }
        @Override public boolean containsAtLeast(@Nullable ItemStack item, int amount) { int found=0; if(item==null)return false; for(int i=0;i<size;i++){ItemStack s=getItem(i); if(s!=null&&item.isSimilar(s)) found+=s.getAmount(); if(found>=amount)return true;} return amount < 1; }
        @Override public @NotNull java.util.HashMap<Integer, ? extends ItemStack> all(@NotNull org.bukkit.Material material) { java.util.HashMap<Integer,ItemStack> out=new java.util.HashMap<>(); for(int i=0;i<size;i++){ItemStack s=getItem(i); if(s!=null&&s.getType()==material)out.put(i,s);} return out; }
        @Override public @NotNull java.util.HashMap<Integer, ? extends ItemStack> all(@Nullable ItemStack item) { java.util.HashMap<Integer,ItemStack> out=new java.util.HashMap<>(); if(item!=null)for(int i=0;i<size;i++){ItemStack s=getItem(i); if(item.equals(s))out.put(i,s);} return out; }
        @Override public int first(@NotNull org.bukkit.Material material) { for(int i=0;i<size;i++){ItemStack s=getItem(i); if(s!=null&&s.getType()==material)return i;} return -1; }
        @Override public int first(@NotNull ItemStack item) { for(int i=0;i<size;i++)if(item.equals(getItem(i)))return i; return -1; }
        @Override public int firstEmpty() { for(int i=0;i<size;i++){ItemStack s=getItem(i); if(s==null||s.getType()==org.bukkit.Material.AIR)return i;} return -1; }
        @Override public boolean isEmpty() { return firstEmpty()==0 && java.util.Arrays.stream(getContents()).allMatch(s -> s==null || s.getType()==org.bukkit.Material.AIR); }
        @Override public void remove(@NotNull org.bukkit.Material material) { for(int i=0;i<size;i++){ItemStack s=getItem(i); if(s!=null&&s.getType()==material)setItem(i,null);} }
        @Override public void remove(@NotNull ItemStack item) { for(int i=0;i<size;i++)if(item.equals(getItem(i)))setItem(i,null); }
        @Override public void clear(int index) { setItem(index,null); }
        @Override public void clear() { for(int i=0;i<size;i++)setItem(i,null); }
        private java.util.List<ItemStack> liveList() {
            return new java.util.AbstractList<>() {
                @Override public ItemStack get(int index) { return MenuSlotInventory.this.getItem(index); }
                @Override public int size() { return MenuSlotInventory.this.size; }
                @Override public ItemStack set(int index, ItemStack element) { ItemStack previous=get(index); MenuSlotInventory.this.setItem(index,element); return previous; }
            };
        }
        @Override public @NotNull java.util.ListIterator<ItemStack> iterator() { return liveList().listIterator(); }
        @Override public @NotNull java.util.ListIterator<ItemStack> iterator(int index) { return liveList().listIterator(index); }
        @Override public @NotNull java.util.List<HumanEntity> getViewers() {
            return java.util.Collections.singletonList((HumanEntity) owner);
        }
    }

    /** Whether a raw slot index falls in the top inventory, as CraftBukkit asks it. */
    public boolean isInTop(int rawSlot) {
        return rawSlot < this.top.getSize();
    }
}
