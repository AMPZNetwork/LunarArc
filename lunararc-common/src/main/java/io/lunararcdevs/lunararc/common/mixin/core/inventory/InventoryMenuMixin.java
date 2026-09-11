package io.lunararcdevs.lunararc.common.mixin.core.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin implements io.lunararcdevs.lunararc.common.bridge.access.InventoryMenuAccessBridge {
    @Shadow @Final private Player owner;
    @Shadow @Final private net.minecraft.world.inventory.CraftingContainer craftSlots;
    @Shadow @Final private net.minecraft.world.inventory.ResultContainer resultSlots;

    @Override public Player lunararc$getOwner() { return this.owner; }
    @Override public net.minecraft.world.inventory.CraftingContainer lunararc$getCraftSlots() { return this.craftSlots; }
    @Override public net.minecraft.world.inventory.ResultContainer lunararc$getResultSlots() { return this.resultSlots; }
    @Inject(method = "slotsChanged", at = @At("RETURN"), require = 0)
    private void lunararc$preparePlayerCraft(Container ignored, CallbackInfo ci) {
        InventoryMenu menu = (InventoryMenu) (Object) this;
        io.lunararcdevs.lunararc.common.bridge.access.InventoryMenuAccessBridge access = (io.lunararcdevs.lunararc.common.bridge.access.InventoryMenuAccessBridge) (Object) this;
        if (!(access.lunararc$getOwner() instanceof ServerPlayer serverPlayer) || !menu.active) return;
        var recipe = serverPlayer.serverLevel().getRecipeManager().getRecipeFor(
                net.minecraft.world.item.crafting.RecipeType.CRAFTING,
                access.lunararc$getCraftSlots().asCraftInput(), serverPlayer.serverLevel()).orElse(null);
        Object bukkit = ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) serverPlayer).lunararc$getBukkitEntity();
        if (!(bukkit instanceof org.bukkit.craftbukkit.entity.CraftPlayer craftPlayer)) return;
        var inventory = new org.bukkit.craftbukkit.inventory.CraftInventoryCrafting(
                access.lunararc$getCraftSlots(), access.lunararc$getResultSlots(), craftPlayer, recipe);
        var view = new org.bukkit.craftbukkit.inventory.CraftInventoryView(
                craftPlayer, menu, inventory, craftPlayer.getInventory(),
                org.bukkit.event.inventory.InventoryType.CRAFTING,
                net.kyori.adventure.text.Component.translatable("container.crafting"));
        boolean repair = recipe != null && recipe.value() instanceof net.minecraft.world.item.crafting.RepairItemRecipe;
        org.bukkit.Bukkit.getPluginManager().callEvent(
                new org.bukkit.event.inventory.PrepareItemCraftEvent(inventory, view, repair));
        net.minecraft.world.item.ItemStack result = access.lunararc$getResultSlots().getItem(0).copy();
        menu.setRemoteSlot(0, result.copy());
        if (serverPlayer.connection != null) {
            serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket(
                    menu.containerId, menu.incrementStateId(), 0, result));
        }
    }
}
