package io.lunararcdevs.lunararc.common.server;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.VersionCommand;
import org.jetbrains.annotations.NotNull;


public final class LunarArcVersionCommand extends VersionCommand {
    public LunarArcVersionCommand(String name) {
        super(name);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String currentAlias, @NotNull String[] args) {
        return super.execute(sender, currentAlias, args);
    }
}
