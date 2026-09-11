package org.bukkit.craftbukkit;

import net.minecraft.server.MinecraftServer;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CraftConsoleCommandSender extends org.bukkit.craftbukkit.command.ServerCommandSender implements ConsoleCommandSender {
    private final MinecraftServer server;
    private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger("Console");
    private static final String ANSI_RESET = "\u001B[0m";
    private static final boolean ANSI_ENABLED = detectAnsi();
    private static final Object CONSOLE_LOCK = new Object();
    private final org.bukkit.craftbukkit.conversations.ConversationTracker conversationTracker = new org.bukkit.craftbukkit.conversations.ConversationTracker();

    public CraftConsoleCommandSender(MinecraftServer server) {
        this.server = server;
    }

    public static org.bukkit.command.CommandSender fromSource(net.minecraft.commands.CommandSourceStack source) {
        if (source.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            return ((io.lunararcdevs.lunararc.common.bridge.EntityBridge) player).lunararc$getBukkitEntity();
        }
        return new CraftConsoleCommandSender(source.getServer());
    }

    @Override
    public void sendMessage(@NotNull String message) {
        writeConsole(message);
    }

    @Override
    public void sendMessage(net.kyori.adventure.text.Component message) {


        writeConsole(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                .legacySection().serialize(message));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void sendMessage(net.kyori.adventure.identity.Identity identity,
            net.kyori.adventure.text.Component message, net.kyori.adventure.audience.MessageType type) {
        sendMessage(message);
    }

    private static void writeConsole(String text) {
        if (text == null) {
            return;
        }

        // NeoForge's own log4j2.xml already renders standard section-sign legacy color codes
        // correctly per-appender: %minecraftFormatting converts them to ANSI for the live
        // TerminalConsole appender and strips them entirely (via its {strip} option) for the
        // File/DebugFile appenders. Baking raw ANSI into the message ourselves - as this used
        // to do for every code, not just hex ones - defeats that: {strip} only recognizes real
        // section-sign codes, so pre-baked ANSI bytes pass straight through into logs/latest.log
        // unrendered. Hex colors ("&x...", "&#RRGGBB") are the one thing that converter cannot
        // render at all, so those still need converting to ANSI here; everything else is left
        // as real section-sign codes for log4j's own converter to handle appropriately.
        String rendered = ANSI_ENABLED ? translateHexOnly(text) : stripLegacy(text);


        synchronized (CONSOLE_LOCK) {
            LOGGER.info(rendered);
        }
    }

    private static boolean detectAnsi() {
        String override = System.getProperty("lunararc.console.ansi");
        if (override != null) {
            return Boolean.parseBoolean(override);
        }
        if (System.getenv("NO_COLOR") != null) {
            return false;
        }
        String term = System.getenv("TERM");
        if (term != null && "dumb".equalsIgnoreCase(term)) {
            return false;
        }


        return true;
    }

    private static String translateHexOnly(String text) {
        StringBuilder out = new StringBuilder(text.length() + 32);
        boolean bakedAnsi = false;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if ((ch == '§' || ch == '&') && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(i + 1));


                if (code == 'x') {
                    String hex = readLegacyHex(text, i);
                    if (hex != null) {
                        int rgb = Integer.parseInt(hex, 16);
                        out.append("\u001B[38;2;")
                                .append((rgb >> 16) & 0xff).append(';')
                                .append((rgb >> 8) & 0xff).append(';')
                                .append(rgb & 0xff).append('m');
                        i += 13;
                        bakedAnsi = true;
                        continue;
                    }
                }


                if (code == '#' && i + 7 < text.length()) {
                    String hex = text.substring(i + 2, i + 8);
                    if (isHex(hex)) {
                        int rgb = Integer.parseInt(hex, 16);
                        out.append("\u001B[38;2;")
                                .append((rgb >> 16) & 0xff).append(';')
                                .append((rgb >> 8) & 0xff).append(';')
                                .append(rgb & 0xff).append('m');
                        i += 7;
                        bakedAnsi = true;
                        continue;
                    }
                }

                // Standard single-character codes are left as real section-sign codes -
                // NeoForge's log4j2.xml already renders or strips these correctly per-appender
                // via %minecraftFormatting, and pre-baking ANSI here would defeat that (see
                // writeConsole). '&' shorthand is normalized to the real marker so that
                // conversion still recognizes it.
                if (ansiFor(code) != null) {
                    out.append('§').append(code);
                    i++;
                    continue;
                }
            }
            out.append(ch);
        }
        if (bakedAnsi) {
            out.append(ANSI_RESET);
        }
        return out.toString();
    }

    private static String stripLegacy(String text) {
        StringBuilder out = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if ((ch == '§' || ch == '&') && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(i + 1));
                if (code == 'x' && readLegacyHex(text, i) != null) {
                    i += 13;
                    continue;
                }
                if (code == '#' && i + 7 < text.length() && isHex(text.substring(i + 2, i + 8))) {
                    i += 7;
                    continue;
                }
                if (ansiFor(code) != null) {
                    i++;
                    continue;
                }
            }
            out.append(ch);
        }
        return out.toString();
    }

    private static String readLegacyHex(String text, int start) {
        if (start + 13 >= text.length()) {
            return null;
        }
        StringBuilder hex = new StringBuilder(6);
        for (int n = 0; n < 6; n++) {
            int marker = start + 2 + (n * 2);
            int digit = marker + 1;
            char markerChar = text.charAt(marker);
            char digitChar = text.charAt(digit);
            if ((markerChar != '§' && markerChar != '&') || Character.digit(digitChar, 16) < 0) {
                return null;
            }
            hex.append(digitChar);
        }
        return hex.toString();
    }

    private static boolean isHex(String value) {
        if (value.length() != 6) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (Character.digit(value.charAt(i), 16) < 0) {
                return false;
            }
        }
        return true;
    }

    private static String ansiFor(char code) {
        return switch (code) {
            case '0' -> "\u001B[30m";
            case '1' -> "\u001B[34m";
            case '2' -> "\u001B[32m";
            case '3' -> "\u001B[36m";
            case '4' -> "\u001B[31m";
            case '5' -> "\u001B[35m";
            case '6' -> "\u001B[33m";
            case '7' -> "\u001B[37m";
            case '8' -> "\u001B[90m";
            case '9' -> "\u001B[94m";
            case 'a' -> "\u001B[92m";
            case 'b' -> "\u001B[96m";
            case 'c' -> "\u001B[91m";
            case 'd' -> "\u001B[95m";
            case 'e' -> "\u001B[93m";
            case 'f' -> "\u001B[97m";
            case 'k' -> "";
            case 'l' -> "\u001B[1m";
            case 'm' -> "\u001B[9m";
            case 'n' -> "\u001B[4m";
            case 'o' -> "\u001B[3m";
            case 'r' -> ANSI_RESET;
            default -> null;
        };
    }

    @Override
    public void sendMessage(@NotNull String... messages) {
        for (String message : messages) {
            sendMessage(message);
        }
    }

    @Override
    public void sendMessage(@Nullable UUID sender, @NotNull String message) {
        sendMessage(message);
    }

    @Override
    public void sendMessage(@Nullable UUID sender, @NotNull String... messages) {
        sendMessage(messages);
    }

    @Override
    public @NotNull Server getServer() {
        return io.lunararcdevs.lunararc.common.LunarArcServerAccess.getCraftServer(this.server);
    }

    @Override
    public @NotNull String getName() {
        return "CONSOLE";
    }

    @Override
    public @NotNull net.kyori.adventure.text.Component name() {
        return net.kyori.adventure.text.Component.text(getName());
    }

    @Override
    public @NotNull Spigot spigot() {
        return super.spigot();
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean value) {
        if (!value) throw new UnsupportedOperationException("Console cannot be de-opped");
    }

    @Override
    public void abandonConversation(@NotNull Conversation conversation) {
        this.conversationTracker.abandonConversation(conversation, new ConversationAbandonedEvent(conversation, new org.bukkit.conversations.ManuallyAbandonedConversationCanceller()));
    }

    @Override
    public void abandonConversation(@NotNull Conversation conversation,
            @NotNull ConversationAbandonedEvent abandonedEvent) {
        this.conversationTracker.abandonConversation(conversation, abandonedEvent);
    }

    @Override
    public void acceptConversationInput(@NotNull String input) {
        this.conversationTracker.acceptConversationInput(input);
    }

    @Override
    public boolean beginConversation(@NotNull Conversation conversation) {
        return this.conversationTracker.beginConversation(conversation);
    }

    @Override
    public boolean isConversing() {
        return this.conversationTracker.isConversing();
    }

    @Override
    public void sendRawMessage(@NotNull String message) {
        sendMessage(message);
    }

    @Override
    public void sendRawMessage(@Nullable UUID sender, @NotNull String message) {
        sendMessage(message);
    }
}
