package io.lunararcdevs.lunararc.fabric;

import io.lunararcdevs.lunararc.common.LunarArcClientSideGuard;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public final class LunarArcFabricPreLaunch implements PreLaunchEntrypoint {

    @Override
    public void onPreLaunch() {
        LunarArcClientSideGuard.requireDedicatedServer(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT);
    }
}
