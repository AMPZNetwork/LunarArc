package io.papermc.paper.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;

public final class PaperInventoryCustomHolderContainer implements Container {
    private final InventoryHolder owner;
    private final Container delegate;
    private final InventoryType type;
    private final net.kyori.adventure.text.Component title;
    private final java.util.List<HumanEntity> viewers = new java.util.ArrayList<>();
    private Integer maxStackSize;

    public PaperInventoryCustomHolderContainer(InventoryHolder owner, Container delegate, InventoryType type) {
        this.owner = owner;
        this.delegate = java.util.Objects.requireNonNull(delegate);
        this.type = java.util.Objects.requireNonNull(type);
        var custom = delegate instanceof net.minecraft.world.level.block.entity.BaseContainerBlockEntity block
                ? block.getCustomName() : null;
        this.title = custom == null ? type.defaultTitle() : io.papermc.paper.adventure.PaperAdventure.asAdventure(custom);
    }

    public net.kyori.adventure.text.Component title() { return title; }
    public String getTitle() { return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(title); }
    public InventoryType getType() { return type; }
    @Override public int getContainerSize() { return delegate.getContainerSize(); }
    @Override public boolean isEmpty() { return delegate.isEmpty(); }
    @Override public ItemStack getItem(int slot) { return delegate.getItem(slot); }
    @Override public ItemStack removeItem(int slot, int amount) { return delegate.removeItem(slot, amount); }
    @Override public ItemStack removeItemNoUpdate(int slot) { return delegate.removeItemNoUpdate(slot); }
    @Override public void setItem(int slot, ItemStack stack) { delegate.setItem(slot, stack); }
    @Override public int getMaxStackSize() { return maxStackSize == null ? delegate.getMaxStackSize() : maxStackSize; }
    @Override public void setChanged() { delegate.setChanged(); }
    @Override public boolean stillValid(net.minecraft.world.entity.player.Player player) { return delegate.stillValid(player); }
    @Override public void clearContent() { delegate.clearContent(); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return delegate.canPlaceItem(slot, stack); }
    @Override public void startOpen(net.minecraft.world.entity.player.Player player) { delegate.startOpen(player); }
    @Override public void stopOpen(net.minecraft.world.entity.player.Player player) { delegate.stopOpen(player); }
    public java.util.List<ItemStack> getContents() {
        return new java.util.AbstractList<>() {
            @Override public ItemStack get(int index) { return delegate.getItem(index); }
            @Override public int size() { return delegate.getContainerSize(); }
            @Override public ItemStack set(int index, ItemStack item) {
                ItemStack previous = delegate.getItem(index);
                delegate.setItem(index, item);
                return previous;
            }
        };
    }
    public void onOpen(CraftHumanEntity player) { if (!viewers.contains(player)) viewers.add((HumanEntity) player); }
    public void onClose(CraftHumanEntity player) { viewers.remove(player); }
    public java.util.List<HumanEntity> getViewers() { return viewers; }
    public InventoryHolder getOwner() { return owner; }
    public void setMaxStackSize(int size) {
        if (size <= 0) throw new IllegalArgumentException("Stack size must be positive");
        maxStackSize = size;
        if (delegate instanceof io.lunararcdevs.lunararc.common.bridge.SimpleContainerBridge bridge) bridge.lunararc$setMaxStackSize(size);
    }
    public org.bukkit.Location getLocation() {
        if (delegate instanceof net.minecraft.world.level.block.entity.BlockEntity block && block.getLevel() != null) {
            return io.papermc.paper.util.MCUtil.toLocation(block.getLevel(), block.getBlockPos());
        }
        return null;
    }
}
