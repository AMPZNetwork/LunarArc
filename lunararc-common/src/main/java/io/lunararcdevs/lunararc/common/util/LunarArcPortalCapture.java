package io.lunararcdevs.lunararc.common.util;

import net.minecraft.world.entity.Entity;

/** Thread-confined capture for associating vanilla End-platform creation with its triggering entity. */
public final class LunarArcPortalCapture {
    private static final ThreadLocal<Entity> END_PLATFORM_ENTITY = new ThreadLocal<>();

    private LunarArcPortalCapture() {}

    public static void pushEndPlatformEntity(Entity entity) {
        END_PLATFORM_ENTITY.set(entity);
    }

    public static Entity endPlatformEntity() {
        return END_PLATFORM_ENTITY.get();
    }

    public static void clearEndPlatformEntity() {
        END_PLATFORM_ENTITY.remove();
    }
}
