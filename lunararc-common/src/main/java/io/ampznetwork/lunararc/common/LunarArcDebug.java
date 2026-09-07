package io.ampznetwork.lunararc.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

/**
 * Opt-in tracing for the plugin compatibility layer, for use while LunarArc is being built.
 *
 * <p>Every plugin failure this project has had to diagnose so far arrived as a message from the
 * <em>plugin</em>, describing a symptom, with nothing said about what LunarArc did on its way
 * there. "Could not find field 'e' in class ServerCommonPacketListenerImpl" does not distinguish
 * between a missing mapping, a mapping that was found and wrong, and a lookup that never reached
 * the remapper at all - the last of which is what it actually was. That took a source read of
 * another project to establish. These channels answer it directly.</p>
 *
 * <p>Companion to {@link io.ampznetwork.lunararc.common.debug.LunarArcPluginDebug}, which records
 * failures after the fact in {@code logs/lunararc-plugin-debug.log}. This records what led up to
 * one, and writes beside it as {@code logs/lunararc-debug.log} - same folder as the server's own
 * logs, truncated per run like {@code latest.log}, so a reproduction is one file.</p>
 *
 * <p>Off unless asked for:</p>
 *
 * <pre>
 *   -Dlunararc.debug=all                everything
 * </pre>
 *
 * <p>Each channel is a {@code static final boolean} resolved once at class initialization, so a
 * disabled channel is a constant {@code false} the JIT folds away along with the call behind it.
 * That is why every call site is written as {@code if (LunarArcDebug.REFLECT) ...} - the guard is
 * what makes this free to leave in place, and an unguarded call would still build its
 * arguments.</p>
 *
 * <p>Trace goes to the file rather than the console. The reflect channel alone is thousands of
 * lines on a busy join, which would bury the console output someone actually needs to read while
 * reproducing a fault; the console gets one line at startup naming the file. As in
 * LunarArcPluginDebug, every write swallows its own failures - diagnostics must never be the
 * reason a server stops.</p>
 *
 * <p>This is a development aid. It is safe to ship - it does nothing unless the property is set -
 * but it is not an API, and the channels are expected to come and go with whatever is being worked
 * on.</p>
 */
public final class LunarArcDebug {

    private static final Logger LOGGER = LoggerFactory.getLogger("LunarArc/Debug");
    private static final Path OUTPUT = Path.of("logs", "lunararc-debug.log");
    private static final Object LOCK = new Object();

    /** Reflective member lookups routed through LunarArcReflectionBridge: what a plugin asked for, what it was mapped to, and whether it resolved. */
    public static final boolean REFLECT;

    /** Bytecode transformation of plugin classes: which remapping each class was given, and why. */
    public static final boolean REMAP;

    /** Plugin class loading: which loader answered a name, and under which of the requested or mapped spellings. */
    public static final boolean CLASSLOAD;

    /** Entity-side decisions LunarArc overrides, such as letting a player through a portal vanilla would refuse. */
    public static final boolean ENTITY;

    /**
     * The whole life of a fluid step: the block placement that schedules the first tick, the tick
     * itself, and every spread decision with the Bukkit verdict on it.
     *
     * <p>Added because "placed water does not flow" is a symptom four separate mechanisms can
     * produce - the placement never scheduling a tick, the tick never running, the spread being
     * refused by vanilla, or LunarArc's own BlockFromToEvent cancelling it - and reading the source
     * cannot tell them apart. Each line says which stage was reached, so one reproduction rules out
     * three of the four.</p>
     */
    public static final boolean FLUID;

    /**
     * Commands arriving from the console, and what LunarArc did with each one.
     *
     * <p>"stop does nothing at the console" has three possible shapes - the line never reaches the
     * server at all, it reaches it and Bukkit refuses it, or it is dispatched and the command
     * itself does nothing - and the console gives no way to tell them apart. One line per console
     * command, recorded where the server first sees it, separates the first case from the other
     * two straight away.</p>
     */
    public static final boolean COMMAND;

    /**
     * Player interaction pipeline tracing: packet type, hand, item, hit result, event action, event
     * cancelled state, useItemInHand result, firedInteract state, whether handleUseItem continues,
     * whether gameMode.useItem() is called, InteractionResult, and player.isUsingItem before/after.
     */
    public static final boolean INTERACT;

    /**
     * Startup and shutdown lifecycle timing: per-plugin enable/disable durations, phase-level
     * totals, and a ranked summary of the slowest items at the end of each lifecycle.
     */
    public static final boolean TIMING;
    public static final boolean PLUGIN;

