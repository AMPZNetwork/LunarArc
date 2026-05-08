package io.papermc.paper.plugin.manager;

import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.*;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

import org.bukkit.command.SimpleCommandMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.logging.Level;

/**
 * Stub implementation of PaperPluginManagerImpl to satisfy Paper 1.21.1
 * requirements.
 * This class serves as the bridge between legacy Spigot loading and modern
 * Paper loading.
 */
public class PaperPluginManagerImpl implements PluginManager {
    private final Server server;
    private final SimpleCommandMap commandMap;
    private final SimplePluginManager simpleManager;
    private final io.ampznetwork.lunararc.common.server.LunarArcPluginLoader pluginLoader;
    private final Map<String, Plugin> plugins = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Map<String, PluginDescriptionFile> descriptions = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final List<Plugin> pluginList = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger("LunarArc");

    public PaperPluginManagerImpl(Server server, SimpleCommandMap commandMap,
            SimplePluginManager simpleManager) {
        this.server = server;
        this.commandMap = commandMap;
        this.simpleManager = simpleManager;
        this.pluginLoader = new io.ampznetwork.lunararc.common.server.LunarArcPluginLoader(server);
    }

    @Override
    public void callEvent(@NotNull org.bukkit.event.Event event) {
        if (event.isAsynchronous() && server.isPrimaryThread()) {
            throw new IllegalStateException(event.getEventName() + " may only be triggered asynchronously.");
        } else if (!event.isAsynchronous() && !server.isPrimaryThread()) {
            throw new IllegalStateException(event.getEventName() + " may only be triggered synchronously.");
        }

        HandlerList handlers = event.getHandlers();
        RegisteredListener[] listeners = handlers.getRegisteredListeners();

        for (RegisteredListener registration : listeners) {
            if (!registration.getPlugin().isEnabled()) {
                continue;
            }

            try {
                registration.callEvent(event);
            } catch (Throwable ex) {
                server.getLogger().log(java.util.logging.Level.SEVERE, "Could not pass event " + event.getEventName()
                        + " to " + registration.getPlugin().getDescription().getFullName(), ex);
            }
        }
    }

    @Override
    public void registerInterface(@NotNull Class<? extends org.bukkit.plugin.PluginLoader> loader) {
    }

    @Override
    public Plugin getPlugin(@NotNull String name) {
        return plugins.get(normalizePluginName(name));
    }

    @Override
    public Plugin[] getPlugins() {
        return pluginList.toArray(new Plugin[0]);
    }

    @Override
    public boolean isPluginEnabled(@NotNull String name) {
        Plugin plugin = getPlugin(name);
        return plugin != null && plugin.isEnabled();
    }

    @Override
    public boolean isPluginEnabled(Plugin plugin) {
        return plugin != null && plugin.isEnabled();
    }

    @Override
    public Plugin loadPlugin(@NotNull java.io.File file)
            throws InvalidPluginException, InvalidDescriptionException, UnknownDependencyException {
        PluginDescriptionFile description = pluginLoader.getPluginDescription(file);
        validateHardDependencies(description);

        Plugin plugin = pluginLoader.loadPlugin(file);
        if (plugin != null) {
            String pluginName = normalizePluginName(plugin.getName());
            if (plugins.containsKey(pluginName)) {
                throw new InvalidPluginException("Plugin " + plugin.getName() + " is already loaded");
            }
            plugins.put(pluginName, plugin);
            descriptions.put(pluginName, plugin.getDescription());
            for (String provided : plugin.getDescription().getProvides()) {
                String providedName = normalizePluginName(provided);
                if (!plugins.containsKey(providedName)) {
                    plugins.put(providedName, plugin);
                    descriptions.put(providedName, plugin.getDescription());
                }
            }
            pluginList.add(plugin);
            try {
                plugin.onLoad();
            } catch (Throwable e) {
                logger.error("[LunarArc] Error while loading {}", plugin.getDescription().getFullName(), e);
                throw new InvalidPluginException(e);
            }
            logger.info("[LunarArc] Loaded plugin {}", plugin.getDescription().getFullName());
        }
        return plugin;
    }

