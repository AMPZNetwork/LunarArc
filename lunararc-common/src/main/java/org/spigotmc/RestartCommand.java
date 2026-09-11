package org.spigotmc;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Locale;

public class RestartCommand extends Command {

    public RestartCommand(String name) {
        super(name);
        this.description = "Restarts the server";
        this.usageMessage = "/restart";
        this.setPermission("bukkit.command.restart");
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!this.testPermission(sender)) return true;

        org.bukkit.Server server = Bukkit.getServer();
        if (server instanceof org.bukkit.craftbukkit.CraftServer craftServer) {
            // Onto the server thread: the console and RCON both dispatch off it, and disconnecting
            // players from another thread is exactly what AsyncCatcher exists to refuse.
            craftServer.getServer().execute(() -> restart(sender));
        } else {
            restart(sender);
        }
        return true;
    }

    /** Restart using the script named by {@code settings.restart-script} in spigot.yml. */
    public static void restart() {
        restart(null);
    }

    private static void restart(CommandSender sender) {
        String restartScript = SpigotConfig.restartScript;
        boolean scripted = addShutdownHook(restartScript);

        if (scripted) {
            announce(sender, "Restarting with " + restartScript);
        } else {
            announce(sender, "Startup script '" + restartScript + "' does not exist, so the server"
                    + " will stop rather than restart. Set settings.restart-script in spigot.yml to"
                    + " the script that starts LunarArc.");
        }

        // kick(Component), not the String kickPlayer(...) it deprecates.
        net.kyori.adventure.text.Component message =
                net.kyori.adventure.text.Component.text(SpigotConfig.restartMessage);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kick(message);
        }

        Bukkit.shutdown();
    }

    private static void announce(CommandSender sender, String message) {
        if (sender != null) sender.sendMessage(message);
        Bukkit.getLogger().info(message);
    }

    /**
     * Register a shutdown hook that runs {@code restartScript}, reporting whether there was one to
     * run. Paper's own helper of the same name and shape, used by the watchdog as well as here.
     */
    public static boolean addShutdownHook(String restartScript) {
        if (restartScript == null) return false;
        String[] split = restartScript.split(" ");
        if (split.length == 0 || !new File(split[0]).isFile()) return false;

        java.util.List<String> command = new java.util.ArrayList<>();
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH);
        if (os.contains("win")) {
            // start with an empty title: the first quoted argument to start is read as the window
            // title, so a script path arriving in that position would be swallowed.
            command.add("cmd");
            command.add("/c");
            command.add("start");
            command.add("");
        } else {
            command.add("sh");
        }
        java.util.Collections.addAll(command, split);

        Thread shutdownHook = new Thread(() -> {
            try {
                // ProcessBuilder rather than Runtime.exec(String), which Java deprecated in favour
                // of it; the token split above is the one Spigot already does to find the script.
                new ProcessBuilder(command).start();
            } catch (Exception ex) {
                Bukkit.getLogger().log(java.util.logging.Level.SEVERE,
                        "Could not start the restart script " + restartScript, ex);
            }
        }, "LunarArc Restart Hook");

        shutdownHook.setDaemon(true);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        return true;
    }
}
