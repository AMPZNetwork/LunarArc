package io.ampznetwork.lunararc.common.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.OptionalInt;
import java.util.Properties;

public final class LunarArcVersionInfo {
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream in = LunarArcVersionInfo.class.getClassLoader()
                .getResourceAsStream("lunararc-launcher.properties")) {
            if (in != null) {
                PROPERTIES.load(in);
            }
        } catch (IOException ignored) {
        }
    }

    private LunarArcVersionInfo() {
    }

    public static String projectName() {
        return "Paper";
    }

    public static String projectVersion() {
        return "git-Paper-133 (MC: " + minecraftVersion() + ")";
    }

    public static String minecraftVersion() {
        return "1.21.1";
    }

    public static String paperApiVersion() {
        return "1.21.1-R0.1-SNAPSHOT";
    }

    public static OptionalInt dataVersion() {
        String value = property("dataVersion", "");
        if (value.isBlank()) {
            return OptionalInt.empty();
        }
        try {
            return OptionalInt.of(Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
            return OptionalInt.empty();
        }
    }

    private static String property(String key, String fallback) {
        return PROPERTIES.getProperty(key, fallback);
    }
}
