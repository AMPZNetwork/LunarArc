package io.lunararcdevs.lunararc.common.server;

/**
 * Raised when a Bukkit/Paper surface has not yet been wired to a concrete
 * Craft/NMS implementation. LunarArc intentionally fails here instead of
 * manufacturing a dynamic proxy or fabricated fallback value.
 */
public final class LunarArcMissingAdapterException extends UnsupportedOperationException {
    public LunarArcMissingAdapterException(String surface) {
        super("Missing concrete LunarArc adapter: " + surface);
    }

    public static LunarArcMissingAdapterException forSurface(String surface) {
        return new LunarArcMissingAdapterException(surface);
    }
}
