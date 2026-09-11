package io.lunararcdevs.lunararc.common.mixin.core.inventory;

import io.lunararcdevs.lunararc.common.bridge.AbstractContainerMenuBridge;
import net.minecraft.world.inventory.GrindstoneMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneMenuMixin {
    @Inject(method = "createResult", at = @At("RETURN"), require = 0)
    private void lunararc$prepareGrindstone(CallbackInfo ci) {
        GrindstoneMenu menu = (GrindstoneMenu) (Object) this;
        var owner = ((AbstractContainerMenuBridge) menu).lunararc$getOwner();
        if (owner == null) return;
        Object bukkit = ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) owner).lunararc$getBukkitEntity();
        if (!(bukkit instanceof org.bukkit.craftbukkit.entity.CraftPlayer player)) return;
        var top = new org.bukkit.craftbukkit.inventory.CraftGrindstoneInventory(menu, player);
        var view = new org.bukkit.craftbukkit.inventory.CraftInventoryView(
                player, menu, top, player.getInventory(), org.bukkit.event.inventory.InventoryType.GRINDSTONE,
                net.kyori.adventure.text.Component.translatable("container.grindstone_title"));
        var event = new org.bukkit.event.inventory.PrepareGrindstoneEvent(view, top.getResult());
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        top.setResult(event.getResult());
        menu.broadcastChanges();
    }
}
