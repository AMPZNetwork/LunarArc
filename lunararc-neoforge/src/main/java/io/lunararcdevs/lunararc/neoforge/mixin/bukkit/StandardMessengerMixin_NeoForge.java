package io.lunararcdevs.lunararc.neoforge.mixin.bukkit;

import io.lunararcdevs.lunararc.neoforge.network.NeoForgePluginMessaging;
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
public abstract class StandardMessengerMixin_NeoForge {
    @Inject(method = "registerOutgoingPluginChannel", at = @At("RETURN"))
    private void lunararc$registerOutgoing(Plugin plugin, String channel, CallbackInfo ci) {
        NeoForgePluginMessaging.ensureChannel(channel);
    }

    @Inject(method = "registerIncomingPluginChannel", at = @At("RETURN"))
    private void lunararc$registerIncoming(Plugin plugin, String channel, PluginMessageListener listener,
                                           CallbackInfoReturnable<PluginMessageListenerRegistration> cir) {
        NeoForgePluginMessaging.ensureChannel(channel);
    }
}
