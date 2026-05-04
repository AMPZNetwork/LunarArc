package io.ampznetwork.lunararc.neoforge.permissions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.handler.IPermissionHandler;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;
import net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContext;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;

public class LunarArcPermissionHandler implements IPermissionHandler {
    private final Set<PermissionNode<?>> registeredNodes;

    public LunarArcPermissionHandler(Collection<PermissionNode<?>> registeredNodes) {
        this.registeredNodes = Collections.unmodifiableSet(new HashSet<>(registeredNodes));
    }

    @Override
    public @NotNull ResourceLocation getIdentifier() {
        return ResourceLocation.fromNamespaceAndPath("lunararc", "bukkit");
    }

    @Override
    public <T> T getPermission(@NotNull ServerPlayer player, @NotNull PermissionNode<T> node, PermissionDynamicContext<?>... context) {
        if (node.getType() == PermissionTypes.BOOLEAN) {
            Player bukkitPlayer = Bukkit.getPlayer(player.getUUID());
            if (bukkitPlayer != null) {
                return (T) Boolean.valueOf(bukkitPlayer.hasPermission(node.getNodeName()));
            }
        }
        // Fallback to node's default resolver or value
        return null;
    }

    @Override
    public <T> T getOfflinePermission(@NotNull UUID playerUUID, @NotNull PermissionNode<T> node, PermissionDynamicContext<?>... context) {
        if (node.getType() == PermissionTypes.BOOLEAN) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
            return (T) Boolean.valueOf(offlinePlayer.isOp());
        }
        return null;
    }

    @Override
    public @NotNull Set<PermissionNode<?>> getRegisteredNodes() {
        return registeredNodes;
    }
}
