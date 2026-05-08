package io.ampznetwork.lunararc.launcher;

import io.ampznetwork.lunararc.libs.gson.JsonArray;
import io.ampznetwork.lunararc.libs.gson.JsonElement;
import io.ampznetwork.lunararc.libs.gson.JsonObject;
import io.ampznetwork.lunararc.libs.gson.JsonParser;
import io.ampznetwork.lunararc.i18n.TranslationManager;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class UpdateChecker {
    private static final String REPO = "AMPZNetwork/LunarArc";
    private static final String API_URL = "https://api.github.com/repos/" + REPO + "/releases";
    
    public static String LATEST_VERSION = null;
    public static String UPDATE_URL = null;

    public static void check(String currentVersion, String buildName) {
        Path configPath = Paths.get("lunararc.conf");
        Properties props = new Properties();
        boolean enableUpdates = false;

        try {
            if (Files.exists(configPath)) {
                try (java.io.InputStream in = Files.newInputStream(configPath)) {
                    props.load(in);
                    if (!props.containsKey("enable_updates")) {
                        props.setProperty("enable_updates", "false");
                        try (java.io.OutputStream out = Files.newOutputStream(configPath)) {
                            props.store(out, "LunarArc Server Configuration");
                        }
                    }
                    enableUpdates = Boolean.parseBoolean(props.getProperty("enable_updates", "false"));
                }
            } else {
                props.setProperty("enable_updates", "false");
                try (java.io.OutputStream out = Files.newOutputStream(configPath)) {
                    props.store(out, "LunarArc Server Configuration");
                }
            }
        } catch (Exception ignored) {
        }

        if (!enableUpdates) {
            return;
        }

        ConsoleUI.printStep(TranslationManager.get("update.checker.checking", buildName));

        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(API_URL).toURL().openConnection();
            connection.setRequestProperty("User-Agent", "LunarArc-Launcher");

            if (connection.getResponseCode() == 200) {
                JsonArray releases = JsonParser.parseReader(new InputStreamReader(connection.getInputStream()))
                        .getAsJsonArray();

                boolean foundMatch = false;
                for (JsonElement element : releases) {
                    JsonObject release = element.getAsJsonObject();
                    String tagName = release.get("tag_name").getAsString();
                    String name = release.get("name").getAsString();
                    String targetCommitish = release.has("target_commitish")
                            ? release.get("target_commitish").getAsString()
                            : "";
                    String htmlUrl = release.get("html_url").getAsString();

                    String normalizedBuildName = buildName.toLowerCase().replace(" ", "");
                    String normalizedTagName = tagName.toLowerCase().replace("-", "").replace("_", "").replace(" ", "");
                    String normalizedReleaseName = name.toLowerCase().replace("-", "").replace("_", "").replace(" ",
                            "");

                    boolean isMatch = normalizedTagName.contains(normalizedBuildName)
                            || normalizedReleaseName.contains(normalizedBuildName)
                            || targetCommitish.toLowerCase().contains(buildName.toLowerCase());

                    if (isMatch) {
                        foundMatch = true;
                        if (!tagName.equals(currentVersion)) {
                            LATEST_VERSION = tagName;
                            UPDATE_URL = htmlUrl;
                            System.out.println(
                                    TranslationManager.get("update.available", buildName, tagName, currentVersion));
                            System.out.println(TranslationManager.get("update.download", htmlUrl));

                            saveUpdateInfo(currentVersion, tagName, htmlUrl);
                        } else {
                            System.out.println(TranslationManager.get("update.none", buildName));
                        }
                        break;
                    }
                }
                if (!foundMatch) {
                    System.out.println(TranslationManager.get("update.none", buildName));
                }
            }
        } catch (Exception e) {
            // Silently fail update check
        }
    }

    private static void saveUpdateInfo(String current, String latest, String url) {
        try {
            Path configPath = Paths.get("lunararc.conf");
            Properties props = new Properties();

            if (Files.exists(configPath)) {
                try (java.io.InputStream in = Files.newInputStream(configPath)) {
                    props.load(in);
                }
            }

            props.setProperty("update.current", current);
            props.setProperty("update.latest", latest);
            props.setProperty("update.url", url);

            try (java.io.OutputStream out = Files.newOutputStream(configPath)) {
                props.store(out, "LunarArc Server Configuration");
            }
        } catch (Exception ignored) {
        }
    }
}
