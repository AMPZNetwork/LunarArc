package io.ampznetwork.lunararc.common.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.ampznetwork.lunararc.common.LunarArcPlatform;
import io.ampznetwork.lunararc.common.stubs.org.bukkit.craftbukkit.v1_21_R1.CraftPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.concurrent.CompletableFuture;

public class BukkitCommandWrapper {
    private final Command command;

    public BukkitCommandWrapper(Command command) {
        this.command = command;
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        String name = command.getName();
        registerLiteral(dispatcher, name);
        
        for (String alias : command.getAliases()) {
            registerLiteral(dispatcher, alias);
        }
    }

    private void registerLiteral(CommandDispatcher<CommandSourceStack> dispatcher, String label) {
        dispatcher.register(
            Commands.literal(label)
                .requires(stack -> {
                    CommandSender sender = getSender(stack);
                    return command.testPermissionSilent(sender);
                })
                .then(Commands.argument("args", StringArgumentType.greedyString())
                    .suggests(this::getSuggestions)
                    .executes(this::execute))
                .executes(this::execute)
        );
    }

    private CommandSender getSender(CommandSourceStack stack) {
        if (stack.getEntity() instanceof ServerPlayer player) {
            return new CraftPlayer(player);
        }
        return LunarArcPlatform.getServer().getConsoleSender();
    }

    private int execute(CommandContext<CommandSourceStack> context) {
        CommandSender sender = getSender(context.getSource());
        String args = "";
        try {
            args = StringArgumentType.getString(context, "args");
        } catch (IllegalArgumentException ignored) {}
        
        String[] splitArgs = args.isEmpty() ? new String[0] : args.split(" ");
        command.execute(sender, command.getName(), splitArgs);
        return 1;
    }

    private CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        CommandSender sender = getSender(context.getSource());
        String buffer = context.getInput();
        if (buffer.startsWith("/")) {
            buffer = buffer.substring(1);
        }
        
        java.util.List<String> suggestions = command.tabComplete(sender, command.getName(), buffer.split(" ", -1));
        if (suggestions != null) {
            for (String s : suggestions) {
                builder.suggest(s);
            }
        }
        return builder.buildFuture();
    }
}
