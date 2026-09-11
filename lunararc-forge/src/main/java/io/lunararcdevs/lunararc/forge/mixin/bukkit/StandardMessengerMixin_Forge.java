package io.lunararcdevs.lunararc.forge.mixin.bukkit;

import io.lunararcdevs.lunararc.forge.network.ForgePluginMessaging;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.plugin.messaging.PluginMessageListenerRegistration;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = StandardMessenger.class, remap = false)
public abstract class StandardMessengerMixin_Forge {
    @Inject(method = "registerOutgoingPluginChannel", at = @At("RETURN"))
    private void lunararc$registerOutgoing(Plugin plugin, String channel, CallbackInfo ci) {
        ForgePluginMessaging.ensureChannel(channel);
    }

    @Inject(method = "registerIncomingPluginChannel", at = @At("RETURN"))
    private void lunararc$registerIncoming(Plugin plugin, String channel, PluginMessageListener listener,
                                           CallbackInfoReturnable<PluginMessageListenerRegistration> cir) {
        ForgePluginMessaging.ensureChannel(channel);
    }
}
