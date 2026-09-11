package io.lunararcdevs.lunararc.common.server;

import io.lunararcdevs.lunararc.common.LunarArcDebug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.Locale;

public final class LunarArcTimings {

    private static final Logger LOGGER = LoggerFactory.getLogger("LunarArc/Timings");

    private static final List<Entry> startupEntries = new ArrayList<>();
    private static final List<Entry> shutdownEntries = new ArrayList<>();
    private static final Object LOCK = new Object();

    private static long serverStartNanos;
    private static long shutdownStartNanos;

    public record Entry(String phase, String item, long durationMs) {}

    private LunarArcTimings() {}

    // ── Startup ──

    public static void markServerStart() {
        if (!LunarArcDebug.TIMING) return;
        serverStartNanos = System.nanoTime();
    }

    public static long phaseStart() {
        return LunarArcDebug.TIMING ? System.nanoTime() : 0;
    }

    public static void recordStartup(String phase, String item, long startNanos) {
        if (!LunarArcDebug.TIMING) return;
        long ms = elapsedMillis(startNanos);
        synchronized (LOCK) {
            startupEntries.add(new Entry(phase, item, ms));
        }
        if (LunarArcDebug.TIMING) {
            LunarArcDebug.timing("[startup] {} / {} took {}ms", phase, item, ms);
        }
    }

    public static void recordStartupPhase(String phase, long startNanos) {
        if (!LunarArcDebug.TIMING) return;
        long ms = elapsedMillis(startNanos);
        synchronized (LOCK) {
            startupEntries.add(new Entry(phase, null, ms));
        }
        LOGGER.info("[Startup] {} completed in {}", phase, formatDuration(ms));
        if (LunarArcDebug.TIMING) {
            LunarArcDebug.timing("[startup] {} completed in {}ms", phase, ms);
        }
    }

    public static void logStartupSummary() {
        if (!LunarArcDebug.TIMING) return;
        long totalMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - serverStartNanos);

        List<Entry> phases;
        List<Entry> items;
        synchronized (LOCK) {
            phases = startupEntries.stream().filter(e -> e.item == null).toList();
            items = startupEntries.stream().filter(e -> e.item != null)
                    .sorted(Comparator.comparingLong(Entry::durationMs).reversed())
                    .toList();
        }

        logSummary("STARTUP", totalMs, phases, items);

        if (LunarArcDebug.TIMING && !items.isEmpty()) {
            LunarArcDebug.timing("=== Full Startup Item Breakdown ===");
            for (Entry entry : items) {
                LunarArcDebug.timing("  {} / {} — {}ms", entry.phase, entry.item, entry.durationMs);
            }
        }
    }

    // ── Shutdown ──

    public static void markShutdownStart() {
        if (!LunarArcDebug.TIMING) return;
        shutdownStartNanos = System.nanoTime();
    }

    public static void recordShutdown(String phase, String item, long startNanos) {
        if (!LunarArcDebug.TIMING) return;
        long ms = elapsedMillis(startNanos);
        synchronized (LOCK) {
            shutdownEntries.add(new Entry(phase, item, ms));
        }
        if (LunarArcDebug.TIMING) {
            LunarArcDebug.timing("[shutdown] {} / {} took {}ms", phase, item, ms);
        }
    }

    public static void recordShutdownPhase(String phase, long startNanos) {
        if (!LunarArcDebug.TIMING) return;
        long ms = elapsedMillis(startNanos);
        synchronized (LOCK) {
            shutdownEntries.add(new Entry(phase, null, ms));
        }
        LOGGER.info("[Shutdown] {} completed in {}", phase, formatDuration(ms));
        if (LunarArcDebug.TIMING) {
            LunarArcDebug.timing("[shutdown] {} completed in {}ms", phase, ms);
        }
    }

    public static void logShutdownSummary() {
        if (!LunarArcDebug.TIMING) return;
        long totalMs = shutdownStartNanos > 0
                ? TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - shutdownStartNanos) : 0;

        List<Entry> phases;
        List<Entry> items;
        synchronized (LOCK) {
            phases = shutdownEntries.stream().filter(e -> e.item == null).toList();
            items = shutdownEntries.stream().filter(e -> e.item != null)
                    .sorted(Comparator.comparingLong(Entry::durationMs).reversed())
                    .toList();
        }

        logSummary("SHUTDOWN", totalMs, phases, items);
    }

    private static void logSummary(String label, long totalMs, List<Entry> phases, List<Entry> items) {
        LOGGER.info("+------------------------------------------------------------+");
        LOGGER.info("| LunarArc Timings | {} | total {}", label, formatDuration(totalMs));
        LOGGER.info("+------------------------------------------------------------+");
        for (Entry phase : phases) {
            LOGGER.info("{}", timingRow("PHASE", phase.phase, phase.durationMs));
        }

        if (!items.isEmpty()) {
            int slowCount = Math.min(10, items.size());
            long slowThreshold = 50;
            List<Entry> slow = items.stream()
                    .filter(e -> e.durationMs >= slowThreshold)
                    .limit(slowCount)
                    .toList();

            if (!slow.isEmpty()) {
                LOGGER.info("| SLOWEST {} ITEMS (threshold {})", slow.size(), formatDuration(slowThreshold));
                for (Entry entry : slow) {
                    String name = entry.phase + " / " + entry.item;
                    LOGGER.info("{}", timingRow("ITEM", name, entry.durationMs));
                }
            }
        }
        LOGGER.info("+------------------------------------------------------------+");
    }

    private static String formatDuration(long durationMs) {
        if (durationMs < 1000) return durationMs + "ms";
        if (durationMs < 60_000) return String.format(Locale.ROOT, "%.3fs", durationMs / 1000.0d);
        long minutes = durationMs / 60_000;
        long seconds = (durationMs % 60_000) / 1000;
        return minutes + "m " + seconds + "s";
    }

    private static long elapsedMillis(long startNanos) {
        long elapsedNanos = Math.max(0L, System.nanoTime() - startNanos);
        return Math.max(1L, TimeUnit.NANOSECONDS.toMillis(elapsedNanos));
    }

    private static String timingRow(String kind, String name, long durationMs) {
        String displayName = name.length() > 42 ? name.substring(0, 39) + "..." : name;
        return String.format(Locale.ROOT, "| %-6s %-42s %8s |", kind, displayName, formatDuration(durationMs));
    }

    /** Clears all recorded entries. Called at the end of shutdown. */
    public static void reset() {
        synchronized (LOCK) {
            startupEntries.clear();
            shutdownEntries.clear();
        }
    }
}
