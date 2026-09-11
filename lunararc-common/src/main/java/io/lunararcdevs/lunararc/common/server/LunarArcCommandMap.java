package io.lunararcdevs.lunararc.common.server;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;


public class LunarArcCommandMap extends SimpleCommandMap {
    private static volatile CommandDispatcher<CommandSourceStack> dispatcher;
    private final Server lunararc$server;
    private Set<String> lunararc$mirroredLabels;
    private CommandDispatcher<CommandSourceStack> lunararc$mirroredDispatcher;

    public LunarArcCommandMap(Server server) {
        super(server, new HashMap<>());
        this.lunararc$server = java.util.Objects.requireNonNull(server, "server");
    }

    public static void setDispatcher(CommandDispatcher<CommandSourceStack> value) {
        dispatcher = value;
    }

    public static CommandDispatcher<CommandSourceStack> getDispatcher() {
        return dispatcher;
    }

    @Override
    public boolean register(String label, String fallbackPrefix, Command command) {
        boolean result = super.register(label, fallbackPrefix, command);
        syncCommand(command, label, fallbackPrefix);
        syncCommandTreeToPlayers();
        return result;
    }

    public void unregisterPlugin(Plugin plugin) {
        if (plugin == null) return;
        Set<Command> removed = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        Set<String> removedLabels = new LinkedHashSet<>();
        getKnownCommands().entrySet().removeIf(entry -> {
            Command command = entry.getValue();
            if (command instanceof PluginCommand pluginCommand && pluginCommand.getPlugin() == plugin) {
                removed.add(command);
                removedLabels.add(normalize(entry.getKey()));
                return true;
            }
            return false;
        });
        for (Command command : removed) command.unregister(this);
        for (String label : removedLabels) removeMirrorIfUnused(label);
        syncCommandTreeToPlayers();
    }

    @Override
    public void clearCommands() {
        super.clearCommands();
        clearMirrors();
        syncCommandTreeToPlayers();
    }


    public boolean reloadServerAliases(java.util.Map<String, String[]> previousAliases,
                                       java.util.Map<String, String[]> aliases) {
        java.util.Objects.requireNonNull(previousAliases, "previousAliases");
        java.util.Objects.requireNonNull(aliases, "aliases");

        java.util.Set<String> previous = new java.util.LinkedHashSet<>();
        for (String alias : previousAliases.keySet()) {
            String normalized = normalize(alias);
            if (!normalized.isEmpty()) previous.add(normalized);
        }

        if (!previous.isEmpty()) {
            getKnownCommands().entrySet().removeIf(entry -> previous.contains(normalize(entry.getKey())));
            CommandDispatcher<CommandSourceStack> target = dispatcher;
            if (target != null && target.getRoot() instanceof io.lunararcdevs.lunararc.common.bridge.access.CommandNodeAccessBridge accessor) {
                for (String alias : previous) {
                    accessor.lunararc$getChildren().remove(alias);
                    accessor.lunararc$getLiterals().remove(alias);
                    accessor.lunararc$getArguments().remove(alias);
                }
            }
        }

        boolean registered = true;
        for (java.util.Map.Entry<String, String[]> entry : aliases.entrySet()) {
            String alias = normalize(entry.getKey());
            String[] replacements = entry.getValue();
            if (alias.isEmpty() || replacements == null || replacements.length == 0) continue;
            registered &= this.register(alias, "bukkit", new org.bukkit.command.FormattedCommandAlias(alias, replacements.clone()));
        }
        syncCommandTreeToPlayers();
        return registered;
    }

    public void syncToBrigadier(CommandDispatcher<CommandSourceStack> target) {
        if (target == null) return;
        dispatcher = target;
        if (lunararc$mirroredDispatcher != target) {
            mirroredLabels().clear();
            lunararc$mirroredDispatcher = target;
        }

        Set<String> labels = new LinkedHashSet<>(getKnownCommands().keySet());
        for (String knownLabel : labels) {
            registerMirror(target, knownLabel);
        }
    }

    private void syncCommand(Command command, String requestedLabel, String fallbackPrefix) {
        CommandDispatcher<CommandSourceStack> target = dispatcher;
        if (target == null) return;

        Set<String> candidates = new LinkedHashSet<>();
        candidates.add(normalize(requestedLabel));
        candidates.add(normalize(command.getName()));
        for (String alias : command.getAliases()) candidates.add(normalize(alias));

        String prefix = normalize(fallbackPrefix);
        if (!prefix.isEmpty()) {
            candidates.add(prefix + ":" + normalize(requestedLabel));
            candidates.add(prefix + ":" + normalize(command.getName()));
            for (String alias : command.getAliases()) {
                candidates.add(prefix + ":" + normalize(alias));
            }
        }


        for (String candidate : candidates) {
            if (!candidate.isEmpty() && getKnownCommands().containsKey(candidate)) {
                registerMirror(target, candidate);
            }
        }
    }

    private void registerMirror(CommandDispatcher<CommandSourceStack> target, String label) {
        String normalized = normalize(label);
        if (normalized.isEmpty() || getCommand(normalized) == null) return;
        if (lunararc$mirroredDispatcher != target) {
            mirroredLabels().clear();
            lunararc$mirroredDispatcher = target;
        }

        if (target.getRoot().getChild(normalized) != null) {
            if (!mirroredLabels().contains(normalized)) return;
            removeMirror(target, normalized);
        }

        new BukkitCommandWrapper(this, normalized).register(target);
        if (target.getRoot().getChild(normalized) != null) mirroredLabels().add(normalized);
    }

    private void removeMirrorIfUnused(String label) {
        if (getCommand(label) != null) return;
        CommandDispatcher<CommandSourceStack> target = dispatcher;
        if (target != null && target == lunararc$mirroredDispatcher && mirroredLabels().contains(label)) {
            removeMirror(target, label);
        }
    }

    private void clearMirrors() {
        CommandDispatcher<CommandSourceStack> target = dispatcher;
        if (target != null && target == lunararc$mirroredDispatcher) {
            for (String label : Set.copyOf(mirroredLabels())) removeMirror(target, label);
        }
        mirroredLabels().clear();
    }

    private void removeMirror(CommandDispatcher<CommandSourceStack> target, String label) {
        if (target.getRoot() instanceof io.lunararcdevs.lunararc.common.bridge.access.CommandNodeAccessBridge accessor) {
            accessor.lunararc$getChildren().remove(label);
            accessor.lunararc$getLiterals().remove(label);
            accessor.lunararc$getArguments().remove(label);
        }
        mirroredLabels().remove(label);
    }

    private Set<String> mirroredLabels() {
        if (lunararc$mirroredLabels == null) lunararc$mirroredLabels = new LinkedHashSet<>();
        return lunararc$mirroredLabels;
    }

    private void syncCommandTreeToPlayers() {
        Server server = this.lunararc$server;
        if (server == null) {


            return;
        }
        for (org.bukkit.entity.Player player : server.getOnlinePlayers()) {
            player.updateCommands();
        }
    }

    private static String normalize(String value) {
        if (value == null) return "";


        return value.trim().toLowerCase(Locale.ROOT);
    }
}
