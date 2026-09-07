package io.ampznetwork.lunararc;

import io.papermc.paper.configuration.PaperConfigurations;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PaperConfigurationRegressionTest {
    public static void run() throws Exception {
        Path root = Files.createTempDirectory("lunararc-world-config-");
        try {
            Path defaults = root.resolve("config/paper-world-defaults.yml");
            Path first = root.resolve("world");
            var initial = PaperConfigurations.loadWorldConfiguration(defaults, first);
            check(initial.entities.markers.tick, "default marker ticking");
            Files.writeString(defaults, "lootables:\n  auto-replenish: true\n  refresh-min: 1h\n  refresh-max: 2h\n");
            Files.writeString(first.resolve("paper-world.yml"), "entities:\n  markers:\n    tick: false\nlootables:\n  refresh-min: 30m\n");
            var a = PaperConfigurations.loadWorldConfiguration(defaults, first);
            var b = PaperConfigurations.loadWorldConfiguration(defaults, root.resolve("nether"));
            check(a.lootables.autoReplenish && b.lootables.autoReplenish, "shared defaults");
            check(!a.entities.markers.tick && b.entities.markers.tick, "world override isolation");
            check(a.lootables.refreshMin.seconds() == 1800 && b.lootables.refreshMin.seconds() == 3600, "duration overrides");
            a.lootables.autoReplenish = false;
            check(b.lootables.autoReplenish, "mutable configuration isolation");
            Files.writeString(first.resolve("paper-world.yml"), "lootables:\n  refresh-min: invalid\n");
            try {
                PaperConfigurations.loadWorldConfiguration(defaults, first);
                throw new AssertionError("invalid duration accepted");
            } catch (IOException expected) {
                check(expected.getMessage().contains("refresh-min"), "invalid value diagnostic");
            }
            PaperConfigurations.createDirectoriesSymlinkAware(first);
            try {
                PaperConfigurations.createDirectoriesSymlinkAware(defaults);
                throw new AssertionError("file accepted as directory");
            } catch (IOException expected) {
            }
            io.papermc.paper.adventure.PaperAdventure.class.getMethod("resolveWithContext",
                    net.kyori.adventure.text.Component.class, org.bukkit.command.CommandSender.class,
                    org.bukkit.entity.Entity.class, boolean.class);
        } finally {
            try (var paths = Files.walk(root)) {
                for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) Files.delete(path);
            }
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
