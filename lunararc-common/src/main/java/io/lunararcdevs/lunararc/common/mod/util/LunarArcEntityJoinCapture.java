package io.lunararcdevs.lunararc.common.mod.util;

import java.util.UUID;
import net.minecraft.world.entity.Entity;
import org.bukkit.event.Cancellable;

/** Thread-confined link between a Bukkit spawn event and a loader EntityJoinLevelEvent. */
public final class LunarArcEntityJoinCapture {
    private LunarArcEntityJoinCapture() {}

    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();

    public static void capture(Entity entity, Cancellable event) {
        CURRENT.set(new Context(entity.getUUID(), event));
    }

    public static Cancellable matching(Entity entity) {
        Context context = CURRENT.get();
        return context != null && context.entityId.equals(entity.getUUID()) ? context.event : null;
    }

    public static void clear() {
        CURRENT.remove();
    }

    private record Context(UUID entityId, Cancellable event) {}
}
