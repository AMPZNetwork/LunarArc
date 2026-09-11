package io.lunararcdevs.lunararc.common.mod;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public enum PluginMappingNamespace {
    MOJANG(false),
    SPIGOT(true);

    public static final String MANIFEST_ATTRIBUTE = "paperweight-mappings-namespace";

    private static final String MOJANG_NAMESPACE = "mojang";
    private static final String MOJANG_PLUS_YARN_NAMESPACE = "mojang+yarn";
    private static final String SPIGOT_NAMESPACE = "spigot";

    private final boolean requiresNmsRemap;

    PluginMappingNamespace(boolean requiresNmsRemap) {
        this.requiresNmsRemap = requiresNmsRemap;
    }

    public boolean requiresNmsRemap() {
        return requiresNmsRemap;
    }

    public static PluginMappingNamespace detect(File pluginFile) {
        try (JarFile jar = new JarFile(pluginFile)) {
            Manifest manifest = jar.getManifest();
            if (manifest != null) {
                String declared = manifest.getMainAttributes().getValue(MANIFEST_ATTRIBUTE);
                if (declared != null && !declared.isBlank()) {
                    return parseDeclared(pluginFile, declared);
                }
            }

            // Paper 1.20.5+ default: Paper plugins are Mojang mapped; ordinary
            // Bukkit/Spigot plugins are treated as Spigot mapped.
            return jar.getJarEntry("paper-plugin.yml") != null ? MOJANG : SPIGOT;
        } catch (IOException error) {
            throw new IllegalStateException("Could not inspect plugin mappings namespace for "
                    + pluginFile.getName(), error);
        }
    }

    private static PluginMappingNamespace parseDeclared(File pluginFile, String value) {
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            // mojang+yarn differs from mojang only in which parameter names the jar carries, which
            // nothing here reads. Both say the NMS references are already Mojang named.
            case MOJANG_NAMESPACE, MOJANG_PLUS_YARN_NAMESPACE -> MOJANG;
            case SPIGOT_NAMESPACE -> SPIGOT;
            // Paper refuses an unknown namespace rather than guessing at it, and so do we: a jar
            // naming a namespace we cannot remap from would be silently mangled by the wrong
            // mapping set, which is worse than not loading. The list is the whole set Paper knows.
            default -> throw new IllegalArgumentException("Unsupported " + MANIFEST_ATTRIBUTE
                    + " '" + value + "' in " + pluginFile.getName()
                    + "; LunarArc 1.21.1 supports '" + MOJANG_NAMESPACE + "', '"
                    + MOJANG_PLUS_YARN_NAMESPACE + "' or '" + SPIGOT_NAMESPACE + "'");
        };
    }
}
