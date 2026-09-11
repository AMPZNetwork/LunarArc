package io.lunararcdevs.lunararc.common.bridge;

import net.kyori.adventure.bossbar.BossBar;

/** Exposes the {@code adventure} field real Paper adds to {@code net.minecraft.world.BossEvent}. */
public interface BossEventBridge {
    BossBar lunararc$getAdventureBossBar();
    void lunararc$setAdventureBossBar(BossBar bar);
}
