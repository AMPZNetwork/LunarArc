package io.ampznetwork.lunararc.launcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * How long each phase of launcher startup took, printed as one line when it hands off to the server.
 *
 * <p>This exists because "startup is slow" could not be answered from the code. The launcher does
 * several things that each look cheap and are not always - it reads the whole shipped jar more than
 * once, extracts embedded libraries, and asks GitHub about releases over the network - and which of
 * them costs anything depends on the disk, the machine and whether the network answers at all. A
 * phase breakdown turns the next boot log into the answer rather than something to reason about.</p>
 *
 */
public final class StartupTimer {
    private static final boolean ENABLED = java.util.Arrays.stream(System.getProperty("lunararc.debug", "").split(","))
            .map(String::trim).anyMatch(value -> value.equalsIgnoreCase("all") || value.equalsIgnoreCase("timing"));

    private record Phase(String name, long nanos) {}

    private static final List<Phase> PHASES = new ArrayList<>();
    private static final long START = System.nanoTime();

    private StartupTimer() {
    }

    /** Times {@code work}, recording it under {@code name}. */
    public static void phase(String name, ThrowingRunnable work) throws Exception {
        if (!ENABLED) {
            work.run();
            return;
        }
        long began = System.nanoTime();
        try {
            work.run();
        } finally {
            record(name, System.nanoTime() - began);
        }
    }

    /** Records a phase timed by the caller. */
    public static synchronized void record(String name, long nanos) {
        if (!ENABLED) return;
        PHASES.add(new Phase(name, nanos));
    }

    /** Prints the breakdown. Called once, just before the server takes over. */
    public static synchronized void report() {
        if (!ENABLED) return;
        StringBuilder line = new StringBuilder("[LunarArc] Launcher startup took ")
                .append(seconds(System.nanoTime() - START));
        if (!PHASES.isEmpty()) {
            line.append(" (");
            for (int i = 0; i < PHASES.size(); i++) {
                if (i > 0) line.append(", ");
                line.append(PHASES.get(i).name()).append(' ').append(seconds(PHASES.get(i).nanos()));
            }
            line.append(')');
        }
        System.out.println(line);
    }

    private static String seconds(long nanos) {
        return String.format(Locale.ROOT, "%.2fs", nanos / 1_000_000_000.0d);
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }
}
