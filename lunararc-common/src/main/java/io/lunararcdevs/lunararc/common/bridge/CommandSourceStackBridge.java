package io.lunararcdevs.lunararc.common.bridge;

public interface CommandSourceStackBridge {
    org.bukkit.command.CommandSender lunararc$getBukkitSender();
    boolean lunararc$bypassSelectorPermissions();
    void lunararc$setBypassSelectorPermissions(boolean bypass);
}
