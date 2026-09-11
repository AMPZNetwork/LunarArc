package org.bukkit.craftbukkit.command;

import java.util.Set;
import java.util.UUID;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ServerCommandSender implements CommandSender {
    public final PermissibleBase perm;

    public ServerCommandSender() { this.perm = new PermissibleBase(this); }
    public ServerCommandSender(PermissibleBase perm) { this.perm = java.util.Objects.requireNonNull(perm); }
    private final CommandSender.Spigot spigot = new CommandSender.Spigot() {
        @Override
        public void sendMessage(net.md_5.bungee.api.chat.BaseComponent component) {
            sendMessage(new net.md_5.bungee.api.chat.BaseComponent[]{component});
        }

        @Override
        public void sendMessage(net.md_5.bungee.api.chat.BaseComponent... components) {
            ServerCommandSender.this.sendMessage(net.md_5.bungee.api.chat.TextComponent.toLegacyText(components));
        }

        @Override
        public void sendMessage(UUID sender, net.md_5.bungee.api.chat.BaseComponent component) {
            sendMessage(component);
        }

        @Override
        public void sendMessage(UUID sender, net.md_5.bungee.api.chat.BaseComponent... components) {
            sendMessage(components);
        }
    };

    @Override
    public boolean isPermissionSet(@NotNull String name) {
        return this.perm.isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(@NotNull Permission permission) {
        return this.perm.isPermissionSet(permission);
    }

    @Override
    public boolean hasPermission(@NotNull String name) {
        return this.perm.hasPermission(name);
    }

    @Override
    public boolean hasPermission(@NotNull Permission permission) {
        return this.perm.hasPermission(permission);
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value) {
        return this.perm.addAttachment(plugin, name, value);
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin) {
        return this.perm.addAttachment(plugin);
    }

    @Override
    public @Nullable PermissionAttachment addAttachment(
            @NotNull Plugin plugin, @NotNull String name, boolean value, int ticks) {
        return this.perm.addAttachment(plugin, name, value, ticks);
    }

    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, int ticks) {
        return this.perm.addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment(@NotNull PermissionAttachment attachment) {
        this.perm.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        this.perm.recalculatePermissions();
    }

    @Override
    public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return this.perm.getEffectivePermissions();
    }

    @Override
    public void sendMessage(@Nullable UUID sender, @NotNull String message) {
        this.sendMessage(message);
    }

    @Override
    public void sendMessage(@Nullable UUID sender, @NotNull String... messages) {
        this.sendMessage(messages);
    }

    @Override
    public CommandSender.@NotNull Spigot spigot() {
        return this.spigot;
    }

    @Override
    public @NotNull net.kyori.adventure.text.Component name() {
        return net.kyori.adventure.text.Component.text(this.getName());
    }

    public abstract @NotNull Server getServer();

    public boolean isPlayer() {
        return false;
    }
}
