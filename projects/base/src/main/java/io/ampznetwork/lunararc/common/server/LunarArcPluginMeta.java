package io.ampznetwork.lunararc.common.server;

import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoadOrder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class LunarArcPluginMeta implements PluginMeta {
    private final PluginDescriptionFile description;

    public LunarArcPluginMeta(PluginDescriptionFile description) {
        this.description = description;
    }

    @Override
    public @NotNull String getName() {
        return description.getName();
    }

    @Override
    public @NotNull String getDisplayName() {
        return description.getName();
    }

    @Override
    public @NotNull String getMainClass() {
        return description.getMain();
    }

    @Override
    public @NotNull PluginLoadOrder getLoadOrder() {
        return description.getLoad();
    }

    @Override
    public @NotNull String getVersion() {
        return description.getVersion();
    }

    @Override
    public @Nullable String getLoggerPrefix() {
        return description.getPrefix();
    }

    @Override
    public @NotNull List<String> getPluginDependencies() {
        return description.getDepend();
    }

    @Override
    public @NotNull List<String> getPluginSoftDependencies() {
        return description.getSoftDepend();
    }

    @Override
    public @NotNull List<String> getLoadBeforePlugins() {
        return description.getLoadBefore();
    }

    @Override
    public @NotNull List<String> getProvidedPlugins() {
        return description.getProvides();
    }

    @Override
    public @NotNull List<String> getAuthors() {
        return description.getAuthors();
    }

    @Override
    public @NotNull List<String> getContributors() {
        return Collections.emptyList();
    }

    @Override
    public @Nullable String getDescription() {
        return description.getDescription();
    }

    @Override
    public @Nullable String getWebsite() {
        return description.getWebsite();
    }

    @Override
    public @NotNull List<Permission> getPermissions() {
        return description.getPermissions();
    }

    @Override
    public @NotNull PermissionDefault getPermissionDefault() {
        return description.getPermissionDefault() != null ? description.getPermissionDefault() : PermissionDefault.OP;
    }

    @Override
    public @Nullable String getAPIVersion() {
        return description.getAPIVersion();
    }

    public @NotNull List<String> getPluginLibraries() {
        return Collections.emptyList();
    }
}
