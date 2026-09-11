package io.lunararcdevs.lunararc.common.server;

import io.papermc.paper.registry.RegistryAccess;

import java.util.Optional;
import java.util.ServiceLoader;

public final class RegistryAccessRegressionTest {
    public static void run() {
        ServiceLoader<RegistryAccess> loader = ServiceLoader.load(
                RegistryAccess.class, RegistryAccessRegressionTest.class.getClassLoader());
        Optional<RegistryAccess> found = loader.findFirst();
        check(found.isPresent(), "ServiceLoader found no RegistryAccess provider - "
                + "org.bukkit.Registry's static init crashes with this on every real boot");
        check(found.get() instanceof LunarArcRegistryAccess,
                "ServiceLoader resolved to the wrong RegistryAccess implementation: " + found.get().getClass());
        System.out.println("RegistryAccess ServiceLoader regressions passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
