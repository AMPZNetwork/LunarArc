package io.ampznetwork.lunararc.common.compat;

import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.world.phys.AABB;

public final class SableCompatibility {
    private static final double MAX_QUERY_SIZE = 10_000.0;
    private static final AtomicBoolean REPORTED_INVALID_QUERY = new AtomicBoolean();

    private SableCompatibility() {}

    public static boolean isSafeEntityQuery(AABB bounds) {
        return bounds != null
                && Double.isFinite(bounds.minX) && Double.isFinite(bounds.minY) && Double.isFinite(bounds.minZ)
                && Double.isFinite(bounds.maxX) && Double.isFinite(bounds.maxY) && Double.isFinite(bounds.maxZ)
                && Double.isFinite(bounds.getSize()) && bounds.getSize() <= MAX_QUERY_SIZE;
    }

    public static boolean allowEntityQuery(AABB bounds) {
        if (isSafeEntityQuery(bounds)) return true;
        if (REPORTED_INVALID_QUERY.compareAndSet(false, true)) {
            org.slf4j.LoggerFactory.getLogger(SableCompatibility.class).warn(
                    "Blocked an invalid or excessively large Sable entity query: {}. Further occurrences will not be logged.", bounds);
        }
        return false;
    }
}
