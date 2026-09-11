package io.lunararcdevs.lunararc.common.server;

import io.lunararcdevs.lunararc.common.LunarArcDebug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Tracks player-issued commands and captures messages sent back to them during execution,
 * logging the player's name, UUID, command, and the plugin/server response.
 */
public final class LunarArcCommandLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger("LunarArc/Command");
    private static final Pattern STRIP_COLOR = Pattern.compile("(?i)§[0-9A-FK-ORX]");
    private static final ThreadLocal<Deque<CommandSession>> SESSIONS = ThreadLocal.withInitial(ArrayDeque::new);

    private LunarArcCommandLogger() {
    }

    private static final long SLOW_COMMAND_THRESHOLD_NANOS = 250_000_000L;

    public static final class CommandSession {
        private final UUID uuid;
        private final String name;
        private final String command;
        private final long startNanos;
        private final List<String> responses = new ArrayList<>();
        private boolean cancelled;

        public CommandSession(UUID uuid, String name, String command) {
            this.uuid = uuid;
            this.name = name;
            this.command = command.startsWith("/") ? command : "/" + command;
            this.startNanos = System.nanoTime();
        }

        public void addResponse(String message) {
            if (message != null && !message.isBlank()) {
                String clean = STRIP_COLOR.matcher(message).replaceAll("").trim();
                if (!clean.isEmpty()) {
                    responses.add(clean);
                }
            }
        }

        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }

    public static boolean isSessionActive(UUID uuid) {
        if (uuid == null) return false;
        Deque<CommandSession> stack = SESSIONS.get();
        return !stack.isEmpty() && uuid.equals(stack.peek().uuid);
    }

    public static void begin(UUID uuid, String name, String command) {
        if (uuid == null || command == null) return;
        SESSIONS.get().push(new CommandSession(uuid, name, command));
    }

    public static void capture(UUID targetUuid, String message) {
        if (targetUuid == null || message == null) return;
        Deque<CommandSession> stack = SESSIONS.get();
        if (stack.isEmpty()) return;
        CommandSession top = stack.peek();
        if (top != null && targetUuid.equals(top.uuid)) {
            top.addResponse(message);
        }
    }

    /**
     * Mark the currently executing command as cancelled (e.g. by PlayerCommandPreprocessEvent).
     */
    public static void markCancelled() {
        Deque<CommandSession> stack = SESSIONS.get();
        if (!stack.isEmpty()) {
            stack.peek().setCancelled(true);
        }
    }

    public static void end() {
        Deque<CommandSession> stack = SESSIONS.get();
        if (stack.isEmpty()) return;
        CommandSession session = stack.pop();
        if (stack.isEmpty()) {
            SESSIONS.remove();
        }

        String responseStr;
        if (session.cancelled) {
            responseStr = "<cancelled by plugin>";
        } else if (session.responses.isEmpty()) {
            responseStr = "<no response>";
        } else {
            responseStr = String.join(" | ", session.responses);
        }

        String logMessage = String.format("Player %s (UUID: %s) executed '%s' -> response: '%s'",
                session.name, session.uuid, session.command, responseStr);

        LOGGER.info("[Command] {}", logMessage);

        if (LunarArcDebug.COMMAND) {
            LunarArcDebug.command("{}", logMessage);
        }

        long elapsedNanos = System.nanoTime() - session.startNanos;
        if (elapsedNanos >= SLOW_COMMAND_THRESHOLD_NANOS) {
            LOGGER.warn("[Command] Player {} (UUID: {}) command '{}' took {}ms to execute",
                    session.name, session.uuid, session.command, elapsedNanos / 1_000_000);
        }
    }
}
