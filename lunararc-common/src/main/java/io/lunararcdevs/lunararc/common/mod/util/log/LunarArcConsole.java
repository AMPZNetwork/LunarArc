package io.lunararcdevs.lunararc.common.mod.util.log;

import org.slf4j.Logger;

import java.util.logging.Level;


public final class LunarArcConsole {
    private LunarArcConsole() {}

    public static String phase(String message) {
        return "» " + message;
    }

    public static String ok(String message) {
        return "✓ " + message;
    }

    public static String warn(String message) {
        return "! " + message;
    }

    public static void info(Logger logger, String message, Object... args) {
        logger.debug(phase(message), args);
    }

    public static void success(Logger logger, String message, Object... args) {
        logger.debug(ok(message), args);
    }

    public static void warn(Logger logger, String message, Object... args) {
        logger.warn(warn(message), args);
    }

    public static void info(java.util.logging.Logger logger, String message) {
        logger.fine(phase(message));
    }

    public static void success(java.util.logging.Logger logger, String message) {
        logger.fine(ok(message));
    }

    public static void warn(java.util.logging.Logger logger, String message) {
        logger.warning(warn(message));
    }

    public static void severe(java.util.logging.Logger logger, String message) {
        logger.severe("✖ " + message);
    }

    public static void log(java.util.logging.Logger logger, Level level, String message, Throwable error) {
        logger.log(level, "✖ " + message, error);
    }
}
