package io.lunararcdevs.lunararc.common.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.lunararcdevs.lunararc.common.bridge.CommandSourceStackBridge;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;


public final class BukkitCommandWrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger("LunarArc");
    private final CommandMap commandMap;
    private final String label;

    public BukkitCommandWrapper(CommandMap commandMap, String label) {
        this.commandMap = commandMap;
        this.label = normalize(label);
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (label.isEmpty()) return;

        dispatcher.register(
                Commands.literal(label)
                        .requires(source -> canUse(source, label))
                        .then(Commands.argument("args", StringArgumentType.greedyString())
                                .suggests(this::suggest)
                                .executes(context -> execute(context, true)))
                        .executes(context -> execute(context, false)));
    }

    private boolean canUse(CommandSourceStack source, String commandLabel) {
        Command command = commandMap.getCommand(commandLabel);
        if (command == null) return false;
        try {
            return command.testPermissionSilent(sender(source));
        } catch (Throwable ex) {
            // This runs for every command node each time the tree is sent to a player, on the
            // join path. CraftBukkit calls testPermissionSilent unguarded, but it can afford to:
            // there a plugin's commands cannot outlive the plugin the way they can here. Letting a
            // misbehaving or half-unloaded plugin throw takes the server down on player join,
            // which is a far worse failure than hiding one command from one player.
            LOGGER.warn("Permission check for command '{}' threw; hiding it from this sender.",
                    commandLabel, ex);
            return false;
        }
    }

    private int execute(CommandContext<CommandSourceStack> context, boolean hasArguments) {
        String line = label;
        if (hasArguments) {
            String args = StringArgumentType.getString(context, "args");
            if (!args.isBlank()) line += " " + args;
        }


        return commandMap.dispatch(sender(context.getSource()), line) ? 1 : 0;
    }

    private CompletableFuture<Suggestions> suggest(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder) {
        CommandSender sender = sender(context.getSource());
        String input = context.getInput();
        Command command = commandMap.getCommand(label);

        List<String> completions;
        try {
            boolean hasArgumentInput = input.length() > label.length();
            String argumentText = hasArgumentInput ? input.substring(label.length()) : "";
            if (argumentText.startsWith(" ")) argumentText = argumentText.substring(1);
            String[] args = hasArgumentInput ? argumentText.split(" ", -1) : new String[0];
            completions = command == null ? List.of() : command.tabComplete(sender, label, args);
        } catch (org.bukkit.command.CommandException exception) {
            throw exception;
        } catch (Throwable throwable) {
            // A TabCompleter throwing anything else used to escape this method uncaught and
            // silently drop suggestions with no visible evidence in the log - log it so a broken
            // plugin completer surfaces as an obvious cause rather than "tab-complete just doesn't
            // work here".
            LOGGER.warn("TabCompleter for '{}' threw while completing '{}'", label, input, throwable);
            completions = List.of();
        }

        int lastSpace = input.lastIndexOf(' ');
        SuggestionsBuilder target = lastSpace >= 0
                ? builder.createOffset(lastSpace + 1)
                : builder;

        String remaining = target.getRemainingLowerCase();
        // A TabCompleter returning null is Bukkit's documented way of saying "no suggestions of
        // my own"; CraftBukkit treats it as an empty list. DecentHolograms' /dh returns null and
        // took the whole ServerboundCommandSuggestionPacket down with an NPE here.
        if (completions == null) return target.buildFuture();
        for (String completion : completions) {
            if (completion != null && completion.toLowerCase(java.util.Locale.ROOT).startsWith(remaining)) {
                target.suggest(completion);
            }
        }
        return target.buildFuture();
    }

    private static CommandSender sender(CommandSourceStack source) {
        return ((CommandSourceStackBridge) (Object) source).lunararc$getBukkitSender();
    }

    private static String normalize(String value) {
        if (value == null) return "";


        return value.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
