package io.ampznetwork.lunararc.common.server;

import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public class LunarArcPluginLoader implements PluginLoader {
    private final Server server;
    private final Yaml yaml = new Yaml();
    private final Pattern[] fileFilters = new Pattern[]{Pattern.compile("\\.jar$")};

    public LunarArcPluginLoader(Server server) {
        this.server = server;
    }

    @Override
    public Plugin loadPlugin(File file) throws InvalidPluginException, UnknownDependencyException {
        if (!file.exists()) {
            throw new InvalidPluginException(new java.io.FileNotFoundException(file.getPath()));
        }

        PluginDescriptionFile description;
        try (JarFile jar = new JarFile(file)) {
            JarEntry entry = jar.getJarEntry("plugin.yml");
            if (entry == null) {
                throw new InvalidPluginException("Jar does not contain plugin.yml");
            }
            try (InputStream stream = jar.getInputStream(entry)) {
                description = new PluginDescriptionFile(stream);
            }
        } catch (Exception e) {
            throw new InvalidPluginException(e);
        }

        File dataFolder = new File(file.getParentFile(), description.getName());
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        try {
            LunarArcPluginClassLoader loader = new LunarArcPluginClassLoader(this, getClass().getClassLoader(), description, dataFolder, file);
            Class<?> mainClass = loader.findClass(description.getMain(), false);
            Class<? extends JavaPlugin> pluginClass = mainClass.asSubclass(JavaPlugin.class);
            
            // In Bukkit/Paper, JavaPlugin has a protected constructor that takes no args.
            // But it needs to be initialized.
            JavaPlugin plugin = pluginClass.getDeclaredConstructor().newInstance();
            
            // We need to call init() on the plugin.
            // Since init() is protected, we use reflection.
            // Paper 1.21.1 has a few variants.
            java.lang.reflect.Method initMethod = null;
            Object[] initArgs = null;
            
            try {
                // Try modern Paper 1.21.1 signature (with PluginMeta and Logger)
                // Note: PluginDescriptionFile implements PluginMeta
                initMethod = JavaPlugin.class.getDeclaredMethod("init", Server.class, PluginDescriptionFile.class, File.class, File.class, ClassLoader.class, io.papermc.paper.plugin.configuration.PluginMeta.class, java.util.logging.Logger.class);
                initArgs = new Object[]{server, description, dataFolder, file, loader, description, java.util.logging.Logger.getLogger(description.getName())};
            } catch (NoSuchMethodException e) {
                // Fallback to older/alternate signature
                initMethod = JavaPlugin.class.getDeclaredMethod("init", PluginLoader.class, Server.class, PluginDescriptionFile.class, File.class, File.class, ClassLoader.class);
                initArgs = new Object[]{this, server, description, dataFolder, file, loader};
            }

            initMethod.setAccessible(true);
            initMethod.invoke(plugin, initArgs);
            
            loader.initialize(plugin);
            return plugin;
        } catch (Throwable e) {
            throw new InvalidPluginException(e);
        }
    }

    @Override
    public PluginDescriptionFile getPluginDescription(File file) throws InvalidDescriptionException {
        try (JarFile jar = new JarFile(file)) {
            JarEntry entry = jar.getJarEntry("plugin.yml");
            if (entry == null) {
                throw new InvalidDescriptionException("Jar does not contain plugin.yml");
            }
            try (InputStream stream = jar.getInputStream(entry)) {
                return new PluginDescriptionFile(stream);
            }
        } catch (Exception e) {
            throw new InvalidDescriptionException(e);
        }
    }

    @Override
    public Pattern[] getPluginFileFilters() {
        return fileFilters;
    }

    @Override
    public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(Listener listener, Plugin plugin) {
        // This is handled by SimplePluginManager in Paper, but we might need to implement it for some cases.
        return new java.util.HashMap<>();
    }

    @Override
    public void enablePlugin(Plugin plugin) {
        if (!plugin.isEnabled()) {
            try {
                // JavaPlugin.setEnabled is protected, so we use reflection if needed, 
                // but Plugin.setEnabled is actually what we want if we have the instance.
                // However, Plugin interface doesn't have setEnabled. JavaPlugin does.
                if (plugin instanceof org.bukkit.plugin.java.JavaPlugin jp) {
                    var method = org.bukkit.plugin.java.JavaPlugin.class.getDeclaredMethod("setEnabled", boolean.class);
                    method.setAccessible(true);
                    method.invoke(jp, true);
                }
            } catch (Exception e) {
                server.getLogger().severe("Error enabling plugin " + plugin.getName() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void disablePlugin(Plugin plugin) {
        if (plugin.isEnabled()) {
            try {
                if (plugin instanceof org.bukkit.plugin.java.JavaPlugin jp) {
                    var method = org.bukkit.plugin.java.JavaPlugin.class.getDeclaredMethod("setEnabled", boolean.class);
                    method.setAccessible(true);
                    method.invoke(jp, false);
                }
            } catch (Exception e) {
                server.getLogger().severe("Error disabling plugin " + plugin.getName() + ": " + e.getMessage());
            }
        }
    }
}
