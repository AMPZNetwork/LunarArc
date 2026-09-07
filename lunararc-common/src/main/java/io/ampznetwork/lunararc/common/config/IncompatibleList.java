package io.ampznetwork.lunararc.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class IncompatibleList {

    private static final Logger LOGGER = LoggerFactory.getLogger("LunarArc");
    private static final List<Entry> PLUGINS = load("lunararc/incompatible/plugins.tsv");
    private static final List<Entry> MODS = load("lunararc/incompatible/mods.tsv");

    private IncompatibleList() {
    }

    public static Entry check(String pluginMain, String pluginVersion) {
        return find(PLUGINS, pluginMain, pluginVersion);
    }

    public static Entry checkMod(String modId, String version) {
        return find(MODS, modId, version);
    }

    public static void screenLoadedMods(Map<String, String> loadedMods) {
        if (MODS.isEmpty() || loadedMods == null || loadedMods.isEmpty()) return;
        List<Detected> detected = new ArrayList<>();
        for (Map.Entry<String, String> mod : loadedMods.entrySet()) {
            Entry listed = checkMod(mod.getKey(), mod.getValue());
            if (listed != null) detected.add(new Detected("mod", mod.getKey(), mod.getKey(), mod.getValue(), listed.reason()));
        }
        if (!detected.isEmpty()) throw fatal(detected);
    }

    public static IncompatibleSoftwareException fatalPlugin(String pluginMain, String pluginName, String pluginVersion, Entry listed) {
        String displayName = pluginName == null || pluginName.isBlank() ? pluginMain : pluginName;
        return fatal(List.of(new Detected("plugin", pluginMain, displayName, pluginVersion, listed.reason())));
    }

    private static IncompatibleSoftwareException fatal(List<Detected> detected) {
        for (Detected item : detected) {
            LOGGER.error("[LunarArc/Incompatible] type={} id={} name=\"{}\"{} reason=\"{}\"",
                    item.type(), item.id(), item.displayName().replace("\"", "'"),
                    item.version() == null ? "" : " version=" + item.version(),
                    item.reason().replace("\"", "'"));
        }
        LOGGER.error("[LunarArc/IncompatibleFatal] count={}", detected.size());
        LOGGER.error("============================================================");
        LOGGER.error("                 LUNARARC STARTUP ERROR");
        LOGGER.error("============================================================");
        LOGGER.error("Incompatible software detected:");
        for (Detected item : detected) {
            LOGGER.error("  Type:   {}", Character.toUpperCase(item.type().charAt(0)) + item.type().substring(1));
            if ("plugin".equals(item.type())) {
                LOGGER.error("  Plugin: {}{}", item.displayName(), item.version() == null ? "" : " " + item.version());
                LOGGER.error("  Main:   {}", item.id());
            } else {
                LOGGER.error("  Mod:    {}{}", item.displayName(), item.version() == null ? "" : " " + item.version());
            }
            LOGGER.error("  Reason: {}", item.reason());
        }
        LOGGER.error("Server startup has been stopped. Remove the incompatible item(s) and restart.");
        LOGGER.error("============================================================");
        return new IncompatibleSoftwareException(crashMessage(detected));
    }

    private static String crashMessage(List<Detected> detected) {
        StringBuilder message = new StringBuilder();
        if (detected.size() == 1) {
            Detected item = detected.get(0);
            message.append("LunarArc blocked incompatible ")
                    .append(item.type())
                    .append(": ")
                    .append(item.displayName());
            if (item.version() != null) message.append(" ").append(item.version());
            return message.toString();
        }

        message.append("LunarArc blocked ")
                .append(detected.size())
                .append(" incompatible items:");
        for (Detected item : detected) {
            message.append("\n - ")
                    .append(Character.toUpperCase(item.type().charAt(0)))
                    .append(item.type().substring(1))
                    .append(": ")
                    .append(item.displayName());
            if (item.version() != null) message.append(" ").append(item.version());
        }
        return message.toString();
    }

    private static Entry find(List<Entry> entries, String name, String version) {
        if (name == null) return null;
        for (Entry entry : entries) {
            if (!entry.name().equalsIgnoreCase(name)) continue;
            if (entry.version() == null || entry.version().equals(version)) return entry;
        }
        return null;
    }

    private static List<Entry> load(String resource) {
        List<Entry> entries = new ArrayList<>();
        try (InputStream stream = IncompatibleList.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) {
                LOGGER.error("Missing built-in LunarArc incompatible resource: {}", resource);
                return List.of();
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    String[] parts = line.split("\t", 3);
                    if (parts.length != 3) throw new IllegalStateException("Malformed built-in incompatible row in " + resource);
                    entries.add(new Entry(parts[0], parts[1].isEmpty() ? null : parts[1], parts[2]));
                }
            }
        } catch (IOException error) {
            throw new IllegalStateException("Could not read built-in LunarArc incompatible resource " + resource, error);
        }
        return List.copyOf(entries);
    }

    public record Entry(String name, String version, String reason) {
    }

    private record Detected(String type, String id, String displayName, String version, String reason) {
    }
}
