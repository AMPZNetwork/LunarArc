package io.ampznetwork.lunararc.common.server;

import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Pattern[] fileFilters = new Pattern[] { Pattern.compile("\\.jar$") };
    private static final Logger logger = LoggerFactory.getLogger("LunarArc");

    public LunarArcPluginLoader(Server server) {
        this.server = server;
    }

    @Override
    public Plugin loadPlugin(File file) throws InvalidPluginException, UnknownDependencyException {
        logger.info("[LunarArc] Loading plugin jar: " + file.getName());
        if (!file.exists()) {
            throw new InvalidPluginException(new java.io.FileNotFoundException(file.getPath()));
        }

        PluginDescriptionFile description = null;
        try (JarFile jar = new JarFile(file)) {
            JarEntry entry = jar.getJarEntry("plugin.yml");
            if (entry == null) {
                entry = jar.getJarEntry("paper-plugin.yml");
            }

            if (entry == null) {
                throw new InvalidPluginException("Jar does not contain plugin.yml or paper-plugin.yml");
            }

            try (InputStream stream = jar.getInputStream(entry)) {
                description = new PluginDescriptionFile(stream);
                
                // Blacklist check
                io.ampznetwork.lunararc.common.config.PluginBlacklist.BlacklistEntry blacklist = io.ampznetwork.lunararc.common.config.PluginBlacklist.check(description.getName(), description.getVersion());
                if (blacklist != null) {
                    logger.error("****************************************************************");
                    logger.error("INCOMPATIBLE PLUGIN DETECTED: {}", description.getName());
                    logger.error("VERSION: {}", description.getVersion());
                    logger.error("REASON: {}", blacklist.reason);
                    logger.error("****************************************************************");
                    logger.error("LunarArc cannot start while this plugin is present.");
                    logger.error("Please remove or update the plugin to resolve this issue.");
                    System.exit(1);
                    return null;
                }

                logger.info("[LunarArc] Found description for " + description.getName() + " version "
                        + description.getVersion());
            }
        } catch (Exception e) {
            logger.error("[LunarArc] Error reading plugin description from " + file.getName(), e);
            throw new InvalidPluginException(e);
        }

        File dataFolder = new File(file.getParentFile(), description.getName());
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        try {
            logger.info("[LunarArc] Creating ClassLoader for " + description.getName());
            org.bukkit.plugin.java.PluginClassLoader loader = new org.bukkit.plugin.java.PluginClassLoader(this, getClass().getClassLoader(),
                    description, dataFolder, file);
            logger.info("[LunarArc] Finding main class: " + description.getMain());
            Class<?> mainClass = loader.findClass(description.getMain(), false);
            Class<? extends JavaPlugin> pluginClass = mainClass.asSubclass(JavaPlugin.class);

            JavaPlugin plugin;
            try {
                java.lang.reflect.Constructor<? extends JavaPlugin> constructor = pluginClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                
                // Debug check: Is the classloader compatible?
                if (loader != null && !(loader instanceof org.bukkit.plugin.java.PluginClassLoader)) {
                    logger.warn("[LunarArc] Loader for {} is NOT an instance of PluginClassLoader! This will cause instanceof checks to fail.", description.getName());
                }
                
                plugin = constructor.newInstance();
            } catch (Throwable t) {
                logger.warn("[LunarArc] Failed to instantiate {} normally: {}. Falling back to Unsafe.", description.getName(), t.toString());
                if (t.getCause() != null) {
                    logger.warn("[LunarArc]  - Caused by: {}", t.getCause().toString());
                }
                try {
                    java.lang.reflect.Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
                    unsafeField.setAccessible(true);
                    sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
                    plugin = (JavaPlugin) unsafe.allocateInstance(pluginClass);
                } catch (Exception e) {
                    throw new InvalidPluginException("Could not instantiate plugin " + description.getFullName(), e);
                }
            }

            // We need to call init() on the plugin.
            // Since init() is protected, we use reflection.
            java.lang.reflect.Method initMethod = null;
            Object[] initArgs = null;

            io.papermc.paper.plugin.configuration.PluginMeta meta = new LunarArcPluginMeta(description);

            try {
                // Try the 7-parameter signature from our stub
                initMethod = JavaPlugin.class.getDeclaredMethod("init",
                        Server.class, PluginDescriptionFile.class, File.class, File.class, ClassLoader.class,
                        io.papermc.paper.plugin.configuration.PluginMeta.class, java.util.logging.Logger.class);

                initArgs = new Object[] {
                        server, description, dataFolder, file, loader, meta,
                        LunarArcLogger.getLogger(description.getName())
                };
            } catch (NoSuchMethodException e) {
                // Fallback search
                for (java.lang.reflect.Method method : JavaPlugin.class.getDeclaredMethods()) {
                    if (method.getName().equals("init")) {
                        Class<?>[] params = method.getParameterTypes();
                        if (params.length == 7) {
                            initMethod = method;
                            initArgs = new Object[] { server, description, dataFolder, file, loader, meta,
                                    LunarArcLogger.getLogger(description.getName()) };
                            break;
                        }
                    }
                }
            }

            if (initMethod == null) {
                throw new IllegalStateException("Could not find suitable JavaPlugin.init() method!");
            }

            initMethod.setAccessible(true);
            initMethod.invoke(plugin, initArgs);

            // Reflection injection for plugin fields
            Class<?> current = plugin.getClass();
            while (current != null && current != Object.class) {
                try {
                    for (String fieldName : new String[] { "server", "description", "dataFolder", "file", "classLoader",
                            "pluginMeta", "logger" }) {
                        try {
                            java.lang.reflect.Field field = current.getDeclaredField(fieldName);
                            field.setAccessible(true);
                            Object val = null;
                            if (fieldName.equals("server"))
                                val = server;
                            else if (fieldName.equals("description"))
                                val = description;
                            else if (fieldName.equals("dataFolder"))
                                val = dataFolder;
                            else if (fieldName.equals("file"))
                                val = file;
                            else if (fieldName.equals("classLoader"))
                                val = loader;
                            else if (fieldName.equals("pluginMeta"))
                                val = meta;
                            else if (fieldName.equals("logger"))
                                val = LunarArcLogger.getLogger(description.getName());

                            if (val != null) {
                                try {
                                    field.set(plugin, val);
                                } catch (Exception ignored) {
                                }
                            }
                        } catch (NoSuchFieldException ignored) {
                        }
                    }
                } catch (Exception e) {
                    logger.warn("[LunarArc] Failed aggressive field injection for " + description.getName() + " on "
                            + current.getName(), e);
                }
                if (current == JavaPlugin.class)
                    break;
                current = current.getSuperclass();
            }

            try {
                java.lang.reflect.Field loaderField = JavaPlugin.class.getDeclaredField("loader");
                loaderField.setAccessible(true);
                loaderField.set(plugin, this);
            } catch (NoSuchFieldException ignored) {
            }

            // Register commands from plugin.yml to the CommandMap
            try {
                java.lang.reflect.Constructor<org.bukkit.command.PluginCommand> commandConstructor = org.bukkit.command.PluginCommand.class
                        .getDeclaredConstructor(String.class, org.bukkit.plugin.Plugin.class);
                commandConstructor.setAccessible(true);

                Map<String, Map<String, Object>> commands = description.getCommands();
                if (commands != null) {
                    for (Map.Entry<String, Map<String, Object>> entry : commands.entrySet()) {
                        org.bukkit.command.PluginCommand command = commandConstructor.newInstance(entry.getKey(),
                                plugin);

                        Map<String, Object> commandMap = entry.getValue();
                        if (commandMap != null) {
                            if (commandMap.containsKey("description")) {
                                command.setDescription(commandMap.get("description").toString());
                            }
                            if (commandMap.containsKey("usage")) {
                                command.setUsage(commandMap.get("usage").toString());
                            }
                            if (commandMap.containsKey("aliases")) {
                                Object aliases = commandMap.get("aliases");
                                if (aliases instanceof java.util.List) {
                                    command.setAliases((java.util.List<String>) aliases);
                                } else if (aliases instanceof String) {
                                    command.setAliases(java.util.Arrays.asList(aliases.toString()));
                                }
                            }
                            if (commandMap.containsKey("permission")) {
                                command.setPermission(commandMap.get("permission").toString());
                            }
                            if (commandMap.containsKey("permission-message")) {
                                command.setPermissionMessage(commandMap.get("permission-message").toString());
                            }
                        }
                        server.getCommandMap().register(description.getName(), command);
                    }
                }
            } catch (Exception ex) {
                server.getLogger()
                        .severe("Failed to register commands for " + description.getName() + ": " + ex.getMessage());
            }

            // loader.initialize(plugin) is now handled by JavaPlugin constructor
            return plugin;
        } catch (Throwable e) {
            Throwable cause = e;
            if (e instanceof java.lang.reflect.InvocationTargetException ite) {
                cause = ite.getCause();
            }
            server.getLogger().log(java.util.logging.Level.SEVERE,
                    "Failed to load plugin " + file.getName() + ": " + cause.getMessage(), cause);
            throw new InvalidPluginException(cause);
        }
    }

    @Override
    public PluginDescriptionFile getPluginDescription(File file) throws InvalidDescriptionException {
        try (JarFile jar = new JarFile(file)) {
            JarEntry entry = jar.getJarEntry("plugin.yml");
            boolean isPaper = false;
            if (entry == null) {
                entry = jar.getJarEntry("paper-plugin.yml");
                isPaper = true;
            }

            if (entry == null) {
                throw new InvalidDescriptionException("Jar does not contain plugin.yml or paper-plugin.yml");
            }

            try (InputStream stream = jar.getInputStream(entry)) {
                if (!isPaper) {
                    return new PluginDescriptionFile(stream);
                } else {
                    // Manual parsing for paper-plugin.yml to bridge to PluginDescriptionFile
                    Map<String, Object> map = yaml.load(stream);
                    String name = map.getOrDefault("name", "").toString();
                    String version = map.getOrDefault("version", "").toString();
                    String main = map.getOrDefault("main-class", map.getOrDefault("main", "")).toString();
                    
                    if (name.isEmpty() || main.isEmpty()) {
                        throw new InvalidDescriptionException("paper-plugin.yml is missing 'name' or 'main-class'");
                    }

                    // Create a compatible map for PluginDescriptionFile
                    Map<String, Object> compatMap = new java.util.HashMap<>(map);
                    compatMap.put("main", main);
                    
                    // Filter out Paper-specific keys that might confuse the legacy constructor
                    // but usually, it's fine.
                    
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    yaml.dump(compatMap, new java.io.OutputStreamWriter(baos));
                    return new PluginDescriptionFile(new java.io.ByteArrayInputStream(baos.toByteArray()));
                }
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
    public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(Listener listener,
            Plugin plugin) {
        Map<Class<? extends Event>, Set<RegisteredListener>> ret = new java.util.HashMap<>();
        java.util.Set<java.lang.reflect.Method> methods;
        try {
            java.lang.reflect.Method[] publicMethods = listener.getClass().getMethods();
            java.lang.reflect.Method[] privateMethods = listener.getClass().getDeclaredMethods();
            methods = new java.util.HashSet<>(publicMethods.length + privateMethods.length, 1.0f);
            for (java.lang.reflect.Method method : publicMethods) {
                methods.add(method);
            }
            for (java.lang.reflect.Method method : privateMethods) {
                methods.add(method);
            }
        } catch (NoClassDefFoundError e) {
            plugin.getLogger()
                    .severe("Plugin " + plugin.getDescription().getFullName() + " has failed to register events for "
                            + listener.getClass() + " because " + e.getMessage() + " does not exist.");
            return ret;
        }

        for (java.lang.reflect.Method method : methods) {
            org.bukkit.event.EventHandler eh = method.getAnnotation(org.bukkit.event.EventHandler.class);
            if (eh == null)
                continue;
            if (method.isBridge() || method.isSynthetic())
                continue;

            final Class<?> checkClass;
            if (method.getParameterTypes().length != 1
                    || !Event.class.isAssignableFrom(checkClass = method.getParameterTypes()[0])) {
                plugin.getLogger()
                        .severe(plugin.getDescription().getFullName()
                                + " attempted to register an invalid EventHandler method signature \""
                                + method.toGenericString() + "\" in " + listener.getClass());
                continue;
            }

            final Class<? extends Event> eventClass = checkClass.asSubclass(Event.class);
            method.setAccessible(true);
            Set<RegisteredListener> eventSet = ret.computeIfAbsent(eventClass, k -> new java.util.HashSet<>());

            EventExecutor executor = (ignored, event) -> {
                try {
                    if (!eventClass.isAssignableFrom(event.getClass()))
                        return;
                    method.invoke(listener, event);
                } catch (Exception ex) {
                    throw new EventException(ex);
                }
            };

            eventSet.add(new RegisteredListener(listener, executor, eh.priority(), plugin, eh.ignoreCancelled()));
        }
        return ret;
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
                Throwable cause = e;
                if (e instanceof java.lang.reflect.InvocationTargetException ite) {
                    cause = ite.getCause();
                }
                server.getLogger().log(java.util.logging.Level.SEVERE, "Error enabling plugin " + plugin.getName(), cause);
                if (cause instanceof RuntimeException re) throw re;
                throw new RuntimeException(cause);
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
