package io.ampznetwork.lunararc.common.mixin.core.server;

import io.ampznetwork.lunararc.common.LunarArcPlatform;
import io.ampznetwork.lunararc.common.config.LunarArcConfig;
import net.minecraft.server.MinecraftServer;
import org.bukkit.plugin.Plugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void lunararc$onInit(CallbackInfo ci) {
        System.out.println("[LunarArc] Initializing Hybrid Bridge...");
        LunarArcConfig.load();
        io.ampznetwork.lunararc.api.LunarArcServer.init();
    }

    @Inject(method = "runServer", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;initServer()Z"
    ))
    private void lunararc$beforeServerStart(CallbackInfo ci) {
        if (LunarArcPlatform.getServer() == null) return;
        loadPlugins();
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void lunararc$onStop(CallbackInfo ci) {
        if (LunarArcPlatform.getServer() == null) return;
        System.out.println("[LunarArc] Disabling all plugins...");
        var pm = LunarArcPlatform.getServer().getPluginManager();
        for (Plugin plugin : pm.getPlugins()) {
            try {
                pm.disablePlugin(plugin);
            } catch (Exception e) {
                System.err.println("[LunarArc] Error disabling plugin " + plugin.getName() + ": " + e.getMessage());
            }
        }
    }

    private void loadPlugins() {
        File pluginsDir = new File(LunarArcConfig.getPluginsFolder());
        if (!pluginsDir.exists()) {
            pluginsDir.mkdirs();
            System.out.println("[LunarArc] Created plugins directory: " + pluginsDir.getAbsolutePath());
        }

        var pm = LunarArcPlatform.getServer().getPluginManager();
        File[] jars = pluginsDir.listFiles(f -> f.isFile() && f.getName().endsWith(".jar"));

        if (jars == null || jars.length == 0) {
            System.out.println("[LunarArc] No plugins found in " + pluginsDir.getAbsolutePath());
            return;
        }

        System.out.println("[LunarArc] Loading " + jars.length + " plugin(s)...");
        for (File jar : jars) {
            try {
                Plugin plugin = pm.loadPlugin(jar);
                if (plugin != null) {
                    System.out.println("[LunarArc] Loaded plugin: " + plugin.getName() + " v" + plugin.getDescription().getVersion());
                }
            } catch (Exception e) {
                System.err.println("[LunarArc] Failed to load plugin " + jar.getName() + ": " + e.getMessage());
            }
        }

        // Enable all loaded plugins
        for (Plugin plugin : pm.getPlugins()) {
            if (!plugin.isEnabled()) {
                pm.enablePlugin(plugin);
                System.out.println("[LunarArc] Enabled plugin: " + plugin.getName());
            }
        }
    }
}
