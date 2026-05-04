package io.ampznetwork.lunararc.common.server;

import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class LunarArcPluginClassLoader extends URLClassLoader {
    private final Map<String, Class<?>> classes = new HashMap<>();
    private final LunarArcPluginLoader loader;
    private final PluginDescriptionFile description;
    private final File dataFolder;
    private final File file;
    private JavaPlugin plugin;
    
    private static final Set<LunarArcPluginClassLoader> loaders = new CopyOnWriteArraySet<>();

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public LunarArcPluginClassLoader(LunarArcPluginLoader loader, ClassLoader parent, PluginDescriptionFile description, File dataFolder, File file) throws MalformedURLException {
        super(new URL[]{file.toURI().toURL()}, parent);
        this.loader = loader;
        this.description = description;
        this.dataFolder = dataFolder;
        this.file = file;
        loaders.add(this);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return findClass(name, true);
    }

    Class<?> findClass(String name, boolean checkGlobal) throws ClassNotFoundException {
        Class<?> result = classes.get(name);

        if (result == null) {
            if (checkGlobal) {
                for (LunarArcPluginClassLoader loader : loaders) {
                    result = loader.findLoadedClass0(name);
                    if (result != null) break;
                }
            }

            if (result == null) {
                result = super.findClass(name);
                if (result != null) {
                    classes.put(name, result);
                }
            }
        }

        return result;
    }

    private Class<?> findLoadedClass0(String name) {
        return classes.get(name);
    }

    public synchronized void initialize(JavaPlugin plugin) {
        if (this.plugin != null) {
            throw new IllegalStateException("Plugin already initialized!");
        }
        this.plugin = plugin;
    }

    public void close() throws java.io.IOException {
        try {
            super.close();
        } finally {
            loaders.remove(this);
        }
    }
}
