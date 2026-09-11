package org.bukkit.craftbukkit.event;

import org.bukkit.Location;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;

public final class CraftPortalEvent {
    private final Location to;
    private final int searchRadius;
    private final int creationRadius;
    private final boolean canCreatePortal;

    public CraftPortalEvent(EntityPortalEvent event) {
        this.to = event.getTo();
        this.searchRadius = event.getSearchRadius();
        this.creationRadius = event.getCreationRadius();
        this.canCreatePortal = event.getCanCreatePortal();
    }

    public CraftPortalEvent(PlayerPortalEvent event) {
        this.to = event.getTo();
        this.searchRadius = event.getSearchRadius();
        this.creationRadius = event.getCreationRadius();
        this.canCreatePortal = event.getCanCreatePortal();
    }

    public Location getTo() { return to; }
    public int getSearchRadius() { return searchRadius; }
    public int getCreationRadius() { return creationRadius; }
    public boolean getCanCreatePortal() { return canCreatePortal; }
}
