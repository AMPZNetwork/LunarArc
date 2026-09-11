package io.lunararcdevs.lunararc.common.mixin.core.inventory;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.jetbrains.annotations.Nullable;

@Mixin(CraftingMenu.class)
public abstract class CraftingMenuMixin {
    @Inject(method = "slotChangedCraftingGrid", at = @At("RETURN"), require = 0)
    private static void lunararc$prepareCraft(AbstractContainerMenu menu, Level level, Player player,
                                               CraftingContainer craftSlots, ResultContainer resultSlots,
                                               @Nullable RecipeHolder<CraftingRecipe> recipe, CallbackInfo ci) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        Object bukkit = ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) serverPlayer).lunararc$getBukkitEntity();
        if (!(bukkit instanceof org.bukkit.craftbukkit.entity.CraftPlayer craftPlayer)) return;
        var inventory = new org.bukkit.craftbukkit.inventory.CraftInventoryCrafting(
                craftSlots, resultSlots, craftPlayer, recipe);
        var view = new org.bukkit.craftbukkit.inventory.CraftInventoryView(
                craftPlayer, menu, inventory, craftPlayer.getInventory(),
                org.bukkit.event.inventory.InventoryType.WORKBENCH,
                net.kyori.adventure.text.Component.translatable("container.crafting"));
        boolean repair = recipe != null && recipe.value() instanceof net.minecraft.world.item.crafting.RepairItemRecipe;
        org.bukkit.Bukkit.getPluginManager().callEvent(
                new org.bukkit.event.inventory.PrepareItemCraftEvent(inventory, view, repair));
        net.minecraft.world.item.ItemStack result = resultSlots.getItem(0).copy();
        menu.setRemoteSlot(0, result.copy());
        if (serverPlayer.connection != null) {
            serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket(
                    menu.containerId, menu.incrementStateId(), 0, result));
        }
    }
}
