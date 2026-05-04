package io.ampznetwork.lunararc.neoforge.mixin.plugin;

import org.bukkit.Server;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.SimplePluginManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SimplePluginManager.class, remap = false)
public class SimplePluginManagerMixin {
    
    @Shadow public org.bukkit.plugin.PluginManager paperPluginManager;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(Server server, SimpleCommandMap commandMap, CallbackInfo ci) {
        try {
            Class<?> ppmClass = Class.forName("io.papermc.paper.plugin.manager.PaperPluginManagerImpl");
            java.lang.reflect.Constructor<?> ppmCtor = ppmClass.getDeclaredConstructor(Server.class, org.bukkit.command.CommandMap.class, org.bukkit.plugin.PluginManager.class);
            ppmCtor.setAccessible(true);
            this.paperPluginManager = (org.bukkit.plugin.PluginManager) ppmCtor.newInstance(server, commandMap, (SimplePluginManager)(Object)this);
            System.out.println("[LunarArc] Paper 1.21.1 Master Bridge established via Mixin.");
        } catch (Throwable t) {
            // Silently fail if Paper implementation is not present (e.g. on other loaders if this mixin were active)
        }
    }
}
