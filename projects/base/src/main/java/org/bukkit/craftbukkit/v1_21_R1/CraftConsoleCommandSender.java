package org.bukkit.craftbukkit.v1_21_R1;

import net.minecraft.server.MinecraftServer;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class CraftConsoleCommandSender implements ConsoleCommandSender {
    private final MinecraftServer server;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("Console");

    public CraftConsoleCommandSender(MinecraftServer server) {
        this.server = server;
    }

    public static org.bukkit.command.CommandSender fromSource(net.minecraft.commands.CommandSourceStack source) {
        if (source.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            return new CraftPlayer((org.bukkit.craftbukkit.v1_21_R1.CraftServer) org.bukkit.Bukkit.getServer(), player);
        }
        return new CraftConsoleCommandSender(source.getServer());
    }

    @Override
    public void sendMessage(@NotNull String message) {
        logger.info(translate(message));
    }

    private String translate(String text) {
        if (text == null)
            return null;

        // Handle hex colors: <#RRGGBB> or &x&r&r&g&g&b&b or §x§r§r§g§g§b§b
        // For console, we just map them to the closest standard color or strip them if not supported
        // But for now, we just handle the standard § codes
        
        return text.replace("§0", "\u001B[30m") // Black
                .replace("§1", "\u001B[34m") // Dark Blue
                .replace("§2", "\u001B[32m") // Dark Green
                .replace("§3", "\u001B[36m") // Dark Aqua
                .replace("§4", "\u001B[31m") // Dark Red
                .replace("§5", "\u001B[35m") // Dark Purple
                .replace("§6", "\u001B[33m") // Gold
                .replace("§7", "\u001B[37m") // Gray
                .replace("§8", "\u001B[90m") // Dark Gray
                .replace("§9", "\u001B[94m") // Blue
                .replace("§a", "\u001B[92m") // Green
                .replace("§b", "\u001B[96m") // Aqua
                .replace("§c", "\u001B[91m") // Red
                .replace("§d", "\u001B[95m") // Light Purple
                .replace("§e", "\u001B[93m") // Yellow
                .replace("§f", "\u001B[97m") // White
                .replace("§k", "") // Obfuscated (Skip)
                .replace("§l", "\u001B[1m") // Bold
                .replace("§m", "\u001B[9m") // Strikethrough
                .replace("§n", "\u001B[4m") // Underline
                .replace("§o", "\u001B[3m") // Italic
                .replace("§r", "\u001B[0m") // Reset
                + "\u001B[0m"; // Force reset at end
    }

    @Override
    public void sendMessage(@NotNull String... messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }

    @Override
    public void sendMessage(@Nullable UUID sender, @NotNull String message) {
        sendMessage(message);
    }

    @Override
    public void sendMessage(@Nullable UUID sender, @NotNull String... messages) {
        sendMessage(messages);
    }

    @Override
    public @NotNull Server getServer() {
        return org.bukkit.Bukkit.getServer();
    }

    @Override
    public @NotNull String getName() {
        return "CONSOLE";
    }

    @Override
    public @NotNull net.kyori.adventure.text.Component name() {
        return net.kyori.adventure.text.Component.text(getName());
    }

    @Override
    public @NotNull Spigot spigot() {
        return new Spigot();
    }

    @Override
    public boolean isPermissionSet(@NotNull String name) {
        return true;
    }

    @Override
    public boolean isPermissionSet(@NotNull Permission perm) {
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull String name) {
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull Permission perm) {
        return true;
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value) {
        return null;
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin) {
        return null;
    }

    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value,
            int ticks) {
        return null;
    }

    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, int ticks) {
        return null;
    }

    @Override
    public void removeAttachment(@NotNull PermissionAttachment attachment) {
    }

    @Override
    public void recalculatePermissions() {
    }

    @Override
    public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return Collections.emptySet();
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean value) {
    }

    @Override
    public void abandonConversation(@NotNull Conversation conversation) {
    }

    @Override
    public void abandonConversation(@NotNull Conversation conversation,
            @NotNull ConversationAbandonedEvent abandonedEvent) {
    }

    @Override
    public void acceptConversationInput(@NotNull String input) {
    }

    @Override
    public boolean beginConversation(@NotNull Conversation conversation) {
        return false;
    }

    @Override
    public boolean isConversing() {
        return false;
    }

    @Override
    public void sendRawMessage(@NotNull String message) {
        sendMessage(message);
    }

    @Override
    public void sendRawMessage(@Nullable UUID sender, @NotNull String message) {
        sendMessage(message);
    }
}
