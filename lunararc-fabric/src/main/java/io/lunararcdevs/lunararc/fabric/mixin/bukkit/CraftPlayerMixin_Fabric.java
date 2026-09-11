package io.lunararcdevs.lunararc.fabric.mixin.bukkit;

import io.lunararcdevs.lunararc.fabric.network.FabricPluginMessaging;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.plugin.Plugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CraftPlayer.class, remap = false)
public abstract class CraftPlayerMixin_Fabric {
    @Inject(method = "sendPluginMessage", at = @At("HEAD"), cancellable = true)
    private void lunararc$sendNativePluginMessage(Plugin plugin, String channel, byte[] message, CallbackInfo ci) {
        if (FabricPluginMessaging.sendIfManaged((CraftPlayer) (Object) this, plugin, channel, message)) {
            ci.cancel();
        }
    }
}