    private static BufferedWriter writer;
    private static boolean unusable;

    static {
        java.util.Set<String> channels = new java.util.HashSet<>();
        for (String channel : System.getProperty("lunararc.debug", "").split(",")) {
            channels.add(channel.trim().toLowerCase(java.util.Locale.ROOT));
        }
        boolean all = channels.contains("all");
        INTERACT = all || channels.contains("interact");
        TIMING = all || channels.contains("timing");
        REFLECT = all || channels.contains("reflect");
        REMAP = all || channels.contains("remap");
        CLASSLOAD = all || channels.contains("classload");
        ENTITY = all || channels.contains("entity");
        FLUID = all || channels.contains("fluid");
        COMMAND = all || channels.contains("command");
        PLUGIN = all || channels.contains("plugin");
        if (INTERACT || TIMING || REFLECT || REMAP || CLASSLOAD || ENTITY || FLUID || COMMAND || PLUGIN) {
            StringBuilder enabled = new StringBuilder();
            if (INTERACT) enabled.append(" interact");
            if (TIMING) enabled.append(" timing");
            if (REFLECT) enabled.append(" reflect");
            if (REMAP) enabled.append(" remap");
            if (CLASSLOAD) enabled.append(" classload");
            if (ENTITY) enabled.append(" entity");
            if (FLUID) enabled.append(" fluid");
            if (COMMAND) enabled.append(" command");
            if (PLUGIN) enabled.append(" plugin");

            // Opened here rather than on the first trace line. Lazily creating the file made an
            // absent file mean two different things - the channel never turned on, or it turned on
            // and nothing ever reached it - and those need completely different next steps. Now the
            // file exists whenever a channel does, so an empty one is a real answer: the traced
            // code did not run.
            try {
                open();
            } catch (Throwable ignored) {
                // open() reports its own failure once and disables itself.
            }

            String announcement = "[LunarArc/Debug] Debug channels enabled:" + enabled + " - writing to "
                    + OUTPUT.toAbsolutePath() + ". Verbose by design; not meant to be left on for a "
                    + "running server.";
            LOGGER.info(announcement);
            // Also on stdout. This class initializes from the plugin remapper, early enough that
            // whether the logger is configured yet depends on the loader, and an announcement that
            // may or may not appear is no use for telling someone their flag did not take.
            System.out.println(announcement);
        }
    }

    private LunarArcDebug() {
    }

    /** Log on the reflect channel. Call behind {@code if (LunarArcDebug.REFLECT)}. */
    public static void reflect(String format, Object... args) {
        write("reflect", format, args);
    }

    /** Log on the remap channel. Call behind {@code if (LunarArcDebug.REMAP)}. */
    public static void remap(String format, Object... args) {
        write("remap", format, args);
    }

    /** Log on the classload channel. Call behind {@code if (LunarArcDebug.CLASSLOAD)}. */
    public static void classload(String format, Object... args) {
        write("classload", format, args);
    }

    /** Log on the entity channel. Call behind {@code if (LunarArcDebug.ENTITY)}. */
    public static void entity(String format, Object... args) {
        write("entity", format, args);
    }

    /** Log on the interact channel. Call behind {@code if (LunarArcDebug.INTERACT)}. */
    public static void interact(String format, Object... args) {
        write("interact", format, args);
        LOGGER.info("[interact] " + format(format, args));
    }

    /**
     * The enabled channels, or "none", for reporting alongside the build banner.
     *
     * <p>Whether -Dlunararc.debug reached the server JVM at all has cost more than one round trip to
     * establish. The banner is a line already known to appear, so putting the answer on it removes
     * that question from every future one.</p>
     */
    public static String enabledChannels() {
        StringBuilder enabled = new StringBuilder();
        if (INTERACT) enabled.append("interact ");
        if (TIMING) enabled.append("timing ");
        if (REFLECT) enabled.append("reflect ");
        if (REMAP) enabled.append("remap ");
        if (CLASSLOAD) enabled.append("classload ");
        if (ENTITY) enabled.append("entity ");
        if (FLUID) enabled.append("fluid ");
        if (COMMAND) enabled.append("command ");
        if (PLUGIN) enabled.append("plugin ");
        return enabled.isEmpty() ? "none" : enabled.toString().trim().replace(' ', ',');
    }

    /** Log on the command channel. Call behind {@code if (LunarArcDebug.COMMAND)}. */
    public static void command(String format, Object... args) {
        write("command", format, args);
    }

    /** Log on the timing channel. Call behind {@code if (LunarArcDebug.TIMING)}. */
    public static void timing(String format, Object... args) {
        write("timing", format, args);
    }

