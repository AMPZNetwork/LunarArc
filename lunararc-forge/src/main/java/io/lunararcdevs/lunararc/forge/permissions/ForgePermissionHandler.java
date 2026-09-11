package io.lunararcdevs.lunararc.forge.permissions;

import io.lunararcdevs.lunararc.common.permission.LunarArcBukkitPermissions;
import java.util.UUID;

public final class ForgePermissionHandler {
    private ForgePermissionHandler() {}

    public static boolean hasPermission(UUID playerUUID, String permission) {
        return LunarArcBukkitPermissions.hasPermission(playerUUID, permission);
    }

    public static boolean hasPermission(UUID playerUUID, String permission, boolean defaultValue) {
        return LunarArcBukkitPermissions.hasPermission(playerUUID, permission, defaultValue);
    }
}
