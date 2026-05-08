package org.bukkit.plugin;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import io.papermc.paper.plugin.manager.PaperPluginManagerImpl;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * LunarArc Custom SimplePluginManager stub to provide a bridge to
 * PaperPluginManagerImpl.
 */
public class SimplePluginManager implements PluginManager {
    private final Server server;
    private final SimpleCommandMap commandMap;
    private PluginManager paperPluginManager;
    
    // Fields expected by LuckPerms and other plugins via reflection
    private final Map<String, Permission> permissions = new java.util.HashMap<>();
    private final Map<Boolean, Set<Permission>> defaultPerms = new java.util.LinkedHashMap<>();
    private final Map<String, Map<Permissible, Boolean>> permSubs = new java.util.HashMap<>();
    private final Map<Boolean, Map<Permissible, Boolean>> defSubs = new java.util.HashMap<>();

    public SimplePluginManager(Server server, SimpleCommandMap commandMap) {
        this.server = server;
        this.commandMap = commandMap;
    }

    public void setInternalManager(PluginManager manager) {
        this.paperPluginManager = manager;
    }

    @Override
    public void registerInterface(@NotNull Class<? extends PluginLoader> loader) {
        if (paperPluginManager != null)
            paperPluginManager.registerInterface(loader);
    }

    @Override
    public @Nullable Plugin getPlugin(@NotNull String name) {
        return paperPluginManager != null ? paperPluginManager.getPlugin(name) : null;
    }

    @Override
    public Plugin @NotNull [] getPlugins() {
        return paperPluginManager != null ? paperPluginManager.getPlugins() : new Plugin[0];
    }

    @Override
    public boolean isPluginEnabled(@NotNull String name) {
        return paperPluginManager != null && paperPluginManager.isPluginEnabled(name);
    }

    @Override
    public boolean isPluginEnabled(@Nullable Plugin plugin) {
        return paperPluginManager != null && paperPluginManager.isPluginEnabled(plugin);
    }

    @Override
    public @Nullable Plugin loadPlugin(@NotNull File file)
            throws InvalidPluginException, InvalidDescriptionException, UnknownDependencyException {
        return paperPluginManager != null ? paperPluginManager.loadPlugin(file) : null;
    }

    @Override
    public Plugin @NotNull [] loadPlugins(@NotNull File directory) {
        return paperPluginManager != null ? paperPluginManager.loadPlugins(directory) : new Plugin[0];
    }

    public void enablePlugins() {
        enablePlugins(null);
    }

    public void enablePlugins(org.bukkit.plugin.PluginLoadOrder type) {
        if (paperPluginManager instanceof PaperPluginManagerImpl paper) {
            paper.enablePlugins(type);
        } else if (paperPluginManager != null) {
            try {
                java.lang.reflect.Method method = paperPluginManager.getClass().getMethod("enablePlugins");
                method.invoke(paperPluginManager);
            } catch (Exception e) {
                // Fallback or ignore
            }
        }
    }

    @Override
    public void disablePlugins() {
        if (paperPluginManager != null)
            paperPluginManager.disablePlugins();
    }

    @Override
    public void clearPlugins() {
        if (paperPluginManager != null)
            paperPluginManager.clearPlugins();
    }

    @Override
    public void registerEvents(@NotNull Listener listener, @NotNull Plugin plugin) {
        if (paperPluginManager != null)
            paperPluginManager.registerEvents(listener, plugin);
    }

    @Override
    public void registerEvent(@NotNull Class<? extends Event> event, @NotNull Listener listener,
            @NotNull EventPriority priority, @NotNull EventExecutor executor, @NotNull Plugin plugin) {
        if (paperPluginManager != null)
            paperPluginManager.registerEvent(event, listener, priority, executor, plugin);
    }

    @Override
    public void registerEvent(@NotNull Class<? extends Event> event, @NotNull Listener listener,
            @NotNull EventPriority priority, @NotNull EventExecutor executor, @NotNull Plugin plugin,
            boolean ignoreCancelled) {
        if (paperPluginManager != null)
            paperPluginManager.registerEvent(event, listener, priority, executor, plugin, ignoreCancelled);
    }

    @Override
    public void enablePlugin(@NotNull Plugin plugin) {
        if (paperPluginManager != null)
            paperPluginManager.enablePlugin(plugin);
    }

    @Override
    public void disablePlugin(@NotNull Plugin plugin) {
        if (paperPluginManager != null)
            paperPluginManager.disablePlugin(plugin);
    }

    @Override
    public @Nullable Permission getPermission(@NotNull String name) {
        Permission perm = permissions.get(name);
        if (perm == null && paperPluginManager != null) return paperPluginManager.getPermission(name);
        return perm;
    }

