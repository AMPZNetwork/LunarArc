package io.lunararcdevs.lunararc.common.mod.server;

import io.lunararcdevs.lunararc.api.LunarArcPlatform;
import io.lunararcdevs.lunararc.api.LunarArcServerApi;
import io.lunararcdevs.lunararc.api.LunarArcTickingTracker;
import io.lunararcdevs.lunararc.common.server.LunarArcVersionInfo;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

/**
 * The real, concrete implementation of {@link LunarArcServerApi} — wired to
 * {@link io.lunararcdevs.lunararc.api.LunarArcServer} once at boot from
 * {@link LunarArcServer#attach(net.minecraft.server.MinecraftServer)}.
 */
public final class LunarArcServerApiImpl implements LunarArcServerApi {

    public static final LunarArcServerApiImpl INSTANCE = new LunarArcServerApiImpl();

    private LunarArcServerApiImpl() {}

    @Override
    public LunarArcPlatform getPlatform() {
        return LunarArcPlatform.fromPlatformName(LunarArcServer.platformName());
    }

    @Override
    public void registerModEvent(Plugin plugin, Object eventBus, Object target) {
        // Forge and NeoForge both expose IEventBus#register(Object) with the exact same method
        // signature (NeoForge forked from Forge's eventbus API and kept this shape), so a
        // reflective call handles both without the api module needing a hard dependency on
        // either loader's classes. Fabric/Quilt don't use an event-bus object at all — they use
        // static per-event callback registration instead — so registerModEvent has no meaningful
        // equivalent there; fail clearly rather than silently doing nothing.
        if (eventBus == null) {
            throw new IllegalArgumentException("eventBus must not be null");
        }
        try {
            Method register = eventBus.getClass().getMethod("register", Object.class);
            register.invoke(eventBus, target);
        } catch (NoSuchMethodException e) {
            throw new UnsupportedOperationException(
                    "registerModEvent is not supported on this platform (" + getPlatform()
                            + ") — " + eventBus.getClass().getName() + " has no register(Object) method. "
                            + "This is expected on Fabric/Quilt, which use static event callback "
                            + "registration instead of an event-bus object.", e);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to register mod event for plugin " + plugin.getName(), e);
        }
    }

    @Override
    public LunarArcTickingTracker getTickingTracker() {
        return LunarArcTickingTrackerImpl.INSTANCE;
    }

    public static io.lunararcdevs.lunararc.api.LunarArcVersion buildVersion() {
        return new io.lunararcdevs.lunararc.api.LunarArcVersion(
                LunarArcVersionInfo.minecraftVersion(),
                LunarArcVersionInfo.lunarArcVersion(),
                LunarArcVersionInfo.paperApiVersion(),
                LunarArcVersionInfo.paperBuild());
    }
}
