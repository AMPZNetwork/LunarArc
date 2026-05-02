package io.ampznetwork.lunararc.common.config;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Central configuration for LunarArc, stored in lunararc.yml (simplified as .properties for now).
 * This avoids external config library dependencies while remaining clean and extensible.
 */
public class LunarArcConfig {

    private static final Path CONFIG_FILE = Paths.get("lunararc.properties");
    private static final Properties props = new Properties();
    private static boolean loaded = false;

    public static void load() {
        if (loaded) return;
        loaded = true;

        // Write defaults if file doesn't exist
        if (!Files.exists(CONFIG_FILE)) {
            try (OutputStream out = Files.newOutputStream(CONFIG_FILE)) {
                Properties defaults = new Properties();
                defaults.setProperty("velocity.enabled", "false");
                defaults.setProperty("velocity.secret", "");
                defaults.setProperty("plugins.folder", "plugins");
                defaults.store(out, "LunarArc Configuration - see https://docs.lunararc.net");
                System.out.println("[LunarArc] Generated default lunararc.properties");
            } catch (IOException e) {
                System.err.println("[LunarArc] Failed to write default config: " + e.getMessage());
            }
        }

        try (InputStream in = Files.newInputStream(CONFIG_FILE)) {
            props.load(in);
            System.out.println("[LunarArc] Configuration loaded.");
        } catch (IOException e) {
            System.err.println("[LunarArc] Failed to load config: " + e.getMessage());
        }
    }

    public static boolean isVelocityEnabled() {
        return Boolean.parseBoolean(props.getProperty("velocity.enabled", "false"));
    }

    public static String getVelocitySecret() {
        return props.getProperty("velocity.secret", "");
    }

    public static String getPluginsFolder() {
        return props.getProperty("plugins.folder", "plugins");
    }
}
