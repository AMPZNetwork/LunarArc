package io.lunararcdevs.lunararc.common.mod.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class LunarArcLogicWorlds {

    private static final Logger LOGGER = LoggerFactory.getLogger("LunarArc");
    private static final Map<Class<?>, Boolean> SEEN = new ConcurrentHashMap<>();
    private static final Set<String> EXTRA = extraLogicWorlds();

    private LunarArcLogicWorlds() {
    }

    /** Whether Bukkit events may be fired against this level. */
    public static boolean isLogicWorld(LevelAccessor level) {
        return level != null && !level.isClientSide() && isLogicClass(level.getClass());
    }

    /** As {@link #isLogicWorld(LevelAccessor)}, for the read-only view vanilla often passes instead. */
    public static boolean isLogicWorld(BlockGetter getter) {
        return getter instanceof LevelAccessor level && isLogicWorld(level);
    }

    private static boolean isLogicClass(Class<?> type) {
        if (type == ServerLevel.class || type == WorldGenRegion.class) return true;
        return SEEN.computeIfAbsent(type, unseen -> {
            boolean admitted = EXTRA.contains(unseen.getName());
            LOGGER.warn("Level class {} treated as a logic world: {}. If a mod's world is wrongly "
                    + "excluded, add it with -Dlunararc.logic-worlds=<class names, comma separated>.",
                    unseen.getName(), admitted);
            return admitted;
        });
    }

    private static Set<String> extraLogicWorlds() {
        String configured = System.getProperty("lunararc.logic-worlds", "");
        Set<String> names = new java.util.HashSet<>();
        for (String part : configured.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) names.add(trimmed);
        }
        return Set.copyOf(names);
    }
}
