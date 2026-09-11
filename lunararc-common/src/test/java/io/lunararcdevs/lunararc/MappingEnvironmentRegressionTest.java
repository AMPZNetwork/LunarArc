package io.lunararcdevs.lunararc;

import io.papermc.paper.util.MappingEnvironment;

public final class MappingEnvironmentRegressionTest {
    public static void run() {
        check(!MappingEnvironment.isReobfuscatedName("MobCategory"),
                "modern Mojang-mapped name must not be treated as reobfuscated");
        check(MappingEnvironment.isReobfuscatedName("EnumCreatureType"),
                "legacy Spigot/CraftBukkit reobfuscated name must be detected");
        check(!MappingEnvironment.isReobfuscatedName("class_1311"),
                "Fabric's intermediary name for MobCategory regressed - this exact input crashed a real Fabric boot");
        check(!MappingEnvironment.isReobfuscatedName("class_9999"),
                "any unrecognized name must be treated as not reobfuscated, never throw");
        System.out.println("Mapping environment regressions passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
