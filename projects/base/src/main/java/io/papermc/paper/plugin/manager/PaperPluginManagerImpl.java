package io.papermc.paper.plugin.manager;

import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.*;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Stub implementation of PaperPluginManagerImpl to satisfy Paper 1.21.1 requirements.
 * This class serves as the bridge between legacy Spigot loading and modern Paper loading.
 */
public class PaperPluginManagerImpl implements PluginManager {
    private final Server server;
    private final CommandMap commandMap;
    private final PluginManager pluginManager;
    private final io.ampznetwork.lunararc.common.server.LunarArcPluginLoader pluginLoader;

    public PaperPluginManagerImpl(@NotNull Server server, @NotNull CommandMap commandMap, @NotNull PluginManager pluginManager) {
        this.server = server;
        this.commandMap = commandMap;
        this.pluginManager = pluginManager;
        this.pluginLoader = new io.ampznetwork.lunararc.common.server.LunarArcPluginLoader(server);
        System.out.println("[LunarArc] Paper Plugin Manager Engine initialized.");
    }

    // Modern plugins (paper-plugin.yml) look for these methods.
    @Override
    public void callEvent(@NotNull org.bukkit.event.Event event) {
        // Safe stub: Modern Paper plugins would be handled here.
        // Legacy plugins are handled by the caller (SimplePluginManager).
    }

    @Override
    public void registerInterface(@NotNull Class<? extends PluginLoader> loader) {}

    @Override
    public Plugin getPlugin(@NotNull String name) { return null; }

    @Override
    public Plugin[] getPlugins() { return new Plugin[0]; }

    @Override
    public boolean isPluginEnabled(@NotNull String name) { return false; }

    @Override
    public boolean isPluginEnabled(Plugin plugin) { return false; }

    @Override
    public Plugin loadPlugin(@NotNull java.io.File file) throws InvalidPluginException, InvalidDescriptionException, UnknownDependencyException {
        return pluginLoader.loadPlugin(file);
    }

    @Override
    public Plugin[] loadPlugins(@NotNull java.io.File directory) { return new Plugin[0]; }

    @Override
    public void disablePlugins() {}

    @Override
    public void clearPlugins() {}

    @Override
    public void registerEvent(@NotNull Class<? extends org.bukkit.event.Event> event, @NotNull org.bukkit.event.Listener listener, @NotNull org.bukkit.event.EventPriority priority, @NotNull org.bukkit.plugin.EventExecutor executor, @NotNull Plugin plugin) {}

    @Override
    public void registerEvent(@NotNull Class<? extends org.bukkit.event.Event> event, @NotNull org.bukkit.event.Listener listener, @NotNull org.bukkit.event.EventPriority priority, @NotNull org.bukkit.plugin.EventExecutor executor, @NotNull Plugin plugin, boolean ignoreCancelled) {}

    @Override
    public void registerEvents(@NotNull org.bukkit.event.Listener listener, @NotNull Plugin plugin) {}

    @Override
    public void enablePlugin(@NotNull Plugin plugin) {
        pluginLoader.enablePlugin(plugin);
    }

    @Override
    public void disablePlugin(@NotNull Plugin plugin) {
        pluginLoader.disablePlugin(plugin);
    }

    @Override
    public Permission getPermission(@NotNull String name) { return null; }

    @Override
    public void addPermission(@NotNull Permission perm) {}

    @Override
    public void removePermission(@NotNull Permission perm) {}

    @Override
    public void removePermission(@NotNull String name) {}

    @Override
    public Set<Permission> getDefaultPermissions(boolean op) { return java.util.Collections.emptySet(); }

    @Override
    public void recalculatePermissionDefaults(@NotNull Permission perm) {}

    @Override
    public void subscribeToPermission(@NotNull String permission, @NotNull org.bukkit.permissions.Permissible permissible) {}

    @Override
    public void unsubscribeFromPermission(@NotNull String permission, @NotNull org.bukkit.permissions.Permissible permissible) {}

    @Override
    public Set<org.bukkit.permissions.Permissible> getPermissionSubscriptions(@NotNull String permission) { return java.util.Collections.emptySet(); }

    @Override
    public void subscribeToDefaultPerms(boolean op, @NotNull org.bukkit.permissions.Permissible permissible) {}

    @Override
    public void unsubscribeFromDefaultPerms(boolean op, @NotNull org.bukkit.permissions.Permissible permissible) {}

    @Override
    public Set<org.bukkit.permissions.Permissible> getDefaultPermSubscriptions(boolean op) { return java.util.Collections.emptySet(); }

    @Override
    public Set<Permission> getPermissions() { return java.util.Collections.emptySet(); }

    @Override
    public boolean useTimings() { return false; }

    @Override
    public void overridePermissionManager(@NotNull Plugin plugin, @org.jetbrains.annotations.Nullable io.papermc.paper.plugin.PermissionManager permissionManager) {}

    @Override
    public boolean isTransitiveDependency(@NotNull io.papermc.paper.plugin.configuration.PluginMeta plugin, @NotNull io.papermc.paper.plugin.configuration.PluginMeta dependency) { return false; }

    @Override
    public Plugin[] loadPlugins(@NotNull java.io.File[] files) { return new Plugin[0]; }

    @Override
    public void clearPermissions() {}

    @Override
    public void addPermissions(@NotNull java.util.List<Permission> perms) {}
}
