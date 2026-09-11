package io.lunararcdevs.lunararc.common.server;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.lunararcdevs.lunararc.common.bridge.access.CommandNodeAccessBridge;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandRegistrationFlag;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Paper 1.21.1 command registrar backed directly by Minecraft's actual
 * Brigadier dispatcher. The NMS CommandSourceStack implements Paper's API via
 * a mixin, so no wrapper/proxy or platform dispatch layer is involved.
 */
public final class LunarArcPaperCommands implements Commands {
    private final CommandDispatcher<net.minecraft.commands.CommandSourceStack> nmsDispatcher;

    public LunarArcPaperCommands(CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher) {
        this.nmsDispatcher = Objects.requireNonNull(dispatcher, "dispatcher");
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public CommandDispatcher<CommandSourceStack> getDispatcher() {
        return (CommandDispatcher) this.nmsDispatcher;
    }

    @Override
    public Set<String> register(LiteralCommandNode<CommandSourceStack> node, String description,
                                Collection<String> aliases) {
        LifecycleEventOwner owner = LunarArcLifecycleEventRunner.currentOwner();
        if (owner == null) {
            throw new IllegalStateException("Paper command registration requires an active lifecycle owner");
        }
        return register(owner.getPluginMeta(), node, description, aliases);
    }

    @Override
    public Set<String> register(PluginMeta pluginMeta, LiteralCommandNode<CommandSourceStack> node,
                                String description, Collection<String> aliases) {
        return registerWithFlags(pluginMeta, node, description, aliases, Set.of());
    }

    @Override
    public Set<String> registerWithFlags(PluginMeta pluginMeta, LiteralCommandNode<CommandSourceStack> node,
                                         String description, Collection<String> aliases,
                                         Set<CommandRegistrationFlag> flags) {
        Objects.requireNonNull(pluginMeta, "pluginMeta");
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(aliases, "aliases");
        Objects.requireNonNull(flags, "flags");

        String namespace = normalize(pluginMeta.getName());
        String literal = normalize(node.getLiteral());
        if (literal.isEmpty()) throw new IllegalArgumentException("Command literal cannot be empty");

        Set<String> labels = new HashSet<>();

        // Paper makes the plugin-namespaced root canonical, then exposes the
        // unqualified label as an overriding redirect.
        LiteralCommandNode<CommandSourceStack> canonical = redirectNode(namespace + ":" + literal, node, false);
        if (put(canonical, true)) labels.add(canonical.getLiteral());
        if (put(redirectNode(literal, canonical, flags.contains(CommandRegistrationFlag.FLATTEN_ALIASES)), true)) {
            labels.add(literal);
        }

        for (String rawAlias : aliases) {
            String alias = normalize(rawAlias);
            if (alias.isEmpty()) continue;
            if (put(redirectNode(alias, canonical, flags.contains(CommandRegistrationFlag.FLATTEN_ALIASES)), false)) {
                labels.add(alias);
            }
            String namespacedAlias = namespace + ":" + alias;
            if (put(redirectNode(namespacedAlias, canonical, flags.contains(CommandRegistrationFlag.FLATTEN_ALIASES)), false)) {
                labels.add(namespacedAlias);
            }
        }

        return labels.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(labels);
    }

    @Override
    public Set<String> register(String label, String description, Collection<String> aliases, BasicCommand basicCommand) {
        LifecycleEventOwner owner = LunarArcLifecycleEventRunner.currentOwner();
        if (owner == null) {
            throw new IllegalStateException("Paper command registration requires an active lifecycle owner");
        }
        return register(owner.getPluginMeta(), label, description, aliases, basicCommand);
    }

    @Override
    public Set<String> register(PluginMeta pluginMeta, String label, String description,
                                Collection<String> aliases, BasicCommand basicCommand) {
        Objects.requireNonNull(basicCommand, "basicCommand");
        String literal = normalize(label);
        if (literal.isEmpty()) throw new IllegalArgumentException("Command label cannot be empty");

        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(literal)
                .requires(source -> basicCommand.canUse(source.getSender()))
                .then(Commands.argument("args", StringArgumentType.greedyString())
                        .suggests((context, suggestions) -> suggest(basicCommand, context.getSource(), suggestions))
                        .executes(context -> {
                            String value = context.getArgument("args", String.class);
                            basicCommand.execute(context.getSource(), splitArgs(value));
                            return Command.SINGLE_SUCCESS;
                        }))
                .executes(context -> {
                    basicCommand.execute(context.getSource(), new String[0]);
                    return Command.SINGLE_SUCCESS;
                });

        return register(pluginMeta, builder.build(), description, aliases);
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggest(
            BasicCommand command, CommandSourceStack source, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining();
        String[] args = splitArgsPreservingTrailing(remaining);
        SuggestionsBuilder offset = builder.createOffset(builder.getInput().lastIndexOf(' ') + 1);
        Collection<String> suggestions = command.suggest(source, args);
        if (suggestions != null) suggestions.forEach(offset::suggest);
        return offset.buildFuture();
    }

    private static String[] splitArgs(String input) {
        if (input == null || input.isBlank()) return new String[0];
        return input.trim().split("\\s+");
    }

    private static String[] splitArgsPreservingTrailing(String input) {
        if (input == null || input.isEmpty()) return new String[]{""};
        boolean trailing = Character.isWhitespace(input.charAt(input.length() - 1));
        String trimmed = input.trim();
        String[] base = trimmed.isEmpty() ? new String[0] : trimmed.split("\\s+");
        if (!trailing) return base;
        String[] result = java.util.Arrays.copyOf(base, base.length + 1);
        result[result.length - 1] = "";
        return result;
    }

    private static LiteralCommandNode<CommandSourceStack> redirectNode(
            String literal, LiteralCommandNode<CommandSourceStack> target, boolean flatten) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(literal)
                .requires(target.getRequirement());
        if (target.getCommand() != null) builder.executes(target.getCommand());

        LiteralCommandNode<CommandSourceStack> redirect;
        if (!flatten && !target.getChildren().isEmpty()) {
            builder.redirect(target);
            redirect = builder.build();
        } else {
            redirect = builder.build();
            for (CommandNode<CommandSourceStack> child : target.getChildren()) {
                redirect.addChild(child);
            }
        }
        return redirect;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean put(LiteralCommandNode<CommandSourceStack> apiNode, boolean override) {
        LiteralCommandNode<net.minecraft.commands.CommandSourceStack> node =
                (LiteralCommandNode) apiNode;
        CommandNode<net.minecraft.commands.CommandSourceStack> root = this.nmsDispatcher.getRoot();
        CommandNode<net.minecraft.commands.CommandSourceStack> existing = root.getChild(node.getLiteral());
        if (existing != null && !override) return false;
        if (existing != null) {
            CommandNodeAccessBridge<net.minecraft.commands.CommandSourceStack> accessor =
                    (CommandNodeAccessBridge<net.minecraft.commands.CommandSourceStack>) (Object) root;
            accessor.lunararc$getChildren().remove(node.getLiteral());
            accessor.lunararc$getLiterals().remove(node.getLiteral());
            accessor.lunararc$getArguments().remove(node.getLiteral());
        }
        root.addChild(node);
        return true;
    }

    private static String normalize(String value) {
        if (value == null) return "";
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
