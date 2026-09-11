package io.lunararcdevs.lunararc.common.server;

import io.lunararcdevs.lunararc.launcher.LunarArcAgent;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class LunarArcModuleOpener {

    private static final Logger LOGGER = LoggerFactory.getLogger("LunarArc");
    private static final Set<ClassLoader> OPENED = Collections.newSetFromMap(new WeakHashMap<>());

    private LunarArcModuleOpener() {
    }

    public static void openMinecraftModuleTo(ClassLoader pluginClassLoader, String pluginName) {
        if (pluginClassLoader == null) return;
        synchronized (OPENED) {
            if (!OPENED.add(pluginClassLoader)) return;
        }

        Instrumentation instrumentation = LunarArcAgent.sharedInstrumentation();
        if (instrumentation == null) {
            LOGGER.warn("No Instrumentation available - plugins reflecting into NMS classes "
                    + "(as WorldEdit's //regen does) will throw IllegalAccessError.");
            return;
        }

        ModuleLayer layer = MinecraftServer.class.getModule().getLayer();
        if (layer == null) {
            LOGGER.warn("MinecraftServer's module has no layer - plugins reflecting into "
                    + "NMS classes will throw IllegalAccessError.");
            return;
        }

        Module pluginModule = pluginClassLoader.getUnnamedModule();
        Set<Module> openTo = Set.of(pluginModule);
        int opened = 0;
        for (Module module : layer.modules()) {
            if (!instrumentation.isModifiableModule(module)) continue;
            Map<String, Set<Module>> extraOpens = new HashMap<>();
            for (String pkg : module.getPackages()) {
                extraOpens.put(pkg, openTo);
            }
            if (extraOpens.isEmpty()) continue;
            try {
                instrumentation.redefineModule(module, Set.of(), Map.of(), extraOpens, Set.of(), Map.of());
                opened++;
            } catch (RuntimeException failed) {
                LOGGER.warn("Failed to open module '{}' to {}", module.getName(), pluginClassLoader, failed);
            }
        }
        LOGGER.info("Opened {} module(s) in {}'s layer to plugin '{}' ({})",
                opened, MinecraftServer.class.getModule().getName(), pluginName, pluginClassLoader);
    }
}
