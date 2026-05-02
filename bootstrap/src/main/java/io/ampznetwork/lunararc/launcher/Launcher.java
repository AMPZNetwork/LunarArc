package io.ampznetwork.lunararc.launcher;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Objects;
import java.util.Scanner;

public class Launcher {

    public static void main(String[] args) {
        java.util.Properties versions = new java.util.Properties();
        try (var stream = Launcher.class.getResourceAsStream("/versions.properties")) {
            if (stream != null) versions.load(stream);
        } catch (IOException ignored) {}

        String javaVersion = System.getProperty("java.version");
        int majorVersion = Integer.parseInt(javaVersion.split("\\.")[0]);
        if (majorVersion < 21) {
            System.err.println("LunarArc requires Java 21 or higher to run. You are using Java " + javaVersion);
            System.exit(1);
        }

        Gson gson = new Gson();
        JsonObject i18n;
        try (var reader = new InputStreamReader(Objects.requireNonNull(Launcher.class.getResourceAsStream("/en_us.json")), StandardCharsets.UTF_8)) {
            i18n = gson.fromJson(reader, JsonObject.class);
        } catch (Exception e) {
            System.err.println("Failed to load translations!");
            i18n = new JsonObject();
        }

        String currentVersion = Launcher.class.getPackage().getImplementationVersion();
        if (currentVersion == null) currentVersion = "1.0.0-SNAPSHOT";
        String buildDate = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy").format(java.time.LocalDate.now());
        String releaseName = i18n.has("lunararc.launcher.release_name") ? i18n.get("lunararc.launcher.release_name").getAsString() : "";

        if (i18n.has("lunararc.launcher.logo")) {
            for (var lineElement : i18n.getAsJsonArray("lunararc.launcher.logo")) {
                String line = lineElement.getAsString();
                System.out.println(line.replace("%s", currentVersion)
                        .replace("%s", releaseName)
                        .replace("%s", buildDate)
                        .replace("§", "\u001B[3") + "\u001B[0m");
            }
        }

        Path lunararcDir = Paths.get(".lunararc");
        Path versionFile = lunararcDir.resolve("version.txt");
        Path configFile = Paths.get("lunararc.conf");

        try {
            Path eulaFile = Paths.get("eula.txt");
            if (!Files.exists(eulaFile) || !Files.readString(eulaFile).contains("eula=true")) {
                System.out.println("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
                System.out.print("Type 'true' to agree: ");
                Scanner eulaScanner = new Scanner(System.in);
                String agree = eulaScanner.nextLine();
                if ("true".equalsIgnoreCase(agree)) {
                    Files.writeString(eulaFile, "#By changing the setting below to TRUE you are indicating your agreement to our EULA (https://aka.ms/MinecraftEULA).\neula=true\n");
                } else {
                    System.out.println("EULA not accepted. Exiting...");
                    System.exit(0);
                }
            }
            
            if (!Files.exists(configFile)) {
                Files.writeString(configFile, "proxy-plugins=false\n# LunarArc Configuration\n");
            }

            if (Files.exists(lunararcDir)) {
                String cachedVersion = "";
                if (Files.exists(versionFile)) {
                    cachedVersion = Files.readString(versionFile).trim();
                }

                if (!currentVersion.equals(cachedVersion)) {
                    System.out.println("New version detected, cleaning .lunararc...");
                    try (var stream = Files.walk(lunararcDir)) {
                        stream.sorted(Comparator.reverseOrder())
                              .filter(p -> !p.equals(lunararcDir))
                              .forEach(p -> {
                                  try { Files.delete(p); } catch (Exception ignored) {}
                              });
                    }
                }
            } else {
                Files.createDirectories(lunararcDir);
            }
            Files.writeString(versionFile, currentVersion);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String choice = "1";
        try {
            java.util.List<String> configLines = Files.readAllLines(configFile);
            for (String line : configLines) {
                if (line.startsWith("modloader=")) {
                    choice = line.substring(10).trim();
                    System.out.println("Auto-booting selected modloader: " + choice);
                    gotoLaunch(choice, i18n, versions, lunararcDir);
                    return;
                }
            }
        } catch (IOException ignored) {}

        System.out.println(i18n.has("lunararc.launcher.select_modloader") ? i18n.get("lunararc.launcher.select_modloader").getAsString() : "Select Modloader:");
        System.out.println("1) NeoForge (" + versions.getProperty("neoforge", "unknown") + ")");
        System.out.println("2) Forge (" + versions.getProperty("forge", "unknown") + ")");
        System.out.println("3) Fabric (" + versions.getProperty("fabric", "unknown") + ")");
        System.out.println("4) Quilt (" + versions.getProperty("quilt", "unknown") + ")");
        System.out.println("========================================");

        Scanner scanner = new Scanner(System.in);
        System.out.print("Choice: ");
        choice = scanner.hasNextLine() ? scanner.nextLine() : "1";
        
        try {
            Files.writeString(configFile, "modloader=" + choice + "\n" + Files.readString(configFile));
        } catch (IOException ignored) {}

        gotoLaunch(choice, i18n, versions, lunararcDir);
    }

    private static void gotoLaunch(String choice, JsonObject i18n, java.util.Properties versions, Path lunararcDir) {
        String projectName = versions.getProperty("projectName", "LunarArc");
        String releaseName = i18n.has("lunararc.launcher.release_name") ? i18n.get("lunararc.launcher.release_name").getAsString() : "";
        System.setProperty("lunararc.name", projectName + releaseName);
        
        switch (choice) {
            case "1" -> {
                System.out.println("Selected NeoForge");
                try {
                    NeoForgeInstaller.install(lunararcDir, versions);
                    String mcVersion = versions.getProperty("minecraft", "1.21.1");
                    String paperBuild = versions.getProperty("paperBuild", "131");
                    Path paperJar = lunararcDir.resolve("paper-" + mcVersion + ".jar");
                    if (!Files.exists(paperJar)) {
                        String paperUrl = String.format("https://api.papermc.io/v2/projects/paper/versions/%s/builds/%s/downloads/paper-%s-%s.jar", mcVersion, paperBuild, mcVersion, paperBuild);
                        Downloader.download(paperUrl, paperJar);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            case "2" -> System.out.println("Selected Forge");
            case "3" -> System.out.println("Selected Fabric");
            case "4" -> System.out.println("Selected Quilt");
            default -> System.out.println("Invalid choice, defaulting to NeoForge");
        }
    }
}
