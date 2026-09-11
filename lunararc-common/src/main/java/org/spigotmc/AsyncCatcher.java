package org.spigotmc;

public final class AsyncCatcher {
    /**
     * Kept for binary/source compatibility with plugins and server integrations
     * that temporarily disable the Spigot async catcher.
     */
    public static boolean enabled = true;

    private AsyncCatcher() {
    }

    public static void catchOp(String reason) {
        io.lunararcdevs.lunararc.common.util.AsyncCatcher.enabled = enabled;
        io.lunararcdevs.lunararc.common.util.AsyncCatcher.catchOp(reason);
    }
}
