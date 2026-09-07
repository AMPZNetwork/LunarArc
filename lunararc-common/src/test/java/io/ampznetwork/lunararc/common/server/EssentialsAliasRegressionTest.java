package io.ampznetwork.lunararc.common.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.resources.ResourceLocation;

public final class EssentialsAliasRegressionTest {
    public static void run() {
        Map<ResourceLocation, String> materials = new ConcurrentHashMap<>();
        materials.put(ResourceLocation.parse("example:gear"), "gear");
        AtomicInteger scans = new AtomicInteger();
        var index = new LunarArcEssentialsItemBridge.AliasIndex<>(() -> materials, item -> {
            scans.incrementAndGet();
            return true;
        });
        if (!"gear".equals(index.get("example_gear"))) throw new AssertionError("alias missing");
        for (int i = 0; i < 100000; i++) {
            if (index.get("missing_" + i) != null) throw new AssertionError("unknown alias resolved");
        }
        if (scans.get() != 1) throw new AssertionError("unknown aliases rescanned registry");
        materials.put(ResourceLocation.parse("example:cog"), "cog");
        if (!"cog".equals(index.get("example_cog"))) throw new AssertionError("new registration not indexed");
        if (scans.get() != 3) throw new AssertionError("unexpected index rebuilds");
        System.out.println("Essentials alias regressions passed");
    }
}