    @Override
    public Plugin[] loadPlugins(@NotNull java.io.File directory) {
        logger.info("[LunarArc] Scanning directory for plugins: " + directory.getAbsolutePath());
        if (!directory.isDirectory()) {
            logger.warn("[LunarArc] " + directory.getAbsolutePath() + " is not a directory!");
            return new Plugin[0];
        }
        java.io.File[] files = directory.listFiles(file -> file.isFile()
                && file.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".jar"));
        if (files == null || files.length == 0) {
            logger.info("[LunarArc] No plugin jars found in " + directory.getAbsolutePath());
            return new Plugin[0];
        }
        logger.info("[LunarArc] Found " + files.length + " jar files in " + directory.getAbsolutePath());
        java.util.Arrays.sort(files,
                java.util.Comparator.comparing(java.io.File::getName, String.CASE_INSENSITIVE_ORDER));
        return loadPlugins(files);
    }

    @Override
    public Plugin[] loadPlugins(@NotNull java.io.File[] files) {
        logger.info("[LunarArc] Loading " + files.length + " plugin jars...");
        java.util.List<Plugin> loaded = new java.util.ArrayList<>();
        java.util.Map<String, PluginCandidate> candidates = discoverPluginCandidates(files);

        logger.info("[LunarArc] Discovered " + candidates.size() + " plugin candidates.");
        java.util.Map<String, String> candidateAliases = buildCandidateAliases(candidates);
        for (PluginCandidate candidate : sortCandidates(candidates, candidateAliases)) {
            try {
                logger.info("[LunarArc] Attempting to load plugin: " + candidate.file.getName());
                Plugin plugin = loadPlugin(candidate.file);
                if (plugin != null) {
                    loaded.add(plugin);
                    logger.info("[LunarArc] Successfully loaded: " + plugin.getName());
                }
            } catch (Throwable e) {
                logger.error("[LunarArc] Could not load plugin from " + candidate.file.getName(), e);
            }
        }
        logger.info("[LunarArc] Loaded " + loaded.size() + " plugin(s).");
        return loaded.toArray(new Plugin[0]);
    }

    public void enablePlugins() {
        enablePlugins(null);
    }

    public void enablePlugins(org.bukkit.plugin.PluginLoadOrder type) {
        java.util.List<Plugin> unenabled = new java.util.ArrayList<>(pluginList);
        boolean changed = true;
        while (changed && !unenabled.isEmpty()) {
            changed = false;
            java.util.Iterator<Plugin> it = unenabled.iterator();
            while (it.hasNext()) {
                Plugin plugin = it.next();
                if (plugin.isEnabled()) {
                    it.remove();
                    continue;
                }

                if (type != null && plugin.getDescription().getLoad() != type) {
                    continue;
                }

                boolean canEnable = true;
                for (String depend : plugin.getDescription().getDepend()) {
                    Plugin dep = getPlugin(depend);
                    if (dep == null || !dep.isEnabled()) {
                        canEnable = false;
                        break;
                    }
                }
                if (canEnable) {
                    for (String softDepend : plugin.getDescription().getSoftDepend()) {
                        Plugin dep = getPlugin(softDepend);
                        if (dep != null && !dep.isEnabled() && unenabled.contains(dep)) {
                            if (dep == plugin) {
                                continue;
                            }
                            if (type == null || dep.getDescription().getLoad() == type) {
                                canEnable = false;
                                break;
                            }
                        }
                    }
                }

                if (canEnable) {
                    try {
                        long start = System.currentTimeMillis();
                        enablePlugin(plugin);
                        long end = System.currentTimeMillis();
                        logger.info("[LunarArc] Enabled plugin {} (took {}ms)", plugin.getDescription().getFullName(),
                                (end - start));
                        it.remove();
                        changed = true;
                    } catch (Throwable e) {
                        logger.error("[LunarArc] Error enabling {}", plugin.getDescription().getFullName(), e);
                        it.remove();
                        changed = true;
                    }
                }
            }

            if (!changed && !unenabled.isEmpty()) {
                // Check if any matching type plugins are actually stuck
                boolean matchingTypeStuck = false;
                for (Plugin p : unenabled) {
                    if (type == null || p.getDescription().getLoad() == type) {
                        matchingTypeStuck = true;
                        List<String> reasons = getEnablementReasons(p);
                        logger.warn("[LunarArc] Cannot enable {} yet. Reasons: {}", p.getName(), reasons);
                    }
                }
                if (matchingTypeStuck) {
                    // We have stuck plugins of the current type, might be circular
                    break;
                } else {
                    // Only plugins of other types left, we are done with this phase
                    break;
                }
            }
        }

        if (type == org.bukkit.plugin.PluginLoadOrder.POSTWORLD || type == null) {
            for (Plugin plugin : unenabled) {
                if (plugin.isEnabled())
                    continue;
                server.getLogger().severe("[LunarArc] Could not resolve enable order for "
                        + plugin.getDescription().getFullName() + "; check for circular dependencies.");
            }
        }
    }

    private List<String> getEnablementReasons(Plugin plugin) {
        List<String> reasons = new ArrayList<>();
        for (String depend : plugin.getDescription().getDepend()) {
            Plugin dep = getPlugin(depend);
            if (dep == null)
                reasons.add("missing depend: " + depend);
            else if (!dep.isEnabled())
                reasons.add("depend not enabled: " + depend);
        }
        for (String softDepend : plugin.getDescription().getSoftDepend()) {
            Plugin dep = getPlugin(softDepend);
            if (dep != null && dep != plugin && !dep.isEnabled() && pluginList.contains(dep)) {
                reasons.add("softDepend not enabled: " + softDepend);
            }
        }
        return reasons;
    }

    public void disablePlugins() {
        for (int i = pluginList.size() - 1; i >= 0; i--) {
            disablePlugin(pluginList.get(i));
        }
    }

    @Override
    public void clearPlugins() {
        HandlerList.unregisterAll();
        plugins.clear();
        descriptions.clear();
        pluginList.clear();
    }

    @Override
    public void registerEvent(@NotNull Class<? extends org.bukkit.event.Event> event,
            @NotNull org.bukkit.event.Listener listener, @NotNull org.bukkit.event.EventPriority priority,
            @NotNull org.bukkit.plugin.EventExecutor executor, @NotNull Plugin plugin) {
        registerEvent(event, listener, priority, executor, plugin, false);
    }

    @Override
    public void registerEvent(@NotNull Class<? extends org.bukkit.event.Event> event,
            @NotNull org.bukkit.event.Listener listener, @NotNull org.bukkit.event.EventPriority priority,
            @NotNull org.bukkit.plugin.EventExecutor executor, @NotNull Plugin plugin, boolean ignoreCancelled) {
        if (!plugin.isEnabled()) {
            throw new IllegalPluginAccessException("Plugin attempted to register " + event + " while not enabled");
        }

        getEventListeners(event)
                .register(new RegisteredListener(listener, executor, priority, plugin, ignoreCancelled));
    }

    @Override
    public void registerEvents(@NotNull org.bukkit.event.Listener listener, @NotNull Plugin plugin) {
        if (!plugin.isEnabled()) {
            throw new IllegalPluginAccessException("Plugin attempted to register " + listener + " while not enabled");
        }

        for (java.util.Map.Entry<Class<? extends org.bukkit.event.Event>, java.util.Set<RegisteredListener>> entry : pluginLoader
                .createRegisteredListeners(listener, plugin).entrySet()) {
            getEventListeners(getRegistrationClass(entry.getKey())).registerAll(entry.getValue());
        }
    }

    private HandlerList getEventListeners(Class<? extends org.bukkit.event.Event> type) {
        try {
            java.lang.reflect.Method method = getRegistrationClass(type).getDeclaredMethod("getHandlerList");
            method.setAccessible(true);
            return (HandlerList) method.invoke(null);
        } catch (Exception e) {
            throw new org.bukkit.plugin.IllegalPluginAccessException(
                    "Error while registering listener for event type " + type.getName() + ": " + e.toString());
        }
    }

    private Class<? extends org.bukkit.event.Event> getRegistrationClass(
            Class<? extends org.bukkit.event.Event> clazz) {
        try {
            clazz.getDeclaredMethod("getHandlerList");
            return clazz;
        } catch (NoSuchMethodException e) {
            if (clazz.getSuperclass() != null
                    && !clazz.getSuperclass().equals(org.bukkit.event.Event.class)
                    && org.bukkit.event.Event.class.isAssignableFrom(clazz.getSuperclass())) {
                return getRegistrationClass(clazz.getSuperclass().asSubclass(org.bukkit.event.Event.class));
            } else {
                throw new org.bukkit.plugin.IllegalPluginAccessException("Unable to find handler list for event "
                        + clazz.getName() + ". Static getHandlerList method required!");
            }
        }
    }

    @Override
    public void enablePlugin(@NotNull Plugin plugin) {
        pluginLoader.enablePlugin(plugin);
    }

    @Override
    public void disablePlugin(@NotNull Plugin plugin) {
        pluginLoader.disablePlugin(plugin);
    }

    @Override
    public Permission getPermission(@NotNull String name) {
        return null;
    }

    @Override
    public void addPermission(@NotNull Permission perm) {
    }

    @Override
    public void removePermission(@NotNull Permission perm) {
    }

    @Override
    public void removePermission(@NotNull String name) {
    }

    @Override
    public Set<Permission> getDefaultPermissions(boolean op) {
        return java.util.Collections.emptySet();
    }

    @Override
    public void recalculatePermissionDefaults(@NotNull Permission perm) {
    }

    @Override
    public void subscribeToPermission(@NotNull String permission,
            @NotNull org.bukkit.permissions.Permissible permissible) {
    }

    @Override
    public void unsubscribeFromPermission(@NotNull String permission,
            @NotNull org.bukkit.permissions.Permissible permissible) {
    }

    @Override
    public Set<org.bukkit.permissions.Permissible> getPermissionSubscriptions(@NotNull String permission) {
        return java.util.Collections.emptySet();
    }

    @Override
    public void subscribeToDefaultPerms(boolean op, @NotNull org.bukkit.permissions.Permissible permissible) {
    }

    @Override
    public void unsubscribeFromDefaultPerms(boolean op, @NotNull org.bukkit.permissions.Permissible permissible) {
    }

    @Override
    public Set<org.bukkit.permissions.Permissible> getDefaultPermSubscriptions(boolean op) {
        return java.util.Collections.emptySet();
    }

    @Override
    public Set<Permission> getPermissions() {
        return java.util.Collections.emptySet();
    }

    @Override
    public boolean useTimings() {
        return false;
    }

    @Override
    public void overridePermissionManager(@NotNull Plugin plugin,
            @org.jetbrains.annotations.Nullable io.papermc.paper.plugin.PermissionManager permissionManager) {
    }

    @Override
    public boolean isTransitiveDependency(@NotNull io.papermc.paper.plugin.configuration.PluginMeta plugin,
            @NotNull io.papermc.paper.plugin.configuration.PluginMeta dependency) {
        return false;
    }

    @Override
    public void clearPermissions() {
    }

    @Override
    public void addPermissions(@NotNull java.util.List<Permission> perms) {
    }

    private String normalizePluginName(String name) {
        return name.replace(' ', '_');
    }

    private java.util.Map<String, PluginCandidate> discoverPluginCandidates(java.io.File[] files) {
        java.util.Map<String, PluginCandidate> candidates = new java.util.LinkedHashMap<>();
        java.util.Set<String> claimedNames = new java.util.HashSet<>(plugins.keySet());
        java.util.List<java.io.File> jars = new java.util.ArrayList<>();
        for (java.io.File file : files) {
            if (file == null || !file.isFile()
                    || !file.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".jar")) {
                continue;
            }
            jars.add(file);
        }
        jars.sort(java.util.Comparator.comparing(java.io.File::getName, String.CASE_INSENSITIVE_ORDER));

        for (java.io.File file : jars) {
            try {
                PluginDescriptionFile description = pluginLoader.getPluginDescription(file);
                String pluginName = normalizePluginName(description.getName());
                if (candidates.containsKey(pluginName) || plugins.containsKey(pluginName)) {
                    server.getLogger().severe("[LunarArc] Duplicate plugin " + description.getName()
                            + " in " + file.getName() + "; skipping");
                    continue;
                }
                boolean duplicateAlias = false;
                for (String provided : description.getProvides()) {
                    String providedName = normalizePluginName(provided);
                    if (claimedNames.contains(providedName)) {
                        server.getLogger().severe("[LunarArc] Plugin " + description.getName()
                                + " provides duplicate plugin name " + provided + "; skipping");
                        duplicateAlias = true;
                        break;
                    }
                }
                if (duplicateAlias) {
                    continue;
                }
                candidates.put(pluginName, new PluginCandidate(file, description));
                claimedNames.add(pluginName);
                for (String provided : description.getProvides()) {
                    claimedNames.add(normalizePluginName(provided));
                }
            } catch (Exception e) {
                server.getLogger().log(Level.SEVERE,
                        "Could not read plugin metadata from " + file.getName() + ": " + e.getMessage(), e);
            }
        }
        return candidates;
    }

    private java.util.Map<String, String> buildCandidateAliases(java.util.Map<String, PluginCandidate> candidates) {
        java.util.Map<String, String> aliases = new java.util.HashMap<>();
        for (java.util.Map.Entry<String, PluginCandidate> entry : candidates.entrySet()) {
            aliases.put(entry.getKey(), entry.getKey());
            for (String provided : entry.getValue().description.getProvides()) {
                aliases.putIfAbsent(normalizePluginName(provided), entry.getKey());
            }
        }
        return aliases;
    }

    private java.util.List<PluginCandidate> sortCandidates(java.util.Map<String, PluginCandidate> candidates,
            java.util.Map<String, String> aliases) {
        java.util.List<PluginCandidate> sorted = new java.util.ArrayList<>();
        java.util.Set<String> visiting = new java.util.HashSet<>();
        java.util.Set<String> visited = new java.util.HashSet<>();

        java.util.List<String> startup = new java.util.ArrayList<>();
        java.util.List<String> postWorld = new java.util.ArrayList<>();
        for (java.util.Map.Entry<String, PluginCandidate> entry : candidates.entrySet()) {
            if (entry.getValue().description.getLoad() == PluginLoadOrder.STARTUP) {
                startup.add(entry.getKey());
            } else {
                postWorld.add(entry.getKey());
            }
        }

        for (String name : startup) {
            visitCandidate(name, candidates, aliases, visiting, visited, sorted);
        }
        for (String name : postWorld) {
            visitCandidate(name, candidates, aliases, visiting, visited, sorted);
        }
        return sorted;
    }

    private void visitCandidate(String name, java.util.Map<String, PluginCandidate> candidates,
            java.util.Map<String, String> aliases, java.util.Set<String> visiting,
            java.util.Set<String> visited, java.util.List<PluginCandidate> sorted) {
        if (visited.contains(name)) {
            return;
        }
        if (!visiting.add(name)) {
            server.getLogger().severe("[LunarArc] Circular plugin dependency involving " + name);
            return;
        }

        PluginCandidate candidate = candidates.get(name);
        if (candidate == null) {
            return;
        }

        for (String dependency : candidate.description.getDepend()) {
            String normalized = aliases.get(normalizePluginName(dependency));
            if (normalized != null && !normalized.equals(name) && candidates.containsKey(normalized)) {
                visitCandidate(normalized, candidates, aliases, visiting, visited, sorted);
            }
        }
        for (String dependency : candidate.description.getSoftDepend()) {
            String normalized = aliases.get(normalizePluginName(dependency));
            if (normalized != null && !normalized.equals(name) && candidates.containsKey(normalized)) {
                visitCandidate(normalized, candidates, aliases, visiting, visited, sorted);
            }
        }

        visiting.remove(name);
        visited.add(name);
        sorted.add(candidate);
    }

    private void validateHardDependencies(PluginDescriptionFile description) throws UnknownDependencyException {
        String missingDependency = findMissingDependency(description);
        if (missingDependency != null) {
            throw new UnknownDependencyException(missingDependency);
        }
    }

    private String findMissingDependency(PluginDescriptionFile description) {
        for (String dependency : description.getDepend()) {
            if (getPlugin(dependency) == null) {
                return dependency;
            }
        }
        return null;
    }

    private static class PluginCandidate {
        private final java.io.File file;
        private final PluginDescriptionFile description;

        public PluginCandidate(java.io.File file, PluginDescriptionFile description) {
            this.file = file;
            this.description = description;
        }

        public java.io.File file() {
            return file;
        }

        public PluginDescriptionFile description() {
            return description;
        }
    }
}
