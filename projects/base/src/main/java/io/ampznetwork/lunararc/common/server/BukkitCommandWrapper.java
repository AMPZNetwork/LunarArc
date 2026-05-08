package io.ampznetwork.lunararc.common.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.ampznetwork.lunararc.common.LunarArcPlatform;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("all")
public class BukkitCommandWrapper {
    private final Command command;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("LunarArc");

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

    private void registerLiteral(CommandDispatcher<CommandSourceStack> dispatcher, @NotNull String label) {
        dispatcher.register(
                Commands.literal(label)
                        .requires(stack -> {
                            CommandSender sender = getSender(stack);
                            boolean hasPerm = command.testPermissionSilent(sender);
                            if (!hasPerm) {
                                logger.debug("[LunarArc] Command '" + label + "' permission check FAILED for " + sender.getName());
                            }
                            return hasPerm;
                        })
                        .then(Commands.argument("args", StringArgumentType.greedyString())
                                .suggests(this::getSuggestions)
                                .executes(ctx -> execute(ctx, label)))
                        .executes(ctx -> execute(ctx, label)));
    }

    private CommandSender getSender(CommandSourceStack stack) {
        if (stack.getEntity() instanceof ServerPlayer player) {
            org.bukkit.entity.Entity bukkitEntity = ((io.ampznetwork.lunararc.common.bridge.EntityBridge) player).lunararc$getBukkitEntity();
            if (bukkitEntity instanceof CommandSender sender) {
                return sender;
            }
            // Fallback for early command registration or unbridged entities
            return new CraftPlayer((org.bukkit.craftbukkit.v1_21_R1.CraftServer) org.bukkit.Bukkit.getServer(), player);
        }
        return LunarArcPlatform.getServer().getConsoleSender();
    }

    private int execute(CommandContext<CommandSourceStack> context, String label) {
        CommandSender sender = getSender(context.getSource());
        String args = "";
        try {
            args = StringArgumentType.getString(context, "args");
        } catch (IllegalArgumentException ignored) {
        }

        String[] splitArgs = args.isEmpty() ? new String[0] : args.split(" ");
        
        logger.info("[LunarArc] Executing command '" + label + "' (original: '" + command.getName() + "') for " + sender.getName() + " with args: [" + String.join(", ", splitArgs) + "]");
        
        try {
            boolean success = command.execute(sender, label, splitArgs);
            logger.info("[LunarArc] Command '" + label + "' execution returned: " + success);
        } catch (Throwable t) {
            logger.error("[LunarArc] Exception executing command '" + label + "'", t);
        }
        return 1;
    }


    private CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder) {
        CommandSender sender = getSender(context.getSource());
        String buffer = context.getInput();
        if (buffer.startsWith("/")) {
            buffer = buffer.substring(1);
        }

        String[] split = buffer.split(" ", -1);
        String label = split[0];
        String[] args = new String[split.length - 1];
        System.arraycopy(split, 1, args, 0, args.length);

        java.util.List<String> suggestions = command.tabComplete(sender, label, args);
        if (suggestions == null) {
            // Fallback to online players if no suggestions provided
            suggestions = new java.util.ArrayList<>();
            for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(split[split.length - 1].toLowerCase())) {
                    suggestions.add(p.getName());
                }
            }
        }

        if (suggestions != null) {
            String lastArg = split[split.length - 1];
            builder = builder.createOffset(context.getInput().length() - lastArg.length());
            for (String s : suggestions) {
                builder.suggest(s);
            }
        }
        return builder.buildFuture();
    }
}