    @Override
    public void addPermission(@NotNull Permission permission) {
        permissions.put(permission.getName(), permission);
        if (paperPluginManager != null) paperPluginManager.addPermission(permission);
    }

    @Override
    public void removePermission(@NotNull Permission permission) {
        permissions.remove(permission.getName());
        if (paperPluginManager != null) paperPluginManager.removePermission(permission);
    }

    @Override
    public void removePermission(@NotNull String name) {
        permissions.remove(name);
        if (paperPluginManager != null) paperPluginManager.removePermission(name);
    }

    @Override
    public @NotNull Set<Permission> getDefaultPermissions(boolean op) {
        return paperPluginManager != null ? paperPluginManager.getDefaultPermissions(op)
                : defaultPerms.getOrDefault(op, Collections.emptySet());
    }

    @Override
    public void recalculatePermissionDefaults(@NotNull Permission perm) {
        if (paperPluginManager != null)
            paperPluginManager.recalculatePermissionDefaults(perm);
    }

    @Override
    public void subscribeToPermission(@NotNull String permission, @NotNull Permissible permissible) {
        permSubs.computeIfAbsent(permission, k -> new java.util.HashMap<>()).put(permissible, true);
        if (paperPluginManager != null)
            paperPluginManager.subscribeToPermission(permission, permissible);
    }

    @Override
    public void unsubscribeFromPermission(@NotNull String permission, @NotNull Permissible permissible) {
        Map<Permissible, Boolean> subs = permSubs.get(permission);
        if (subs != null) subs.remove(permissible);
        if (paperPluginManager != null)
            paperPluginManager.unsubscribeFromPermission(permission, permissible);
    }

    @Override
    public @NotNull Set<Permissible> getPermissionSubscriptions(@NotNull String permission) {
        Map<Permissible, Boolean> subs = permSubs.get(permission);
        if (subs == null || subs.isEmpty()) {
            if (paperPluginManager != null) return paperPluginManager.getPermissionSubscriptions(permission);
            return Collections.emptySet();
        }
        return new HashSet<>(subs.keySet());
    }

    @Override
    public void subscribeToDefaultPerms(boolean op, @NotNull Permissible permissible) {
        defSubs.computeIfAbsent(op, k -> new java.util.HashMap<>()).put(permissible, true);
        if (paperPluginManager != null)
            paperPluginManager.subscribeToDefaultPerms(op, permissible);
    }

    @Override
    public void unsubscribeFromDefaultPerms(boolean op, @NotNull Permissible permissible) {
        Map<Permissible, Boolean> subs = defSubs.get(op);
        if (subs != null) subs.remove(permissible);
        if (paperPluginManager != null)
            paperPluginManager.unsubscribeFromDefaultPerms(op, permissible);
    }

    @Override
    public @NotNull Set<Permissible> getDefaultPermSubscriptions(boolean op) {
        Map<Permissible, Boolean> subs = defSubs.get(op);
        if (subs == null || subs.isEmpty()) {
            if (paperPluginManager != null) return paperPluginManager.getDefaultPermSubscriptions(op);
            return Collections.emptySet();
        }
        return new HashSet<>(subs.keySet());
    }

    @Override
    public @NotNull Set<Permission> getPermissions() {
        Set<Permission> all = new java.util.HashSet<>(permissions.values());
        if (paperPluginManager != null) all.addAll(paperPluginManager.getPermissions());
        return all;
    }

    @Override
    public boolean useTimings() {
        return paperPluginManager != null && paperPluginManager.useTimings();
    }

    @Override
    public void callEvent(@NotNull Event event) {
        if (paperPluginManager != null)
            paperPluginManager.callEvent(event);
    }

    @Override
    public void overridePermissionManager(@NotNull Plugin plugin,
            @Nullable io.papermc.paper.plugin.PermissionManager permissionManager) {
        if (paperPluginManager != null)
            paperPluginManager.overridePermissionManager(plugin, permissionManager);
    }

    @Override
    public boolean isTransitiveDependency(@NotNull io.papermc.paper.plugin.configuration.PluginMeta plugin,
            @NotNull io.papermc.paper.plugin.configuration.PluginMeta dependency) {
        return paperPluginManager != null && paperPluginManager.isTransitiveDependency(plugin, dependency);
    }

    @Override
    public Plugin @NotNull [] loadPlugins(@NotNull File @NotNull [] files) {
        return paperPluginManager != null ? paperPluginManager.loadPlugins(files) : new Plugin[0];
    }

    @Override
    public void clearPermissions() {
        permissions.clear();
        if (paperPluginManager != null)
            paperPluginManager.clearPermissions();
    }

    @Override
    public void addPermissions(@NotNull List<Permission> perms) {
        for (Permission p : perms) permissions.put(p.getName(), p);
        if (paperPluginManager != null)
            paperPluginManager.addPermissions(perms);
    }
}
