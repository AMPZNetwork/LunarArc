package io.lunararcdevs.lunararc.common.mixin.core.entity;

import io.lunararcdevs.lunararc.common.LunarArcServerAccess;
import io.lunararcdevs.lunararc.common.bridge.EntityBridge;
import io.lunararcdevs.lunararc.common.bridge.ItemEntityBridge;
import net.kyori.adventure.util.TriState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin implements ItemEntityBridge {
    @Shadow public int pickupDelay;
    @Shadow public java.util.UUID target;
    @Shadow public abstract ItemStack getItem();
    @Unique private boolean lunararc$canMobPickup = true;
    @Unique private TriState lunararc$frictionState = TriState.NOT_SET;

    @Override
    public boolean lunararc$canMobPickup() {
        return lunararc$canMobPickup;
    }

    @Override
    public void lunararc$setCanMobPickup(boolean value) {
        lunararc$canMobPickup = value;
    }

    @Override
    public TriState lunararc$getFrictionState() {
        return lunararc$frictionState;
    }

    @Override
    public void lunararc$setFrictionState(TriState state) {
        lunararc$frictionState = java.util.Objects.requireNonNull(state, "state");
    }


    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true, require = 0)
    private void lunararc$pickupEvents(Player player, CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (self.level().isClientSide || this.pickupDelay != 0
                || (this.target != null && !this.target.equals(player.getUUID()))) {
            return;
        }

        ItemStack stack = this.getItem();
        if (stack.isEmpty()) {
            return;
        }
        int remaining = lunararc$remainingAfterPickup(player, stack);
        if (remaining >= stack.getCount()) {
            return; // Vanilla would not pick up anything either.
        }

        Object bukkitPlayerObject = ((EntityBridge) player).lunararc$getBukkitEntity();
        Object bukkitItemObject = ((EntityBridge) self).lunararc$getBukkitEntity();
        if (!(bukkitPlayerObject instanceof org.bukkit.entity.Player bukkitPlayer)
                || !(bukkitItemObject instanceof org.bukkit.entity.Item bukkitItem)) {
            return;
        }

        @SuppressWarnings("deprecation")
        org.bukkit.event.player.PlayerPickupItemEvent legacy =
                new org.bukkit.event.player.PlayerPickupItemEvent(bukkitPlayer, bukkitItem, remaining);
        LunarArcServerAccess.getCraftServer(((net.minecraft.server.level.ServerPlayer) player).server)
                .getPluginManager().callEvent(legacy);
        if (legacy.isCancelled()) {
            ci.cancel();
            return;
        }

        org.bukkit.event.entity.EntityPickupItemEvent event =
                new org.bukkit.event.entity.EntityPickupItemEvent(bukkitPlayer, bukkitItem, remaining);
        LunarArcServerAccess.getCraftServer(((net.minecraft.server.level.ServerPlayer) player).server)
                .getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Unique
    private static int lunararc$remainingAfterPickup(Player player, ItemStack stack) {
        int count = stack.getCount();
        if (player.hasInfiniteMaterials()) {
            return 0;
        }
        net.minecraft.world.entity.player.Inventory inventory = player.getInventory();
        if (stack.isDamaged()) {
            return inventory.getFreeSlot() >= 0 ? 0 : count;
        }

        int capacity = 0;
        for (ItemStack existing : inventory.items) {
            if (existing.isEmpty()) {
                capacity += stack.getMaxStackSize();
            } else if (ItemStack.isSameItemSameComponents(existing, stack) && existing.isStackable()) {
                capacity += Math.max(0, existing.getMaxStackSize() - existing.getCount());
            }
            if (capacity >= count) {
                return 0;
            }
        }
        ItemStack offhand = inventory.getItem(net.minecraft.world.entity.player.Inventory.SLOT_OFFHAND);
        if (!offhand.isEmpty() && ItemStack.isSameItemSameComponents(offhand, stack) && offhand.isStackable()) {
            capacity += Math.max(0, offhand.getMaxStackSize() - offhand.getCount());
        }
        return Math.max(0, count - capacity);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"), require = 0)
    private void lunararc$savePaperItemState(CompoundTag tag, CallbackInfo ci) {
        if (!lunararc$canMobPickup) tag.putBoolean("Paper.CanMobPickup", false);
        if (lunararc$frictionState != TriState.NOT_SET) {
            tag.putString("Paper.FrictionState", lunararc$frictionState.toString());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"), require = 0)
    private void lunararc$loadPaperItemState(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("Paper.CanMobPickup")) lunararc$canMobPickup = tag.getBoolean("Paper.CanMobPickup");
        if (tag.contains("Paper.FrictionState")) {
            try {
                lunararc$frictionState = TriState.valueOf(tag.getString("Paper.FrictionState"));
            } catch (IllegalArgumentException ignored) {
                lunararc$frictionState = TriState.NOT_SET;
            }
        }
    }
}
