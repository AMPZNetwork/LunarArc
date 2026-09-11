package io.lunararcdevs.lunararc.common.server;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEvent;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;
import io.papermc.paper.plugin.lifecycle.event.types.AbstractLifecycleEventType;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEventType;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * Concrete lifecycle-event registry/runner. This is intentionally scoped only
 * to Paper lifecycle events; it is not a platform or gameplay dispatcher.
 */
public final class LunarArcLifecycleEventRunner {
    private static final List<AbstractLifecycleEventType<?, ?, ?>> TYPES = new CopyOnWriteArrayList<>();
    private static volatile boolean blocksPluginReloading;
    private static final ThreadLocal<LifecycleEventOwner> CURRENT_OWNER = new ThreadLocal<>();

    private LunarArcLifecycleEventRunner() {
    }

    public static void registerType(AbstractLifecycleEventType<?, ?, ?> type) {
        if (!TYPES.contains(type)) TYPES.add(type);
    }

    public static <O extends LifecycleEventOwner> void checkRegisteredHandler(
            O owner, AbstractLifecycleEventType<O, ?, ?> eventType) {
        if (eventType.blocksReloading(owner)) blocksPluginReloading = true;
    }

    public static boolean blocksPluginReloading() {
        return blocksPluginReloading;
    }

    /**
     * Release server-run state on final shutdown without discarding the static
     * Paper lifecycle event type registry. This keeps same-JVM test/restart
     * cycles from inheriting a previous server's reload-blocking state or
     * thread-local owner reference.
     */
    public static void resetServerState() {
        blocksPluginReloading = false;
        CURRENT_OWNER.remove();
    }

    /** Owner whose lifecycle handler is currently executing on this thread. */
    public static LifecycleEventOwner currentOwner() {
        return CURRENT_OWNER.get();
    }

    @SuppressWarnings("unchecked")
    public static <O extends LifecycleEventOwner, E extends LifecycleEvent> void fire(
            LifecycleEventType<O, ? super E, ?> eventType,
            E event,
            Predicate<? super O> ownerPredicate) {
        if (!(eventType instanceof AbstractLifecycleEventType<?, ?, ?> raw)) {
            throw new IllegalArgumentException("Lifecycle event type is not backed by LunarArc: " + eventType.name());
        }
        AbstractLifecycleEventType<O, E, ?> concrete = (AbstractLifecycleEventType<O, E, ?>) raw;
        concrete.forEachHandler(event, registered -> {
            LifecycleEventOwner previous = CURRENT_OWNER.get();
            LifecycleEventOwner owner = (LifecycleEventOwner) registered.owner();
            CURRENT_OWNER.set(owner);
            Thread thread = Thread.currentThread();
            ClassLoader previousLoader = thread.getContextClassLoader();
            try {
                ClassLoader handlerLoader = registered.lifecycleEventHandler().getClass().getClassLoader();
                if (handlerLoader != null) thread.setContextClassLoader(handlerLoader);
                registered.lifecycleEventHandler().run(event);
            } catch (Throwable throwable) {
                String ownerName;
                try { ownerName = owner.getPluginMeta().getDisplayName(); }
                catch (Throwable ignored) { ownerName = String.valueOf(owner); }
                throw new RuntimeException("Could not run '" + concrete.name()
                        + "' lifecycle event handler from " + ownerName, throwable);
            } finally {
                thread.setContextClassLoader(previousLoader);
                if (previous == null) CURRENT_OWNER.remove();
                else CURRENT_OWNER.set(previous);
            }
        }, registered -> ownerPredicate.test((O) registered.owner()));
    }

    public static <O extends LifecycleEventOwner, E extends LifecycleEvent> void fire(
            LifecycleEventType<O, ? super E, ?> eventType, E event) {
        fire(eventType, event, owner -> true);
    }

    public static void unregisterAll(Plugin plugin) {
        if (plugin == null) return;
        for (AbstractLifecycleEventType<?, ?, ?> type : TYPES) unregisterAllFromType(type, plugin);
    }

    /** Remove every lifecycle handler owned by a bootstrap context or plugin instance. */
    public static void unregisterAllOwner(LifecycleEventOwner owner) {
        if (owner == null) return;
        for (AbstractLifecycleEventType<?, ?, ?> type : TYPES) unregisterAllOwnerFromType(type, owner);
    }

    @SuppressWarnings("unchecked")
    private static <O extends LifecycleEventOwner, E extends LifecycleEvent> void unregisterAllOwnerFromType(
            AbstractLifecycleEventType<?, ?, ?> rawType, LifecycleEventOwner owner) {
        AbstractLifecycleEventType<O, E, ?> type = (AbstractLifecycleEventType<O, E, ?>) rawType;
        type.removeMatching(registered -> registered.owner() == owner);
    }

    @SuppressWarnings("unchecked")
    private static <O extends LifecycleEventOwner, E extends LifecycleEvent> void unregisterAllFromType(
            AbstractLifecycleEventType<?, ?, ?> rawType, Plugin plugin) {
        AbstractLifecycleEventType<O, E, ?> type = (AbstractLifecycleEventType<O, E, ?>) rawType;
        type.removeMatching(registered -> {
            O owner = registered.owner();
            if (owner == plugin) return true;
            try {
                return owner.getPluginMeta().getName().equals(plugin.getPluginMeta().getName());
            } catch (Throwable ignored) {
                return false;
            }
        });
    }
}
