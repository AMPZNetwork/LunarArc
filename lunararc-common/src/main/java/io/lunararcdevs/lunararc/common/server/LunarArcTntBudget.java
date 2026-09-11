package io.lunararcdevs.lunararc.common.server;

import io.lunararcdevs.lunararc.common.LunarArcServerAccess;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LunarArcTntBudget {

    /** Spigot's own default. */
    private static final int DEFAULT_MAX_TNT_PER_TICK = 100;

    private static final Map<String, State> BY_WORLD = new ConcurrentHashMap<>();

    private static final class State {
        long tick = Long.MIN_VALUE;
        int count;
        int max = DEFAULT_MAX_TNT_PER_TICK;
    }

    private LunarArcTntBudget() {
    }

    /** {@code true} if this tick still has room for one more TNT to actually explode. */
    public static boolean tryConsume(Level level) {
        org.bukkit.World world;
        try {
            world = LunarArcServerAccess.getCraftWorld(level);
        } catch (Throwable notReady) {
            // Before the Bukkit layer exists, vanilla decides.
            return true;
        }
        if (world == null) return true;

        State state = BY_WORLD.computeIfAbsent(world.getName(), name -> new State());
        long gameTime = level.getGameTime();
        synchronized (state) {
            if (state.tick != gameTime) {
                state.tick = gameTime;
                state.count = 0;
                state.max = readMax(world.getName());
            }
            if (state.count >= state.max) return false;
            state.count++;
            return true;
        }
    }

    private static int readMax(String worldName) {
        YamlConfiguration spigot = spigotConfig();
        if (spigot == null) return DEFAULT_MAX_TNT_PER_TICK;
        String perWorld = "world-settings." + worldName + ".max-tnt-per-tick";
        if (spigot.isInt(perWorld)) return spigot.getInt(perWorld);
        String shared = "world-settings.default.max-tnt-per-tick";
        if (spigot.isInt(shared)) return spigot.getInt(shared);
        return DEFAULT_MAX_TNT_PER_TICK;
    }

    private static YamlConfiguration spigotConfig() {
        try {
            return Bukkit.getServer().spigot().getSpigotConfig();
        } catch (Throwable unavailable) {
            return null;
        }
    }

    /** Forget cached values, so a reload picks up an edited spigot.yml. */
    public static void invalidate() {
        BY_WORLD.clear();
    }
}
