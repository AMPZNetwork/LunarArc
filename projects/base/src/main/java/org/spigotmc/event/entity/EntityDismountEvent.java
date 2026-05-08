package org.spigotmc.event.entity;

import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;

public class EntityDismountEvent extends EntityEvent {
    private static final HandlerList handlers = new HandlerList();
    private final Entity exited;

    public EntityDismountEvent(@NotNull Entity what, @NotNull Entity exited) {
        super(what);
        this.exited = exited;
    }

    public @NotNull Entity getDismounted() {
        return entity;
    }

    public @NotNull Entity getEntity() {
        return exited;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
