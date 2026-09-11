package io.lunararcdevs.lunararc.common.mod.util.remapper.patcher;

import io.lunararcdevs.lunararc.common.mod.util.remapper.patcher.integrated.LunarArcIntegratedPatcher;
import org.bukkit.configuration.file.YamlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class LunarArcPluginPatcherLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger("LunarArc/PluginPatcher");

    private LunarArcPluginPatcherLoader() {}

    public static List<PluginPatcher> load() {
        List<PluginPatcher> list = new ArrayList<>();
        File pluginFolder = new File("plugins");
        if (pluginFolder.exists()) {
            File[] files = pluginFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".jar")) {
                        loadFromJar(file).ifPresent(list::add);
                    }
                }
                if (!list.isEmpty()) {
                    LOGGER.info("Loaded {} plugin-declared patcher(s)", list.size());
                }
            }
        }
        list.add(new LunarArcIntegratedPatcher());
        list.sort(Comparator.comparing(PluginPatcher::priority));
        return list;
    }

    private static Optional<PluginPatcher> loadFromJar(File file) {
        try (JarFile jarFile = new JarFile(file)) {
            JarEntry jarEntry = jarFile.getJarEntry("plugin.yml");
            if (jarEntry == null) return Optional.empty();
            String name;
            try (InputStream stream = jarFile.getInputStream(jarEntry)) {
                YamlConfiguration configuration = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(stream, StandardCharsets.UTF_8));
                name = configuration.getString("lunararc.patcher");
            }
            if (name == null) return Optional.empty();
            URLClassLoader loader = new URLClassLoader(
                    new URL[]{file.toURI().toURL()}, LunarArcPluginPatcherLoader.class.getClassLoader());
            Class<?> clazz = Class.forName(name, false, loader);
            PluginPatcher patcher = clazz.asSubclass(PluginPatcher.class).getConstructor().newInstance();
            return Optional.of(patcher);
        } catch (Throwable e) {
            LOGGER.debug("Failed to load plugin patcher from {}", file.getName(), e);
            return Optional.empty();
        }
    }
}
