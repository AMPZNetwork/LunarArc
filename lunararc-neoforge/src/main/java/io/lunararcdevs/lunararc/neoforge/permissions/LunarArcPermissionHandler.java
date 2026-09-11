package io.lunararcdevs.lunararc.neoforge.permissions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.permission.handler.IPermissionHandler;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;
import net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContext;
import io.lunararcdevs.lunararc.common.permission.LunarArcBukkitPermissions;
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
            var explicit = LunarArcBukkitPermissions.explicitOnlinePermission(player.getUUID(), node.getNodeName());
            if (explicit.isPresent()) {
                @SuppressWarnings("unchecked")
                T resolved = (T) explicit.get();
                return resolved;
            }
        }

        return node.getDefaultResolver().resolve(player, player.getUUID(), context);
    }

    @Override
    public <T> T getOfflinePermission(@NotNull UUID playerUUID, @NotNull PermissionNode<T> node, PermissionDynamicContext<?>... context) {
        return node.getDefaultResolver().resolve(null, playerUUID, context);
    }

    @Override
    public @NotNull Set<PermissionNode<?>> getRegisteredNodes() {
        return registeredNodes;
    }
}
