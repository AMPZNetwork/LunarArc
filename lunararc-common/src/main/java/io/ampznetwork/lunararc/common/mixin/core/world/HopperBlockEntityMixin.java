package io.ampznetwork.lunararc.common.mixin.core.world;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.ampznetwork.lunararc.common.bridge.EntityBridge;
import io.ampznetwork.lunararc.common.bridge.HopperBlockEntityBridge;
import io.ampznetwork.lunararc.common.mod.server.LunarArcTickingTrackerImpl;
import io.ampznetwork.lunararc.common.mod.util.LunarArcInventories;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin implements HopperBlockEntityBridge {

    @Unique
    private static final ThreadLocal<java.util.IdentityHashMap<Container, Inventory>> lunararc$transferInventories = new ThreadLocal<>();

    @Unique
    private static final ThreadLocal<java.util.IdentityHashMap<Container, Inventory>> lunararc$reusableInventories = ThreadLocal.withInitial(() -> new java.util.IdentityHashMap<>(2));

    @WrapMethod(method = "ejectItems")
    private static boolean lunararc$cachePushInventories(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos,
            HopperBlockEntity hopper, Operation<Boolean> original) {
        if (InventoryMoveItemEvent.getHandlerList().getRegisteredListeners().length == 0) return original.call(level, pos, hopper);
        var previous = lunararc$transferInventories.get();
        var current = previous == null ? lunararc$reusableInventories.get() : new java.util.IdentityHashMap<Container, Inventory>(2);
        lunararc$transferInventories.set(current);
        try {
            return original.call(level, pos, hopper);
        } finally {
            current.clear();
            if (previous == null) lunararc$transferInventories.remove();
            else lunararc$transferInventories.set(previous);
        }
    }

    @WrapMethod(method = "suckInItems")
    private static boolean lunararc$cachePullInventories(net.minecraft.world.level.Level level,
            net.minecraft.world.level.block.entity.Hopper hopper, Operation<Boolean> original) {
        if (InventoryMoveItemEvent.getHandlerList().getRegisteredListeners().length == 0) return original.call(level, hopper);
        var previous = lunararc$transferInventories.get();
        var current = previous == null ? lunararc$reusableInventories.get() : new java.util.IdentityHashMap<Container, Inventory>(2);
        lunararc$transferInventories.set(current);
        try {
            return original.call(level, hopper);
        } finally {
            current.clear();
            if (previous == null) lunararc$transferInventories.remove();
            else lunararc$transferInventories.set(previous);
        }
    }

    @Unique
    private static Inventory lunararc$transferInventory(Container container) {
        var inventories = lunararc$transferInventories.get();
        if (inventories == null) return LunarArcInventories.ownerInventory(container);
        if (!inventories.containsKey(container)) inventories.put(container, LunarArcInventories.ownerInventory(container));
        return inventories.get(container);
    }

    // Private in 1.21.1, and the handler that needs it is static rather than being a hopper, so it
    // is shadowed here and reached through the bridge - the same route ServerPlayer's private
    // nextContainerCounter takes, and one that needs no access widener on any loader.
    @Shadow private void setCooldown(int cooldown) {}

    @Override
    public void lunararc$setCooldown(int cooldown) {
        this.setCooldown(cooldown);
    }

    @Inject(
            method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/entity/item/ItemEntity;)Z",
            at = @At("HEAD"),
            cancellable = true,
            require = 0)
    private static void lunararc$hopperPickupItem(Container container, ItemEntity itemEntity,
            CallbackInfoReturnable<Boolean> cir) {
        if (InventoryPickupItemEvent.getHandlerList().getRegisteredListeners().length == 0) return;
        Inventory inventory = LunarArcInventories.ownerInventory(container);
        if (inventory == null) return;
        if (!(((EntityBridge) itemEntity).lunararc$getBukkitEntity() instanceof org.bukkit.entity.Item item)) {
            return;
        }

        InventoryPickupItemEvent event = new InventoryPickupItemEvent(inventory, item);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) cir.setReturnValue(false);
    }

    @WrapOperation(
            method = "ejectItems",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;"),
            require = 0)
    private static ItemStack lunararc$hopperPush(Container source, Container destination, ItemStack stack,
            Direction direction, Operation<ItemStack> original) {
        ItemStack decided = lunararc$moveItem(source, destination, stack, true);
        if (decided == null) return stack;
        return original.call(source, destination, decided, direction);
    }

    @WrapOperation(
            method = "tryTakeInItemFromSlot",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;"),
            require = 0)
    private static ItemStack lunararc$hopperPull(Container source, Container destination, ItemStack stack,
            Direction direction, Operation<ItemStack> original) {
        ItemStack decided = lunararc$moveItem(source, destination, stack, false);
        if (decided == null) return stack;
        return original.call(source, destination, decided, direction);
    }

    /**
     * The stack to move on, or null if a plugin cancelled the transfer.
     *
     * <p>{@code sourceInitiated} distinguishes a hopper pushing into a container from a hopper
     * pulling out of one; the hopper is the source in the first case and the destination in the
     * second.</p>
     *
     * <p>The item goes to the event as a live mirror, not a copy, which is deliberate and is
     * Paper's choice at the same point. A plugin that adjusts the stack in place - the common
     * shape, {@code event.getItem().setAmount(n)} - is then adjusting the stack that actually
     * moves. Hand it a copy and that edit is silently dropped.</p>
     *
     * <p>What comes back is the caller's own object unless the plugin replaced it outright. That
     * matters: vanilla passes a stack it still holds a reference to, and {@code addItem} shrinks
     * the stack it is given as it transfers. Substituting a fresh copy for every transfer would
     * leave vanilla's own bookkeeping looking at an object that never shrank, which is how a
     * hopper ends up both keeping an item and delivering it. Paper guards this with a flag
     * recording whether setItem was called; the identity check here answers the same question
     * without needing Paper's own event subclass to ask it.</p>
     */
    @Unique
    private static ItemStack lunararc$moveItem(Container source, Container destination, ItemStack stack,
            boolean sourceInitiated) {
        if (InventoryMoveItemEvent.getHandlerList().getRegisteredListeners().length == 0) return stack;

        Inventory sourceInventory = lunararc$transferInventory(source);
        Inventory destinationInventory = lunararc$transferInventory(destination);
        if (sourceInventory == null || destinationInventory == null) return stack;

        org.bukkit.inventory.ItemStack mirror = CraftItemStack.asCraftMirror(stack);
        var event = new io.papermc.paper.event.inventory.PaperInventoryMoveItemEvent(
                sourceInventory, mirror, destinationInventory, sourceInitiated);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            lunararc$delayTickingHopper();
            return null;
        }
        return !event.calledSetItem || event.getItem() == mirror ? stack : CraftItemStack.asNMSCopy(event.getItem());
    }

    @Unique
    private static void lunararc$delayTickingHopper() {
        if (LunarArcTickingTrackerImpl.INSTANCE.getTickingSource() instanceof HopperBlockEntity hopper) {
            // 8 ticks: vanilla's own transfer cooldown, and Spigot's default hopper-transfer.
            ((HopperBlockEntityBridge) hopper).lunararc$setCooldown(8);
        }
    }
}
