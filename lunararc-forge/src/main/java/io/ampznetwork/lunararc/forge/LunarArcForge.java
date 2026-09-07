package io.ampznetwork.lunararc.forge;

import io.ampznetwork.lunararc.common.LunarArcClientSideGuard;
import io.ampznetwork.lunararc.common.mod.server.LunarArcServer;
import io.ampznetwork.lunararc.forge.command.ForgeCommandHook;
import io.ampznetwork.lunararc.forge.server.ForgeServerLifecycle;
import io.ampznetwork.lunararc.forge.network.ForgeChannelRegistration;
import io.ampznetwork.lunararc.forge.event.ForgeBlockBreakEvents;
import io.ampznetwork.lunararc.forge.event.ForgeBlockPlaceEvents;
import io.ampznetwork.lunararc.forge.event.ForgeEntityTeleportEvents;
import io.ampznetwork.lunararc.forge.event.ForgeEntityJoinEvents;
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
        io.ampznetwork.lunararc.common.config.IncompatibleList.screenLoadedMods(
                lunararc$loadedMods());
        ForgeCommandHook.install();
        ForgeServerLifecycle.register(MinecraftForge.EVENT_BUS);
        ForgeChannelRegistration.register(MinecraftForge.EVENT_BUS);
        ForgeBlockBreakEvents.register(MinecraftForge.EVENT_BUS);
        ForgeBlockPlaceEvents.register(MinecraftForge.EVENT_BUS);
        ForgeEntityTeleportEvents.register(MinecraftForge.EVENT_BUS);
        ForgeEntityJoinEvents.register(MinecraftForge.EVENT_BUS);
    }

    /**
     * Mod ID to version for every loaded mod.
     *
     * <p>getVersion() is called reflectively, and deliberately. It returns Maven's ArtifactVersion,
     * and the shaded runtime relocates org.apache.maven into io.ampznetwork.lunararc.libs.maven -
     * which rewrites the return type in our call site's descriptor, so a direct call goes looking
     * for a method Forge does not have and the mod fails to construct:</p>
     *
     * <pre>
     *   NoSuchMethodError: 'io.ampznetwork.lunararc.libs.maven.core.artifact.versioning
     *   .ArtifactVersion net.minecraftforge.forgespi.language.IModInfo.getVersion()'
     * </pre>
     *
     * <p>Looking the method up by name emits no descriptor for the relocator to touch. A version
     * that cannot be read is left null, which still matches any incompatible entry that does not pin
     * one - the screening is a warning, and losing a version is not worth failing a boot over.</p>
     */
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
