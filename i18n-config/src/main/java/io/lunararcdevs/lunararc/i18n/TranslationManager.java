package io.lunararcdevs.lunararc.i18n;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

public class TranslationManager {
    private static final String DEFAULT_LOCALE = "en_gb";
    private static final Map<String, JsonObject> cache = new java.util.concurrent.ConcurrentHashMap<>();
    private static volatile String currentLocale = DEFAULT_LOCALE;

    static {
        currentLocale = detectLocale(Locale.getDefault());
        loadLocale(DEFAULT_LOCALE);
        loadLocale(currentLocale);
    }

    public static void setLocale(String locale) {
        currentLocale = normalizeLocale(locale);
        loadLocale(currentLocale);
    }

    public static String detectLocale(Locale locale) {
        if (locale == null) return DEFAULT_LOCALE;
        String language = locale.getLanguage().toLowerCase(Locale.ROOT);
        String country = locale.getCountry().toLowerCase(Locale.ROOT);
        if (language.isBlank()) return DEFAULT_LOCALE;
        if (country.isBlank()) {
            country = switch (language) {
                case "en" -> "us";
                case "pt" -> "br";
                case "zh" -> "cn";
                default -> language;
            };
        }
        return language + "_" + country;
    }

    /** Accepts either Minecraft IDs such as {@code en_gb} or country aliases such as {@code gb}. */
    private static String normalizeLocale(String locale) {
        if (locale == null || locale.isBlank()) return DEFAULT_LOCALE;
        String normalized = locale.trim().toLowerCase(Locale.ROOT).replace('-', '_');
        return switch (normalized) {
            case "gb" -> "en_gb";
            case "us" -> "en_us";
            default -> normalized;
        };
    }

    private static void loadLocale(String locale) {
        if (cache.containsKey(locale))
            return;

        try (InputStream in = openLocale(locale)) {
            if (in != null) {
                JsonObject json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                        .getAsJsonObject();
                cache.put(locale, json);
            } else if (!locale.equals(DEFAULT_LOCALE)) {
                loadLocale(DEFAULT_LOCALE);
            }
        } catch (Exception e) {
            if (!locale.equals(DEFAULT_LOCALE)) {
                loadLocale(DEFAULT_LOCALE);
            }
        }
    }

    private static InputStream openLocale(String locale) {
        String resource = "locale/" + normalizeLocale(locale) + ".json";
        ClassLoader own = TranslationManager.class.getClassLoader();
        if (own != null) {
            InputStream in = own.getResourceAsStream(resource);
            if (in != null) return in;
        }

        ClassLoader context = Thread.currentThread().getContextClassLoader();
        if (context != null && context != own) {
            InputStream in = context.getResourceAsStream(resource);
            if (in != null) return in;
        }

        return ClassLoader.getSystemResourceAsStream(resource);
    }

    public static String get(String key, Object... args) {
        String translation = getRaw(currentLocale, key);
        if (translation == null && !currentLocale.equals(DEFAULT_LOCALE)) {
            translation = getRaw(DEFAULT_LOCALE, key);
        }

        if (translation == null)
            return key;

        for (int i = 0; i < args.length; i++) {
            translation = translation.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return translation;
    }

    private static String getRaw(String locale, String key) {
        JsonObject json = cache.get(locale);
        if (json == null) {
            loadLocale(locale);
            json = cache.get(locale);
        }
        if (json != null && json.has(key)) {
            return json.get(key).getAsString();
        }
        return null;
    }
}
