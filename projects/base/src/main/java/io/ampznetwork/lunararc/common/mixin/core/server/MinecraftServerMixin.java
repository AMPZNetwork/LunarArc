package io.ampznetwork.lunararc.common.mixin.core.server;

import io.ampznetwork.lunararc.common.LunarArcPlatform;
import io.ampznetwork.lunararc.common.config.LunarArcConfig;
import io.ampznetwork.lunararc.common.bridge.MinecraftServerBridge;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.server.players.PlayerList;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements MinecraftServerBridge {

    @Unique private final Queue<Runnable> lunararc$taskQueue = new ConcurrentLinkedQueue<>();

    @Override
    public void lunararc$queueTask(Runnable runnable) {
        this.lunararc$taskQueue.add(runnable);
    }

    @Inject(method = "tickChildren", at = @At("TAIL"))
    private void lunararc$processTasks(CallbackInfo ci) {
        Runnable task;
        while ((task = this.lunararc$taskQueue.poll()) != null) {
            try {
                task.run();
            } catch (Exception e) {
                System.err.println("[LunarArc] Error executing queued task: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void lunararc$onInit(CallbackInfo ci) {
        System.out.println("[LunarArc] Initializing Hybrid Bridge...");
        LunarArcConfig.load();
        io.ampznetwork.lunararc.api.LunarArcServer.init();
    }


    @Inject(method = "runServer", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;initServer()Z",
            shift = At.Shift.AFTER
    ))
    private void lunararc$afterServerInit(CallbackInfo ci) {
        System.out.println("[LunarArc] Creating CraftServer bridge...");
        PlayerList playerList = ((MinecraftServerAccessor) this).getPlayerList();
        io.ampznetwork.lunararc.common.stubs.org.bukkit.craftbukkit.v1_21_R1.CraftServer craftServer = new io.ampznetwork.lunararc.common.stubs.org.bukkit.craftbukkit.v1_21_R1.CraftServer((MinecraftServer)(Object)this, playerList);
        LunarArcPlatform.setServer(craftServer);
        
        loadPlugins();
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void lunararc$onStop(CallbackInfo ci) {
        try {
            // Use reflection to avoid hard references that crash the Mixin transformer if Bukkit is missing
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Object server = bukkitClass.getMethod("getServer").invoke(null);
            if (server == null) return;

            Object pluginManager = server.getClass().getMethod("getPluginManager").invoke(server);
            Object[] plugins = (Object[]) pluginManager.getClass().getMethod("getPlugins").invoke(pluginManager);

            System.out.println("[LunarArc] Disabling " + plugins.length + " plugins...");
            java.lang.reflect.Method disableMethod = pluginManager.getClass().getMethod("disablePlugin", Class.forName("org.bukkit.plugin.Plugin"));

            for (Object plugin : plugins) {
                try {
                    disableMethod.invoke(pluginManager, plugin);
                } catch (Exception e) {
                    System.err.println("[LunarArc] Error disabling plugin: " + e.getMessage());
                }
            }
        } catch (Throwable ignored) {
            // Bukkit not present or not initialized yet
        }
    }

    private void loadPlugins() {
        try {
            System.out.println("[LunarArc] Working directory: " + new File(".").getAbsolutePath());
            File pluginsDir = new File(LunarArcConfig.getPluginsFolder());
            
            if (!pluginsDir.exists()) {
                pluginsDir.mkdirs();
            }

            // Use reflection for everything Bukkit-related
            Object craftServer = LunarArcPlatform.getServer();
            if (craftServer == null) return;

            Object pm = craftServer.getClass().getMethod("getPluginManager").invoke(craftServer);
            File[] jars = pluginsDir.listFiles(f -> f.isFile() && f.getName().endsWith(".jar"));

            if (jars == null || jars.length == 0) return;

            // Use the native Bukkit loadPlugins method which lets Paper handle dependency graphs
            java.lang.reflect.Method loadPluginsMethod = pm.getClass().getMethod("loadPlugins", File.class);
            java.lang.reflect.Method enableMethod = pm.getClass().getMethod("enablePlugin", Class.forName("org.bukkit.plugin.Plugin"));
            
            try {
                System.out.println("[LunarArc] Delegating plugin loading to Paper PluginManager...");
                loadPluginsMethod.invoke(pm, pluginsDir);
            } catch (Throwable e) {
                System.err.println("[LunarArc] Critical failure during plugin graph loading:");
                if (e instanceof java.lang.reflect.InvocationTargetException) {
                    ((java.lang.reflect.InvocationTargetException)e).getCause().printStackTrace();
                } else {
                    e.printStackTrace();
                }
            }

            // Get loaded plugins and enable them
            java.lang.reflect.Method getPluginsMethod = pm.getClass().getMethod("getPlugins");
            Object[] plugins = (Object[]) getPluginsMethod.invoke(pm);
            for (Object plugin : plugins) {
                try {
                    Boolean isEnabled = (Boolean) plugin.getClass().getMethod("isEnabled").invoke(plugin);
                    if (!isEnabled) {
                        enableMethod.invoke(pm, plugin);
                    }
                } catch (Exception e) {
                    System.err.println("[LunarArc] Error enabling " + plugin.toString());
                }
            }

            // Sync commands to Vanilla dispatcher
            System.out.println("[LunarArc] Synchronizing commands with Vanilla dispatcher...");
            com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher = ((net.minecraft.server.MinecraftServer)(Object)this).getCommands().getDispatcher();
            
            org.bukkit.Bukkit.getServer().getCommandMap().getKnownCommands().values().forEach(command -> {
                String name = command.getName();
                if (name.contains(":") || name.contains(" ")) return; // Skip invalid or namespaced for now
                
                try {
                    com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> node = 
                        com.mojang.brigadier.builder.LiteralArgumentBuilder.<net.minecraft.commands.CommandSourceStack>literal(name)
                        .executes(context -> {
                            org.bukkit.command.CommandSender sender = io.ampznetwork.lunararc.common.stubs.org.bukkit.craftbukkit.v1_21_R1.CraftConsoleCommandSender.fromSource(context.getSource());
                            String input = context.getInput();
                            if (input.startsWith("/")) input = input.substring(1);
                            org.bukkit.Bukkit.dispatchCommand(sender, input);
                            return 1;
                        });
                    
                    // Allow arguments
                    node.then(com.mojang.brigadier.builder.RequiredArgumentBuilder.<net.minecraft.commands.CommandSourceStack, String>argument("args", com.mojang.brigadier.arguments.StringArgumentType.greedyString())
                        .executes(context -> {
                            org.bukkit.command.CommandSender sender = io.ampznetwork.lunararc.common.stubs.org.bukkit.craftbukkit.v1_21_R1.CraftConsoleCommandSender.fromSource(context.getSource());
                            String input = context.getInput();
                            if (input.startsWith("/")) input = input.substring(1);
                            org.bukkit.Bukkit.dispatchCommand(sender, input);
                            return 1;
                        })
                    );
                    
                    dispatcher.register(node);
                    
                    for (String alias : command.getAliases()) {
                        try {
                            dispatcher.register(com.mojang.brigadier.builder.LiteralArgumentBuilder.<net.minecraft.commands.CommandSourceStack>literal(alias)
                                .executes(node.getCommand())
                                // Note: we can't easily copy the 'then' logic without more complex tree walking
                                // But for now, simple aliases will work for commands without args
                            );
                        } catch (Exception ignored) {}
                    }
                } catch (Exception e) {
                    System.err.println("[LunarArc] Could not register command: " + name);
                }
            });
        } catch (Throwable e) {
            System.err.println("[LunarArc] Critical error in plugin loading sequence: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
