package io.ampznetwork.lunararc.neoforge.mixin.permission;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.handler.DefaultPermissionHandler;
import net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContext;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DefaultPermissionHandler.class, remap = false)
public class PermissionAPIMixin {
    
    @Inject(method = "getPermission", at = @At("HEAD"), cancellable = true)
    private <T> void onGetPermission(ServerPlayer player, PermissionNode<T> node, PermissionDynamicContext<?>[] context, CallbackInfoReturnable<T> cir) {
        try {
            if (node.getType() == net.neoforged.neoforge.server.permission.nodes.PermissionTypes.BOOLEAN) {
                org.bukkit.entity.Player bukkitPlayer = org.bukkit.Bukkit.getPlayer(player.getUUID());
                if (bukkitPlayer != null && bukkitPlayer.isPermissionSet(node.getNodeName())) {
                    cir.setReturnValue((T) (Boolean) bukkitPlayer.hasPermission(node.getNodeName()));
                }
            }
        } catch (Throwable t) {
            // Ignore if Bukkit API is not fully loaded or accessible
        }
    }
}
