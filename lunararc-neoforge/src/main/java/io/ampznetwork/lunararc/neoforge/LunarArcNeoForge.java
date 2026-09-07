package io.ampznetwork.lunararc.neoforge;

import io.ampznetwork.lunararc.common.LunarArcClientSideGuard;
import io.ampznetwork.lunararc.common.mod.server.LunarArcServer;
import io.ampznetwork.lunararc.neoforge.command.NeoForgeCommandHook;
import io.ampznetwork.lunararc.neoforge.server.NeoForgeServerLifecycle;
import io.ampznetwork.lunararc.neoforge.event.NeoForgeBlockBreakEvents;
import io.ampznetwork.lunararc.neoforge.event.NeoForgeBlockPlaceEvents;
import io.ampznetwork.lunararc.neoforge.event.NeoForgeEntityTeleportEvents;
import io.ampznetwork.lunararc.neoforge.event.NeoForgeEntityJoinEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod("lunararc")
public final class LunarArcNeoForge {

    public LunarArcNeoForge(IEventBus modBus) {
        // Before anything is wired up: FML surfaces an exception thrown from a mod constructor
        // on its mod-loading error screen, so this is what a client user actually reads.
        LunarArcClientSideGuard.requireDedicatedServer(FMLEnvironment.dist == Dist.CLIENT);
        LunarArcServer.installPlatform("NeoForge", LunarArcNeoForge.class.getClassLoader());
        io.ampznetwork.lunararc.common.config.IncompatibleList.screenLoadedMods(
                lunararc$loadedMods());
        NeoForgeCommandHook.install();
        NeoForgeServerLifecycle.register();
        NeoForgeBlockBreakEvents.register();
        NeoForgeBlockPlaceEvents.register();
        NeoForgeEntityTeleportEvents.register();
        NeoForgeEntityJoinEvents.register();
    }

    /**
     * Mod ID to version for every loaded mod.
     *
     * <p>getVersion() is called reflectively, and deliberately. It returns Maven's ArtifactVersion,
     * and the shaded runtime relocates org.apache.maven into io.ampznetwork.lunararc.libs.maven -
     * which rewrites the return type in our call site's descriptor, so a direct call goes looking
     * for a method NeoForge does not have and the mod fails to construct:</p>
     *
     * <pre>
     *   NoSuchMethodError: 'io.ampznetwork.lunararc.libs.maven.core.artifact.versioning
     *   .ArtifactVersion net.neoforged.neoforgespi.language.IModInfo.getVersion()'
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
            getVersion = net.neoforged.neoforgespi.language.IModInfo.class.getMethod("getVersion");
        } catch (ReflectiveOperationException | RuntimeException unavailable) {
            getVersion = null;
        }
        for (net.neoforged.neoforgespi.language.IModInfo mod : net.neoforged.fml.ModList.get().getMods()) {
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
