package io.ampznetwork.lunararc;

import io.ampznetwork.lunararc.common.compat.SableCompatibility;
import net.minecraft.world.phys.AABB;

public final class SableCompatibilityRegressionTest {
    public static void run() {
        check(SableCompatibility.isSafeEntityQuery(new AABB(0, 0, 0, 1, 2, 1)), "normal collision query rejected");
        check(SableCompatibility.isSafeEntityQuery(new AABB(1.0E9, -2048, -1.0E9, 1.0E9 + 16, -2032, -1.0E9 + 16)),
                "valid sublevel storage coordinates rejected");
        check(SableCompatibility.isSafeEntityQuery(new AABB(0, 0, 0, 10000, 10000, 10000)), "upstream size boundary rejected");
        check(!SableCompatibility.isSafeEntityQuery(new AABB(0, 0, 0, 10001, 10001, 10001)), "oversized query accepted");
        check(!SableCompatibility.isSafeEntityQuery(new AABB(Double.NaN, 0, 0, 1, 1, 1)), "NaN query accepted");
        check(!SableCompatibility.isSafeEntityQuery(new AABB(0, 0, 0, Double.POSITIVE_INFINITY, 1, 1)), "infinite query accepted");
        check(!SableCompatibility.isSafeEntityQuery(new AABB(-Double.MAX_VALUE, 0, 0, Double.MAX_VALUE, 1, 1)), "overflowing query accepted");
        check(!SableCompatibility.isSafeEntityQuery(null), "null query accepted");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
