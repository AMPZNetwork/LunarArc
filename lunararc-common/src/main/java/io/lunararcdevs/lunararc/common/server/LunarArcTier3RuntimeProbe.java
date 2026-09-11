package io.lunararcdevs.lunararc.common.server;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Opt-in runtime acceptance diagnostics for the Tier-3 hybrid compatibility surface.
 * Enable with -Dlunararc.tier3Probe=true on a test server. This class does not alter
 * loader, plugin, permission or service behavior; it only reports the state exposed
 * by the real Bukkit/Paper runtime after plugins have enabled.
 */
public final class LunarArcTier3RuntimeProbe {
    private static final Logger LOGGER = LoggerFactory.getLogger("LunarArc/Tier3Probe");

    private LunarArcTier3RuntimeProbe() {}

    public static void run(Server server) {
        if (!Boolean.getBoolean("lunararc.tier3Probe")) return;

        LOGGER.info("Tier-3 runtime probe starting; plugins={}, worlds={}",
                server.getPluginManager().getPlugins().length, server.getWorlds().size());
        probePlugin(server, "LuckPerms", "net.luckperms.api.LuckPerms");
        probePlugin(server, "Vault", "net.milkbowl.vault.permission.Permission");

        long customWorlds = server.getWorlds().stream()
                .filter(world -> world.getEnvironment() != org.bukkit.World.Environment.NORMAL
                        && world.getEnvironment() != org.bukkit.World.Environment.NETHER
                        && world.getEnvironment() != org.bukkit.World.Environment.THE_END)
                .count();
        LOGGER.info("Tier-3 runtime probe worlds: total={}, custom-environment={}", server.getWorlds().size(), customWorlds);
        LOGGER.info("Tier-3 runtime probe complete");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void probePlugin(Server server, String pluginName, String serviceClassName) {
        Plugin plugin = server.getPluginManager().getPlugin(pluginName);
        if (plugin == null) {
            LOGGER.info("Tier-3 probe {}: not installed", pluginName);
            return;
        }
        LOGGER.info("Tier-3 probe {}: enabled={}, classloader={}",
                pluginName, plugin.isEnabled(), plugin.getClass().getClassLoader().getClass().getName());
        try {
            Class<?> serviceClass = Class.forName(serviceClassName, false, plugin.getClass().getClassLoader());
            RegisteredServiceProvider registration = server.getServicesManager().getRegistration((Class) serviceClass);
            if (registration == null) {
                LOGGER.warn("Tier-3 probe {}: service {} is not registered", pluginName, serviceClassName);
            } else {
                Object provider = registration.getProvider();
                LOGGER.info("Tier-3 probe {}: service {} provider={} priority={}",
                        pluginName, serviceClassName,
                        provider == null ? "null" : provider.getClass().getName(), registration.getPriority());
            }
        } catch (ClassNotFoundException ex) {
            LOGGER.warn("Tier-3 probe {}: service API class {} is not visible from its plugin classloader",
                    pluginName, serviceClassName);
        } catch (Throwable throwable) {
            LOGGER.warn("Tier-3 probe {} failed while checking service {}", pluginName, serviceClassName, throwable);
        }
    }
}
