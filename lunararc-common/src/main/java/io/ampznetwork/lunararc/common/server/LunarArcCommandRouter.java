package io.ampznetwork.lunararc.common.server;

import net.minecraft.commands.CommandSourceStack;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Locale;
import java.util.logging.Level;


public final class LunarArcCommandRouter {

    @FunctionalInterface
    public interface PlatformCommandHook {
        HookResult apply(CraftServer server, CommandSender sender, String commandLine);
    }

    public record HookResult(boolean cancelled, String commandLine) {
        public static HookResult pass(String commandLine) {
            return new HookResult(false, commandLine);
        }

        public static HookResult cancel() {
            return new HookResult(true, "");
        }
    }

    private static volatile PlatformCommandHook platformCommandHook = (server, sender, line) -> HookResult.pass(line);

    private LunarArcCommandRouter() {
    }

    public static void installPlatformCommandHook(PlatformCommandHook hook) {
        platformCommandHook = hook == null ? (server, sender, line) -> HookResult.pass(line) : hook;
    }

    public enum PacketResult {

        PASS,

        CANCEL
    }

    public static PacketResult routePlayerPacket(Server server, Player player, String packetCommand) {
        if (server == null || player == null || packetCommand == null) {
            return PacketResult.PASS;
        }


        String originalEventMessage = "/" + packetCommand;
        PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, originalEventMessage);
        server.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            LunarArcCommandLogger.markCancelled();
            return PacketResult.CANCEL;
        }

        String eventMessage = event.getMessage();
        if (eventMessage == null) {
            return PacketResult.CANCEL;
        }

        String routedLine = removeEventIntroducer(eventMessage);
        if (routedLine.isEmpty()) {
            return PacketResult.CANCEL;
        }

        boolean modified = !eventMessage.equals(originalEventMessage);
        CommandMap commandMap = server.getCommandMap();
        String label = labelOf(routedLine);
        Command command = commandMap.getCommand(label);

        if (command != null) {


            commandMap.dispatch(player, routedLine);
            return PacketResult.CANCEL;
        }

        if (modified) {


            dispatch(server, player, routedLine);
            return PacketResult.CANCEL;
        }


        return PacketResult.PASS;
    }


    public static boolean dispatch(Server server, CommandSender sender, String commandLine) {
        if (server == null || sender == null || commandLine == null) return false;

        String line = commandLine.trim();
        if (line.isEmpty()) return false;

        boolean startedSession = false;
        if (sender instanceof Player player && !LunarArcCommandLogger.isSessionActive(player.getUniqueId())) {
            LunarArcCommandLogger.begin(player.getUniqueId(), player.getName(), line);
            startedSession = true;
        }

        try {
            if (server instanceof CraftServer craftServer) {
                HookResult hookResult = platformCommandHook.apply(craftServer, sender, line);
                if (hookResult == null || hookResult.cancelled()) return false;
                line = hookResult.commandLine() == null ? "" : hookResult.commandLine().trim();
                if (line.isEmpty()) return false;
            }

            CommandMap map = server.getCommandMap();


            String exactLabel = labelOf(line);
            if (map.getCommand(exactLabel) != null) {
                return map.dispatch(sender, line);
            }


            if (line.startsWith("/")) {
                line = line.substring(1).trim();
                if (line.isEmpty()) return false;
            }

            String label = labelOf(line);
            if (map.getCommand(label) != null) {
                return map.dispatch(sender, line);
            }

            return dispatchNative(server, sender, line);
        } finally {
            if (startedSession) {
                LunarArcCommandLogger.end();
            }
        }
    }

    /**
     * Run a line no Bukkit command claimed, the way Minecraft itself runs it.
     *
     * <p>This used to call {@code getDispatcher().execute(line, source)} - brigadier's own entry
     * point, which is not the one Minecraft uses. Since 1.20.2 a command goes through
     * {@link net.minecraft.commands.Commands#performPrefixedCommand}, which parses into a
     * {@code ContextChain} and runs it on an ExecutionContext: that is what gives /execute its
     * chaining and fan-out, what bounds /function recursion, and what produces the "Unknown
     * command" message with the caret under the offending character. Brigadier's execute skips all
     * of it and reports a failure as a bare exception message.</p>
     *
     * <p>CraftBukkit reaches the same method by a different road - every vanilla command is in its
     * command map as a VanillaCommandWrapper, whose execute calls performPrefixedCommand. LunarArc
     * has no such wrapper yet, so this fallback stands in for it, and it should call what the
     * wrapper calls.</p>
     *
     * <p>The return value still says whether the label was a command at all, because callers use
     * it to decide whether anything handled the line; the reporting of a bad one is left to
     * Minecraft, which does it better than a rethrown parse error.</p>
     */
    public static boolean dispatchNative(Server server, CommandSender sender, String line) {
        if (!(server instanceof CraftServer craftServer)) return false;
        try {
            net.minecraft.commands.Commands commands = craftServer.getServer().getCommands();
            boolean known = commands.getDispatcher().getRoot().getChild(rawLabelOf(line)) != null;
            commands.performPrefixedCommand(source(craftServer, sender), line);
            return known;
        } catch (Throwable error) {
            server.getLogger().log(Level.SEVERE, "Error dispatching command: " + line, error);
            return false;
        }
    }

    private static CommandSourceStack source(CraftServer server, CommandSender sender) {
        if (sender instanceof CraftPlayer player) {
            return player.getHandle().createCommandSourceStack();
        }
        if (sender == server.getConsoleSender()) {
            return server.getServer().createCommandSourceStack();
        }
        if (sender instanceof org.bukkit.craftbukkit.entity.CraftEntity entity) {
            return entity.getHandle().createCommandSourceStack();
        }
        throw new IllegalArgumentException("Unsupported native command sender: " + sender.getClass().getName());
    }


    static String removeEventIntroducer(String eventMessage) {
        String line = eventMessage.trim();
        if (line.startsWith("/")) {
            line = line.substring(1);
        }
        return line.trim();
    }

    /**
     * The label exactly as typed. Brigadier's command tree is case-sensitive, so the lower-cased
     * label {@link #labelOf} produces - right for the Bukkit command map, which is not - would miss
     * every node when asked of the dispatcher.
     */
    static String rawLabelOf(String line) {
        String trimmed = line == null ? "" : line.trim();
        if (trimmed.startsWith("/")) trimmed = trimmed.substring(1).trim();
        if (trimmed.isEmpty()) return "";
        int space = trimmed.indexOf(' ');
        return space >= 0 ? trimmed.substring(0, space) : trimmed;
    }

    static String labelOf(String line) {
        String trimmed = line == null ? "" : line.trim();
        if (trimmed.isEmpty()) return "";
        int space = trimmed.indexOf(' ');
        String label = space >= 0 ? trimmed.substring(0, space) : trimmed;
        return label.toLowerCase(Locale.ROOT);
    }
}