    /** Log on the fluid channel. Call behind {@code if (LunarArcDebug.FLUID)}. */
    public static void fluid(String format, Object... args) {
        write("fluid", format, args);
    }

    private static void write(String channel, String format, Object... args) {
        boolean enabled = switch (channel) {
            case "interact" -> INTERACT;
            case "timing" -> TIMING;
            case "reflect" -> REFLECT;
            case "remap" -> REMAP;
            case "classload" -> CLASSLOAD;
            case "entity" -> ENTITY;
            case "fluid" -> FLUID;
            case "command" -> COMMAND;
            default -> false;
        };
        if (!enabled) return;
        try {
            synchronized (LOCK) {
                BufferedWriter out = open();
                if (out == null) return;
                out.write(Instant.now() + " [" + channel + "] " + format(format, args) + "\n");
                // Flushed per line rather than on a buffer boundary: the failures this exists to
                // trace routinely end in a crash or a hung server, and a half-written buffer is
                // exactly the tail that matters.
                out.flush();
            }
        } catch (Throwable ignored) {
            // Diagnostics must never be the reason a server stops.
        }
    }

    private static BufferedWriter open() throws IOException {
        if (writer != null) return writer;
        if (unusable) return null;
        try {
            Path parent = OUTPUT.getParent();
            if (parent != null) Files.createDirectories(parent);
            // TRUNCATE, matching latest.log: one file per run is what a reproduction wants, and
            // an appending trace at this volume would be unreadable by the third restart.
            writer = Files.newBufferedWriter(OUTPUT, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            writer.write("LunarArc debug trace\n");
            writer.write("session-start: " + Instant.now() + "\n");
            writer.write("channels:"
                    + (INTERACT ? " interact" : "")
                    + (TIMING ? " timing" : "")
                    + (REFLECT ? " reflect" : "")
                    + (REMAP ? " remap" : "")
                    + (CLASSLOAD ? " classload" : "")
                    + (ENTITY ? " entity" : "")
                    + (FLUID ? " fluid" : "")
                    + (COMMAND ? " command" : "") + "\n");
            writer.write("This file is passive tracing only; it does not change plugin behaviour.\n\n");
            writer.flush();
            Runtime.getRuntime().addShutdownHook(new Thread(LunarArcDebug::close, "LunarArc-debug-close"));
            return writer;
        } catch (Throwable failure) {
            // One complaint, then stay quiet: a read-only logs directory should not produce a
            // warning per traced lookup.
            unusable = true;
            LOGGER.warn("Cannot write {} - debug tracing disabled for this run: {}",
                    OUTPUT.toAbsolutePath(), failure.toString());
            return null;
        }
    }

    private static void close() {
        synchronized (LOCK) {
            if (writer == null) return;
            try {
                writer.flush();
                writer.close();
            } catch (Throwable ignored) {
            }
            writer = null;
        }
    }

    /**
     * SLF4J-style {@code {}} substitution, so call sites read the same as the logging around them
     * and cost nothing to convert if a channel ever graduates to a real logger.
     */
    private static String format(String format, Object... args) {
        if (args == null || args.length == 0) return format;
        StringBuilder out = new StringBuilder(format.length() + 32);
        int arg = 0;
        int index = 0;
        while (index < format.length()) {
            int placeholder = format.indexOf("{}", index);
            if (placeholder < 0 || arg >= args.length) {
                out.append(format, index, format.length());
                break;
            }
            out.append(format, index, placeholder).append(args[arg++]);
            index = placeholder + 2;
        }
        return out.toString();
    }

    /**
     * The class that called into the bridge, skipping LunarArc's own frames.
     *
     * <p>This is the field that mattered and was missing: a reflective failure names the member
     * but not who looked it up, and "which plugin class is doing this" is what decides whether the
     * lookup is even reaching the remapper. DecentHolograms routes every one of its lookups
     * through a single {@code ReflectField} helper, which is both why its class carried no
     * net.minecraft reference for the old constant-pool test to find and, once named here, the
     * fastest way to see that.</p>
     *
     * <p>Walks the stack, so only ever call it behind an enabled channel.</p>
     */
    public static String caller() {
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(frames -> frames
                        .map(StackWalker.StackFrame::getDeclaringClass)
                        .filter(type -> !type.getName().startsWith("io.ampznetwork.lunararc."))
                        .filter(type -> !type.getName().startsWith("java."))
                        .map(Class::getName)
                        .findFirst()
                        .orElse("unknown"));
    }
}
