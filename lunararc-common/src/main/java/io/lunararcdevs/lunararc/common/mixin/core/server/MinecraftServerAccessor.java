package io.lunararcdevs.lunararc.common.mixin.core.server;

import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Narrow CraftServer metrics/world-map access on the loader-owned MinecraftServer. */
@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor extends io.lunararcdevs.lunararc.common.bridge.access.MinecraftServerAccessBridge {
    @Accessor("tickTimesNanos") long[] lunararc$getTickTimesNanos();
    @Accessor("tickCount") int lunararc$getTickCount();
    @Accessor("levels") Map<ResourceKey<Level>, ServerLevel> lunararc$getLevels();
}
