
package org.bukkit.craftbukkit.v1_21_R1.entity;

import org.bukkit.craftbukkit.v1_21_R1.CraftServer;
import net.minecraft.world.entity.player.Player;
import org.bukkit.permissions.PermissibleBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import java.util.Set;

public abstract class CraftHumanEntity extends CraftEntity {
    protected final PermissibleBase perm = new PermissibleBase(this);

    public CraftHumanEntity(CraftServer server, Player entity) {
        super(server, entity);
    }

    @Override public boolean isPermissionSet(@NotNull String name) { return perm.isPermissionSet(name); }
    @Override public boolean isPermissionSet(@NotNull Permission permission) { return perm.isPermissionSet(permission); }
    @Override public boolean hasPermission(@NotNull String name) { return perm.hasPermission(name); }
    @Override public boolean hasPermission(@NotNull Permission permission) { return perm.hasPermission(permission); }
    @Override public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value) { return perm.addAttachment(plugin, name, value); }
    @Override public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin) { return perm.addAttachment(plugin); }
    @Override public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value, int ticks) { return perm.addAttachment(plugin, name, value, ticks); }
    @Override public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, int ticks) { return perm.addAttachment(plugin, ticks); }
    @Override public void removeAttachment(@NotNull PermissionAttachment attachment) { perm.removeAttachment(attachment); }
    @Override public void recalculatePermissions() { perm.recalculatePermissions(); }
    @Override public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() { return perm.getEffectivePermissions(); }
    @Override
    public boolean isOp() {
        try {
            Object playerList = server.getHandle().getPlayerList();
            return (boolean) playerList.getClass().getMethod("isOp", com.mojang.authlib.GameProfile.class)
                    .invoke(playerList, getHandle().getGameProfile());
        } catch (Exception e) {
            return getHandle().getAbilities().instabuild;
        }
    }

    @Override
    public void setOp(boolean value) {
        if (value == isOp()) return;
        try {
            Object playerList = server.getHandle().getPlayerList();
            if (value) {
                int opLevel = server.getHandle().getOperatorUserPermissionLevel();
                boolean bypass = (boolean) playerList.getClass().getMethod("canBypassPlayerLimit", com.mojang.authlib.GameProfile.class).invoke(playerList, getHandle().getGameProfile());
                Object entry = Class.forName("net.minecraft.server.players.ServerOpListEntry")
                        .getConstructor(com.mojang.authlib.GameProfile.class, int.class, boolean.class)
                        .newInstance(getHandle().getGameProfile(), opLevel, bypass);
                Object ops = playerList.getClass().getMethod("getOps").invoke(playerList);
                ops.getClass().getMethod("add", Class.forName("net.minecraft.server.players.StoredUserEntry")).invoke(ops, entry);
            } else {
                Object ops = playerList.getClass().getMethod("getOps").invoke(playerList);
                ops.getClass().getMethod("remove", Object.class).invoke(ops, getHandle().getGameProfile());
            }
        } catch (Exception e) {
             e.printStackTrace();
        }
        perm.recalculatePermissions();
    }

    public net.minecraft.world.entity.player.Player getHandle() {
        return (net.minecraft.world.entity.player.Player) entity;
    }
}
