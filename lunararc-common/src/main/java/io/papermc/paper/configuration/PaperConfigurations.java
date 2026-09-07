package io.papermc.paper.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import io.papermc.paper.configuration.type.Duration;
import io.papermc.paper.configuration.type.DurationOrDisabled;

public final class PaperConfigurations {
    private PaperConfigurations() {}

    public static void createDirectoriesSymlinkAware(Path path) throws IOException {
        if (!Files.isDirectory(path)) Files.createDirectories(path);
    }

    @Deprecated
    public static YamlConfiguration loadLegacyConfigFile(File file) throws Exception {
        return read(file.toPath());
    }

    public static WorldConfiguration loadWorldConfiguration(Path defaultsFile, Path worldDirectory) throws IOException {
        createDirectoriesSymlinkAware(defaultsFile.toAbsolutePath().getParent());
        createDirectoriesSymlinkAware(worldDirectory);
        YamlConfiguration defaults = new YamlConfiguration();
        defaults.set("_version", 31);
        defaults.set("lootables.auto-replenish", false);
        defaults.set("lootables.restrict-player-reloot", true);
        defaults.set("lootables.restrict-player-reloot-time", "disabled");
        defaults.set("lootables.max-refills", -1);
        defaults.set("lootables.refresh-min", "12h");
        defaults.set("lootables.refresh-max", "2d");
        defaults.set("lootables.reset-seed-on-fill", true);
        defaults.set("entities.markers.tick", true);
        createIfMissing(defaultsFile, defaults.saveToString());
        Path worldFile = worldDirectory.resolve("paper-world.yml");
        createIfMissing(worldFile, "_version: 31\n");
        merge(defaults, read(defaultsFile));
        merge(defaults, read(worldFile));
        try {
            return parseWorldConfiguration(defaults);
        } catch (IllegalArgumentException error) {
            throw new IOException("Invalid Paper world configuration in " + worldFile + " or " + defaultsFile + ": " + error.getMessage(), error);
        }
    }

    private static void createIfMissing(Path path, String contents) throws IOException {
        try {
            Files.writeString(path, contents, StandardOpenOption.CREATE_NEW);
        } catch (java.nio.file.FileAlreadyExistsException existing) {
            if (!Files.isRegularFile(path)) throw existing;
        }
    }

    private static YamlConfiguration read(Path path) throws IOException {
        YamlConfiguration config = new YamlConfiguration();
        if (!Files.exists(path)) return config;
        try {
            config.load(path.toFile());
        } catch (org.bukkit.configuration.InvalidConfigurationException error) {
            throw new IOException("Invalid YAML in " + path, error);
        }
        return config;
    }

    private static void merge(YamlConfiguration target, YamlConfiguration source) {
        for (var entry : source.getValues(true).entrySet()) {
            if (!(entry.getValue() instanceof ConfigurationSection)) target.set(entry.getKey(), entry.getValue());
        }
    }

    public static WorldConfiguration parseWorldConfiguration(YamlConfiguration config) {
        for (String section : java.util.List.of("lootables", "entities", "entities.markers", "anticheat", "anticheat.anti-xray")) {
            Object value = config.get(section);
            if (value != null && !(value instanceof ConfigurationSection)) throw new IllegalArgumentException(section + " must be a mapping");
        }
        WorldConfiguration result = new WorldConfiguration();
        result.lootables.autoReplenish = bool(config, "lootables.auto-replenish", false);
        result.lootables.restrictPlayerReloot = bool(config, "lootables.restrict-player-reloot", true);
        result.lootables.resetSeedOnFill = bool(config, "lootables.reset-seed-on-fill", true);
        result.lootables.maxRefills = integer(config, "lootables.max-refills", -1, -1, Integer.MAX_VALUE);
        result.lootables.refreshMin = duration(config, "lootables.refresh-min", "12h");
        result.lootables.refreshMax = duration(config, "lootables.refresh-max", "2d");
        if (result.lootables.refreshMax.seconds() < result.lootables.refreshMin.seconds()) {
            throw new IllegalArgumentException("lootables.refresh-max must be at least refresh-min");
        }
        Object reloot = config.get("lootables.restrict-player-reloot-time", "disabled");
        result.lootables.restrictPlayerRelootTime = new DurationOrDisabled(
                "disabled".equalsIgnoreCase(String.valueOf(reloot)) ? java.util.Optional.empty()
                        : java.util.Optional.of(duration(config, "lootables.restrict-player-reloot-time", "disabled")));
        result.entities.markers.tick = bool(config, "entities.markers.tick", true);
        var antiXray = result.anticheat.antiXray;
        antiXray.enabled = bool(config, "anticheat.anti-xray.enabled", false);
        antiXray.engineMode = integer(config, "anticheat.anti-xray.engine-mode", 1, 1, 3);
        antiXray.maxBlockHeight = integer(config, "anticheat.anti-xray.max-block-height", 64, Integer.MIN_VALUE, Integer.MAX_VALUE);
        antiXray.updateRadius = integer(config, "anticheat.anti-xray.update-radius", 2, 0, 2);
        antiXray.lavaObscures = bool(config, "anticheat.anti-xray.lava-obscures", false);
        Object hidden = config.get("anticheat.anti-xray.hidden-blocks", java.util.List.of());
        if (!(hidden instanceof java.util.List<?> list) || list.stream().anyMatch(value -> !(value instanceof String))) {
            throw new IllegalArgumentException("anticheat.anti-xray.hidden-blocks must be a list of block names");
        }
        antiXray.hiddenBlocks = java.util.List.copyOf(config.getStringList("anticheat.anti-xray.hidden-blocks"));
        return result;
    }

    private static boolean bool(YamlConfiguration config, String key, boolean fallback) {
        Object value = config.get(key, fallback);
        if (!(value instanceof Boolean result)) throw new IllegalArgumentException(key + " must be true or false");
        return result;
    }

    private static int integer(YamlConfiguration config, String key, int fallback, int min, int max) {
        Object value = config.get(key, fallback);
        if (!(value instanceof Number number) || !Double.isFinite(number.doubleValue())
                || number.doubleValue() != number.longValue() || number.longValue() < min || number.longValue() > max) {
            throw new IllegalArgumentException(key + " must be an integer between " + min + " and " + max);
        }
        return number.intValue();
    }

    private static Duration duration(YamlConfiguration config, String key, String fallback) {
        String value = String.valueOf(config.get(key, fallback)).trim();
        if (!value.matches("[0-9]+(?:\\.[0-9]+)?\\s*[dhms]?")) throw new IllegalArgumentException(key + " must be a non-negative duration");
        double amount = Double.parseDouble(value.replaceAll("[dhms\\s]", ""));
        char unit = value.charAt(value.length() - 1);
        double seconds = amount * switch (unit) { case 'd' -> 86400; case 'h' -> 3600; case 'm' -> 60; default -> 1; };
        if (!Double.isFinite(seconds) || seconds > Integer.MAX_VALUE) throw new IllegalArgumentException(key + " is too large");
        return Duration.of(value);
    }
}
