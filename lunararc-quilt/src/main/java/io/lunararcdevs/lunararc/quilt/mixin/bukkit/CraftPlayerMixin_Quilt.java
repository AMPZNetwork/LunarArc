package io.lunararcdevs.lunararc.quilt.mixin.bukkit;

import io.lunararcdevs.lunararc.quilt.network.QuiltPluginMessaging;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.plugin.Plugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CraftPlayer.class, remap = false)
public abstract class CraftPlayerMixin_Quilt {
    @Inject(method = "sendPluginMessage", at = @At("HEAD"), cancellable = true)
    private void lunararc$sendNativePluginMessage(Plugin plugin, String channel, byte[] message, CallbackInfo ci) {
        if (QuiltPluginMessaging.sendIfManaged((CraftPlayer) (Object) this, plugin, channel, message)) {
            ci.cancel();
        }
    }
}
