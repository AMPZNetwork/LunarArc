package io.lunararcdevs.lunararc.forge;

import io.lunararcdevs.lunararc.common.LunarArcClientSideGuard;
import io.lunararcdevs.lunararc.common.mod.server.LunarArcServer;
import io.lunararcdevs.lunararc.forge.command.ForgeCommandHook;
import io.lunararcdevs.lunararc.forge.server.ForgeServerLifecycle;
import io.lunararcdevs.lunararc.forge.network.ForgeChannelRegistration;
import io.lunararcdevs.lunararc.forge.event.ForgeBlockBreakEvents;
import io.lunararcdevs.lunararc.forge.event.ForgeBlockPlaceEvents;
import io.lunararcdevs.lunararc.forge.event.ForgeEntityTeleportEvents;
import io.lunararcdevs.lunararc.forge.event.ForgeEntityJoinEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod("lunararc")
public final class LunarArcForge {

    public LunarArcForge() {
        // Same as NeoForge: FML shows a mod constructor's exception on its error screen.
        LunarArcClientSideGuard.requireDedicatedServer(FMLEnvironment.dist == Dist.CLIENT);
        LunarArcServer.installPlatform("Forge", LunarArcForge.class.getClassLoader());
        io.lunararcdevs.lunararc.common.config.IncompatibleList.screenLoadedMods(
                lunararc$loadedMods());
        ForgeCommandHook.install();
        ForgeServerLifecycle.register(MinecraftForge.EVENT_BUS);
        ForgeChannelRegistration.register(MinecraftForge.EVENT_BUS);
        ForgeBlockBreakEvents.register(MinecraftForge.EVENT_BUS);
        ForgeBlockPlaceEvents.register(MinecraftForge.EVENT_BUS);
        ForgeEntityTeleportEvents.register(MinecraftForge.EVENT_BUS);
        ForgeEntityJoinEvents.register(MinecraftForge.EVENT_BUS);
    }

    private static java.util.Map<String, String> lunararc$loadedMods() {
        java.util.Map<String, String> mods = new java.util.HashMap<>();
        java.lang.reflect.Method getVersion;
        try {
            getVersion = net.minecraftforge.forgespi.language.IModInfo.class.getMethod("getVersion");
        } catch (ReflectiveOperationException | RuntimeException unavailable) {
            getVersion = null;
        }
        for (net.minecraftforge.forgespi.language.IModInfo mod : net.minecraftforge.fml.ModList.get().getMods()) {
            String version = null;
            if (getVersion != null) {
                try {
                    Object value = getVersion.invoke(mod);
                    if (value != null) version = value.toString();
                } catch (ReflectiveOperationException | RuntimeException ignored) {
                }
            }
            mods.put(mod.getModId(), version);
        }
        return mods;
    }
}
