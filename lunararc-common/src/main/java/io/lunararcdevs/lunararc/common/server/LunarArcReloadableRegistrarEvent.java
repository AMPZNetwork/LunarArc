package io.lunararcdevs.lunararc.common.server;

import io.papermc.paper.plugin.lifecycle.event.registrar.Registrar;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;

import java.util.Objects;

/** Concrete Paper lifecycle registrar event. */
public final class LunarArcReloadableRegistrarEvent<R extends Registrar> implements ReloadableRegistrarEvent<R> {
    private final R registrar;
    private final Cause cause;

    public LunarArcReloadableRegistrarEvent(R registrar, Cause cause) {
        this.registrar = Objects.requireNonNull(registrar, "registrar");
        this.cause = Objects.requireNonNull(cause, "cause");
    }

    @Override
    public R registrar() {
        return registrar;
    }

    @Override
    public Cause cause() {
        return cause;
    }
}
