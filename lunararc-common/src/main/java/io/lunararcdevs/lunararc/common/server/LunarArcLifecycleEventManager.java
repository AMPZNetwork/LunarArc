package io.lunararcdevs.lunararc.common.server;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;
import io.papermc.paper.plugin.lifecycle.event.handler.configuration.AbstractLifecycleEventHandlerConfiguration;
import io.papermc.paper.plugin.lifecycle.event.handler.configuration.LifecycleEventHandlerConfiguration;

import java.util.Objects;
import java.util.function.BooleanSupplier;

/** Concrete Paper lifecycle manager, modeled on Paper/Youer 1.21.1. */
public final class LunarArcLifecycleEventManager<O extends LifecycleEventOwner> implements LifecycleEventManager<O> {
    private final O owner;
    private final BooleanSupplier registrationCheck;

    private LunarArcLifecycleEventManager(O owner, BooleanSupplier registrationCheck) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.registrationCheck = Objects.requireNonNull(registrationCheck, "registrationCheck");
    }

    public static <O extends LifecycleEventOwner> LifecycleEventManager<O> create(
            O owner, BooleanSupplier registrationCheck) {
        return new LunarArcLifecycleEventManager<>(owner, registrationCheck);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void registerEventHandler(LifecycleEventHandlerConfiguration<? super O> handlerConfiguration) {
        Objects.requireNonNull(handlerConfiguration, "handlerConfiguration");
        if (!this.registrationCheck.getAsBoolean()) {
            throw new IllegalStateException("Cannot register lifecycle event handlers at this point in the plugin lifecycle");
        }
        if (!(handlerConfiguration instanceof AbstractLifecycleEventHandlerConfiguration config)) {
            throw new IllegalArgumentException("Lifecycle handler configuration was not created by LunarArc's Paper lifecycle provider");
        }
        config.registerFrom(this.owner);
    }
}
