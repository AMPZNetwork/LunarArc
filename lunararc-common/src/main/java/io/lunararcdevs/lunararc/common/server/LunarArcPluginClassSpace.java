package io.lunararcdevs.lunararc.common.server;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.PluginClassLoader;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Loader-owned plugin class space. */
public final class LunarArcPluginClassSpace {
    private final Map<String, PluginClassLoader> aliases = new LinkedHashMap<>();
    private final Set<PluginClassLoader> legacyLoaders = new LinkedHashSet<>();

    public synchronized void register(PluginClassLoader loader, PluginDescriptionFile description, boolean paperPlugin) {
        aliases.put(normalize(description.getName()), loader);
        for (String provided : description.getProvides()) {
            aliases.putIfAbsent(normalize(provided), loader);
        }
        if (!paperPlugin) legacyLoaders.add(loader);
    }

    public synchronized void unregister(PluginClassLoader loader) {
        aliases.entrySet().removeIf(entry -> entry.getValue() == loader);
        legacyLoaders.remove(loader);
    }

    public Class<?> findDependencyClass(PluginClassLoader requester, String className,
                                         Iterable<String> visibleDependencies, boolean legacyShared)
            throws ClassNotFoundException {
        List<PluginClassLoader> targets = targets(requester, visibleDependencies, legacyShared);
        for (PluginClassLoader target : targets) {
            try {
                return target.findClassFromOwnSpace(className);
            } catch (ClassNotFoundException ignored) {
            }
        }
        throw new ClassNotFoundException(className);
    }

    public URL findDependencyResource(PluginClassLoader requester, String resourceName,
                                      Iterable<String> visibleDependencies, boolean legacyShared) {
        for (PluginClassLoader target : targets(requester, visibleDependencies, legacyShared)) {
            URL resource = target.findResourceFromOwnSpace(resourceName);
            if (resource != null) return resource;
        }
        return null;
    }

    public void addDependencyResources(Set<URL> result, PluginClassLoader requester, String resourceName,
                                       Iterable<String> visibleDependencies, boolean legacyShared) {
        for (PluginClassLoader target : targets(requester, visibleDependencies, legacyShared)) {
            try {
                result.addAll(target.findResourcesFromOwnSpace(resourceName));
            } catch (java.io.IOException ignored) {
                URL resource = target.findResourceFromOwnSpace(resourceName);
                if (resource != null) result.add(resource);
            }
        }
    }

    private synchronized List<PluginClassLoader> targets(PluginClassLoader requester,
                                                          Iterable<String> dependencies, boolean legacyShared) {
        LinkedHashSet<PluginClassLoader> result = new LinkedHashSet<>();
        for (String dependency : dependencies) {
            PluginClassLoader target = aliases.get(normalize(dependency));
            if (target != null && target != requester) result.add(target);
        }
        if (legacyShared) {
            for (PluginClassLoader target : legacyLoaders) {
                if (target != requester) result.add(target);
            }
        }
        return new ArrayList<>(result);
    }

    private static String normalize(String name) {
        return name.replace(' ', '_').toLowerCase(Locale.ROOT);
    }
}
