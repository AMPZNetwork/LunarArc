package io.ampznetwork.lunararc.launcher;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Scanner;

/**
 * The high-performance LunarArc Unified Launcher.
 * Designed to be better than Arclight by providing a cleaner, faster boot
 * sequence.
 */
public class Launcher {
    private static final String VERSION = "1.0.0-SNAPSHOT";

    public static void main(String[] args) {
        try {
            ConsoleUI.printLogo();
            
            ConsoleUI.printStep("Initializing system components...");
            LibraryExtractor.extractLibraries();

            Path workingDir = Paths.get("").toAbsolutePath();
            Path lunararcDir = workingDir.resolve(".lunararc");
            if (!Files.exists(lunararcDir)) {
                ConsoleUI.printStep("Creating working directory: " + lunararcDir.getFileName());
                Files.createDirectories(lunararcDir);
            }

            Properties versions = loadProperties("lunararc-launcher.properties");
            
            // Platform persistence logic
            Path configPath = workingDir.resolve("lunararc.properties");
            Properties config = new Properties();
            String choice = "";
            
            if (Files.exists(configPath)) {
                try (InputStream in = Files.newInputStream(configPath)) {
                    config.load(in);
                    choice = config.getProperty("platform", "");
                }
            }

            if (choice == null || choice.isEmpty()) {
                System.out.println(ConsoleUI.PURPLE + ConsoleUI.BOLD + "Available Platforms:" + ConsoleUI.RESET);
                System.out.println(ConsoleUI.WHITE + "  1) " + ConsoleUI.CYAN + "NeoForge" + ConsoleUI.WHITE + " (1.21.1) " + ConsoleUI.RESET + ConsoleUI.ITALIC + "[Recommended]" + ConsoleUI.RESET);
                System.out.println(ConsoleUI.WHITE + "  2) " + ConsoleUI.CYAN + "Forge" + ConsoleUI.WHITE + "    (1.21.1)");
                System.out.println(ConsoleUI.WHITE + "  3) " + ConsoleUI.CYAN + "Fabric" + ConsoleUI.WHITE + "   (1.21.1)");
                System.out.println(ConsoleUI.WHITE + "  4) " + ConsoleUI.CYAN + "Quilt" + ConsoleUI.WHITE + "    (1.21.1)");
                System.out.println();
                System.out.print(ConsoleUI.YELLOW + "Select platform ID > " + ConsoleUI.RESET);

                Scanner scanner = new Scanner(System.in);
                choice = scanner.nextLine();
                
                config.setProperty("platform", choice);
                try (java.io.OutputStream out = Files.newOutputStream(configPath)) {
                    config.store(out, "LunarArc Server Configuration");
                }
            } else {
                ConsoleUI.printStep("Auto-Selecting platform: " + choice + " from " + configPath.getFileName());
            }

            // Clean mod folder logic (Baked-in strategy)
            Path modsDir = Paths.get("mods");
            if (Files.exists(modsDir)) {
                ConsoleUI.printStep("Cleaning previous LunarArc core files...");
                Files.list(modsDir)
                        .filter(p -> p.getFileName().toString().toLowerCase().contains("lunararc"))
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (Exception ignored) {
                            }
                        });
            }
            if (!Files.exists(modsDir))
                Files.createDirectories(modsDir);

            // Stealth copy the current jar to mods as a platform mod
            Path selfPath = Paths.get(Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            String platformName = "";
            switch (choice) {
                case "1": platformName = "neoforge"; break;
                case "2": platformName = "forge"; break;
                case "3": platformName = "fabric"; break;
                case "4": platformName = "quilt"; break;
            }

            if (!platformName.isEmpty()) {
                ConsoleUI.printStep("Deploying core bridge for " + ConsoleUI.CYAN + platformName + ConsoleUI.RESET + "...");
                Path destPath = modsDir.resolve(".lunararc-" + platformName + ".jar");
                Files.copy(selfPath, destPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                destPath.toFile().deleteOnExit();
            }

            switch (choice) {
                case "1":
                    ConsoleUI.printHeader("NeoForge Boot Sequence");
                    NeoForgeInstaller.install(lunararcDir, versions);
                    break;
                case "2":
                    ConsoleUI.printHeader("Forge Boot Sequence");
                    ForgeInstaller.install(lunararcDir, versions);
                    break;
                case "3":
                    ConsoleUI.printHeader("Fabric Boot Sequence");
                    FabricInstaller.install(lunararcDir, versions);
                    break;
                case "4":
                    ConsoleUI.printHeader("Quilt Boot Sequence");
                    QuiltInstaller.install(lunararcDir, versions);
                    break;
                default:
                    ConsoleUI.printError("Invalid selection. Aborting.");
                    break;
            }

        } catch (Exception e) {
            ConsoleUI.printError("Critical failure in boot sequence:");
            e.printStackTrace();
        }
    }

    private static Properties loadProperties(String name) {
        Properties props = new Properties();
        try (InputStream in = Launcher.class.getClassLoader().getResourceAsStream(name)) {
            if (in != null)
                props.load(in);
        } catch (Exception ignored) {
        }
        return props;
    }
}
