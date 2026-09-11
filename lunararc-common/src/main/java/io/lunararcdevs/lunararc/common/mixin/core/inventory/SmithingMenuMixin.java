package io.lunararcdevs.lunararc.common.mixin.core.inventory;

import io.lunararcdevs.lunararc.common.bridge.AbstractContainerMenuBridge;
import net.minecraft.world.inventory.SmithingMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmithingMenu.class)
public abstract class SmithingMenuMixin {
    @Inject(method = "createResult", at = @At("RETURN"), require = 0)
    private void lunararc$prepareSmithing(CallbackInfo ci) {
        SmithingMenu menu = (SmithingMenu) (Object) this;
        var owner = ((AbstractContainerMenuBridge) menu).lunararc$getOwner();
        if (owner == null) return;
        Object bukkit = ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) owner).lunararc$getBukkitEntity();
        if (!(bukkit instanceof org.bukkit.craftbukkit.entity.CraftPlayer player)) return;
        var top = new org.bukkit.craftbukkit.inventory.CraftSmithingInventory(menu, player);
        var view = new org.bukkit.craftbukkit.inventory.CraftInventoryView(
                player, menu, top, player.getInventory(), org.bukkit.event.inventory.InventoryType.SMITHING,
                net.kyori.adventure.text.Component.translatable("container.upgrade"));
        var event = new org.bukkit.event.inventory.PrepareSmithingEvent(view, top.getResult());
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        top.setResult(event.getResult());
        menu.broadcastChanges();
    }
}
