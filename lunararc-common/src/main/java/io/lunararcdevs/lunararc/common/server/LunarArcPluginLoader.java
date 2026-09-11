package io.lunararcdevs.lunararc.common.server;

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
    private final LunarArcPluginClassSpace classSpace = new LunarArcPluginClassSpace();

    public LunarArcPluginLoader(Server server) {
        this.server = server;
    }

    public Server getServerInstance() {
        return server;
    }

    public LunarArcPluginClassSpace getClassSpace() {
        return classSpace;
    }

    @Override
    public Plugin loadPlugin(File file) throws InvalidPluginException, UnknownDependencyException {
        return loadPlugin(LunarArcPluginProvider.discover(this, file));
    }

    /** Load a provider already discovered and dependency-sorted by the plugin manager. */
    public Plugin loadPlugin(LunarArcPluginProvider provider) throws InvalidPluginException, UnknownDependencyException {
        java.util.Objects.requireNonNull(provider, "provider");
        File file = provider.source();
        PluginDescriptionFile description = provider.description();
        try {
            JavaPlugin plugin = provider.create();

            if (!provider.paperPlugin()) {
                try {
                    java.util.List<org.bukkit.command.Command> commands =
                            org.bukkit.command.PluginCommandYamlParser.parse(plugin);
                    if (!commands.isEmpty()) {
                        server.getCommandMap().registerAll(description.getName(), commands);
                    }
                } catch (Throwable ex) {
                    throw new InvalidPluginException("Failed to register commands for " + description.getName(), ex);
                }
            }

            provider.commit();
            return plugin;
        } catch (Throwable e) {
            try { cleanupFailedPlugin(provider.plugin()); } catch (Throwable cleanupError) { e.addSuppressed(cleanupError); }
            try { provider.close(); } catch (java.io.IOException closeError) { e.addSuppressed(closeError); }
            Throwable cause = e;
            if (e instanceof java.lang.reflect.InvocationTargetException ite && ite.getCause() != null) {
                cause = ite.getCause();
            }
            if (cause instanceof UnsupportedClassVersionError versionError) {
                String message = friendlyJavaVersionMessage(file.getName(), versionError);
                throw new InvalidPluginException(message, versionError);
            }
            if (cause instanceof InvalidPluginException invalid) throw invalid;
            throw new InvalidPluginException(cause);
        }
    }

    public static String friendlyJavaVersionMessage(String fileName, UnsupportedClassVersionError error) {
        int runtimeFeature = Runtime.version().feature();
        int requiredFeature = -1;
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("class file version ([0-9]+)(?:\\.[0-9]+)?")
                .matcher(String.valueOf(error.getMessage()));
        if (matcher.find()) {
            try {
                int classVersion = Integer.parseInt(matcher.group(1));
                requiredFeature = classVersion - 44;
            } catch (NumberFormatException ignored) {
            }
        }
        if (requiredFeature > 0) {
            return "[LunarArc] Cannot load " + fileName + ": plugin requires Java "
                    + requiredFeature + ", but this server is running Java " + runtimeFeature + ".";
        }
        return "[LunarArc] Cannot load " + fileName
                + ": plugin was compiled for a newer Java runtime; this server is running Java "
                + runtimeFeature + ".";
    }

    private static final java.util.Set<String> JAVA_VERSION_WARNINGS = java.util.concurrent.ConcurrentHashMap.newKeySet();
    public static void warnJavaVersionOnce(java.nio.file.Path source, UnsupportedClassVersionError error) {
        String fileName = source.getFileName().toString();
        if (JAVA_VERSION_WARNINGS.add(fileName)) {
            org.slf4j.LoggerFactory.getLogger("LunarArc").warn(friendlyJavaVersionMessage(fileName, error));
        }
    }

    @Override
    public PluginDescriptionFile getPluginDescription(File file) throws InvalidDescriptionException {
        try (JarFile jar = new JarFile(file)) {
            JarEntry entry = jar.getJarEntry("paper-plugin.yml");
            boolean isPaper = entry != null;
            if (entry == null) entry = jar.getJarEntry("plugin.yml");

            if (entry == null) {
                throw new InvalidDescriptionException("Jar does not contain plugin.yml or paper-plugin.yml");
            }

            try (InputStream stream = jar.getInputStream(entry)) {
                if (!isPaper) {
                    return new PluginDescriptionFile(stream);
                } else {

                    Map<String, Object> map = yaml.load(stream);
                    String name = map.getOrDefault("name", "").toString();
                    String version = map.getOrDefault("version", "").toString();
                    String main = map.getOrDefault("main-class", map.getOrDefault("main", "")).toString();

                    if (name.isEmpty() || main.isEmpty()) {
                        throw new InvalidDescriptionException("paper-plugin.yml is missing 'name' or 'main-class'");
                    }

                    Map<String, Object> compatMap = new java.util.HashMap<>(map);
                    compatMap.put("main", main);
                    translatePaperDependencies(compatMap, map.get("dependencies"));

                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    yaml.dump(compatMap, new java.io.OutputStreamWriter(baos));
                    return new PluginDescriptionFile(new java.io.ByteArrayInputStream(baos.toByteArray()));
                }
            }
        } catch (Exception e) {
            throw new InvalidDescriptionException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void translatePaperDependencies(Map<String, Object> target, Object dependenciesValue) {
        if (!(dependenciesValue instanceof Map<?, ?> dependencies)) return;
        Object serverValue = dependencies.get("server");
        if (!(serverValue instanceof Map<?, ?> serverDependencies)) return;

        java.util.LinkedHashSet<String> depend = stringSet(target.get("depend"));
        java.util.LinkedHashSet<String> softDepend = stringSet(target.get("softdepend"));
        java.util.LinkedHashSet<String> loadBefore = stringSet(target.get("loadbefore"));

        for (Map.Entry<?, ?> entry : serverDependencies.entrySet()) {
            String dependencyName = String.valueOf(entry.getKey());
            if (dependencyName.isBlank()) continue;

            boolean required = true;
            String load = "OMIT";
            boolean joinClasspath = true;
            if (entry.getValue() instanceof Map<?, ?> dependency) {
                Object requiredValue = dependency.get("required");
                if (requiredValue != null) required = Boolean.parseBoolean(String.valueOf(requiredValue));
                Object loadValue = dependency.get("load");
                if (loadValue != null) load = String.valueOf(loadValue).toUpperCase(java.util.Locale.ROOT);
                Object joinValue = dependency.get("join-classpath");
                if (joinValue != null) joinClasspath = Boolean.parseBoolean(String.valueOf(joinValue));
            }


            // Paper dependency ordering is independent from required/classpath.
            // BEFORE means the dependency is loaded before this plugin; AFTER means
            // this plugin is loaded before the dependency; OMIT intentionally adds no
            // ordering edge. Required OMIT dependencies are validated during discovery.
            if ("AFTER".equals(load)) {
                loadBefore.add(dependencyName);
            } else if ("BEFORE".equals(load)) {
                if (required) depend.add(dependencyName);
                else softDepend.add(dependencyName);
            }


        }

        if (!depend.isEmpty()) target.put("depend", new java.util.ArrayList<>(depend));
        if (!softDepend.isEmpty()) target.put("softdepend", new java.util.ArrayList<>(softDepend));
        if (!loadBefore.isEmpty()) target.put("loadbefore", new java.util.ArrayList<>(loadBefore));
    }

    private static java.util.LinkedHashSet<String> stringSet(Object value) {
        java.util.LinkedHashSet<String> result = new java.util.LinkedHashSet<>();
        if (value instanceof Iterable<?> iterable) {
            for (Object item : iterable) if (item != null) result.add(String.valueOf(item));
        }
        return result;
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
                if (!eventClass.isAssignableFrom(event.getClass())) return;
                Thread thread = Thread.currentThread();
                ClassLoader previous = thread.getContextClassLoader();
                ClassLoader pluginLoader = plugin.getClass().getClassLoader();
                try {
                    thread.setContextClassLoader(pluginLoader);
                    method.invoke(listener, event);
                } catch (java.lang.reflect.InvocationTargetException ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    throw new EventException(cause);
                } catch (Exception ex) {
                    throw new EventException(ex);
                } finally {
                    thread.setContextClassLoader(previous);
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
                if (plugin instanceof org.bukkit.plugin.java.JavaPlugin jp) {
                    Thread thread = Thread.currentThread();
                    ClassLoader previous = thread.getContextClassLoader();
                    try {
                        thread.setContextClassLoader(jp.getClass().getClassLoader());
                        jp.setEnabled(true);
                    } finally {
                        thread.setContextClassLoader(previous);
                    }

                    if (!jp.isEnabled()) {
                        server.getLogger().severe("[LunarArc] Plugin " + plugin.getName()
                                + " failed to enable (check its log above for the root cause).");
                    } else {
                        server.getPluginManager().callEvent(new org.bukkit.event.server.PluginEnableEvent(plugin));
                    }
                }
            } catch (Exception e) {
                Throwable cause = e;
                if (e instanceof java.lang.reflect.InvocationTargetException ite && ite.getCause() != null) {
                    cause = ite.getCause();
                }
                server.getLogger().log(java.util.logging.Level.SEVERE, "Error enabling plugin " + plugin.getName(), cause);
                try {
                    if (plugin instanceof org.bukkit.plugin.java.JavaPlugin failed && failed.isEnabled()) {
                        Thread thread = Thread.currentThread();
                        ClassLoader previous = thread.getContextClassLoader();
                        try {
                            thread.setContextClassLoader(failed.getClass().getClassLoader());
                            failed.setEnabled(false);
                        } finally {
                            thread.setContextClassLoader(previous);
                        }
                    }
                } catch (Throwable disableError) {
                    cause.addSuppressed(disableError);
                }
                lunararc$cleanupPlugin(plugin, true);
                if (cause instanceof RuntimeException re) throw re;
                throw new RuntimeException(cause);
            }
        }
    }

    @Override
    public void disablePlugin(Plugin plugin) {
        if (plugin == null) return;
        boolean wasEnabled = plugin.isEnabled();

        try {
            if (wasEnabled && plugin instanceof org.bukkit.plugin.java.JavaPlugin javaPlugin) {
                Thread thread = Thread.currentThread();
                ClassLoader previous = thread.getContextClassLoader();
                try {
                    thread.setContextClassLoader(javaPlugin.getClass().getClassLoader());
                    javaPlugin.setEnabled(false);
                    server.getPluginManager().callEvent(new org.bukkit.event.server.PluginDisableEvent(plugin));
                } finally {
                    thread.setContextClassLoader(previous);
                }
            }
        } catch (Exception e) {
            server.getLogger().log(java.util.logging.Level.SEVERE,
                    "Error disabling plugin " + plugin.getName(), e);
        } finally {
            lunararc$cleanupPlugin(plugin, true);
        }
    }

    public void cleanupFailedPlugin(Plugin plugin) {
        if (plugin != null) lunararc$cleanupPlugin(plugin, true);
    }

    private void lunararc$cleanupPlugin(Plugin plugin, boolean closeClassLoader) {
        io.lunararcdevs.lunararc.common.server.LunarArcLifecycleEventRunner.unregisterAll(plugin);
        org.bukkit.event.HandlerList.unregisterAll(plugin);
        server.getScheduler().cancelTasks(plugin);
        try { server.getAsyncScheduler().cancelTasks(plugin); } catch (Throwable ignored) {}
        server.getServicesManager().unregisterAll(plugin);
        server.getMessenger().unregisterIncomingPluginChannel(plugin);
        server.getMessenger().unregisterOutgoingPluginChannel(plugin);
        for (org.bukkit.World world : server.getWorlds()) {
            try { world.removePluginChunkTickets(plugin); } catch (Throwable ignored) {}
        }
        if (server.getCommandMap() instanceof LunarArcCommandMap lunarCommands) {
            lunarCommands.unregisterPlugin(plugin);
        }
        if (closeClassLoader) {
            ClassLoader classLoader = plugin.getClass().getClassLoader();
            if (classLoader instanceof java.io.Closeable closeable) {
                try { closeable.close(); } catch (java.io.IOException ignored) {}
            }
        }
    }
}
