package io.ampznetwork.lunararc.quilt;

import io.ampznetwork.lunararc.common.LunarArcClientSideGuard;
import io.ampznetwork.lunararc.common.mod.server.LunarArcServer;
import io.ampznetwork.lunararc.quilt.event.QuiltBlockBreakEvents;
import io.ampznetwork.lunararc.quilt.network.QuiltChannelRegistration;
import io.ampznetwork.lunararc.quilt.server.QuiltServerLifecycle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class LunarArcQuilt implements ModInitializer {
    @Override
    public void onInitialize() {
        // Quilt runs LunarArc through its Fabric compatibility layer - hence ModInitializer
        // above - so the Fabric loader API answers this here too.
        LunarArcClientSideGuard.requireDedicatedServer(
                FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT);
        LunarArcServer.installPlatform("Quilt", LunarArcQuilt.class.getClassLoader());
        io.ampznetwork.lunararc.common.config.IncompatibleList.screenLoadedMods(
                FabricLoader.getInstance().getAllMods().stream()
                        .collect(java.util.HashMap::new,
                                (map, mod) -> map.put(mod.getMetadata().getId(),
                                        mod.getMetadata().getVersion().getFriendlyString()),
                                java.util.HashMap::putAll));
        QuiltServerLifecycle.register();
        QuiltChannelRegistration.register();
        QuiltBlockBreakEvents.register();
    }
}
