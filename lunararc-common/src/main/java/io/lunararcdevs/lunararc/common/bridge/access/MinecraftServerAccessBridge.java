package io.lunararcdevs.lunararc.common.bridge.access;

import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/** Normal runtime bridge implemented by the corresponding Mixin accessor/invoker. */
public interface MinecraftServerAccessBridge {
    long[] lunararc$getTickTimesNanos();
    int lunararc$getTickCount();
    Map<ResourceKey<Level>, ServerLevel> lunararc$getLevels();
}
