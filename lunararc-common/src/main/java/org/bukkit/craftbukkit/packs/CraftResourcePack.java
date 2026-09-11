package org.bukkit.craftbukkit.packs;

import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.packs.ResourcePack;

public final class CraftResourcePack implements ResourcePack {
    private final MinecraftServer.ServerResourcePackInfo handle;

    public CraftResourcePack(MinecraftServer.ServerResourcePackInfo handle) {
        this.handle = java.util.Objects.requireNonNull(handle, "handle");
    }

    public MinecraftServer.ServerResourcePackInfo getHandle() {
        return this.handle;
    }

    @Override
    public UUID getId() {
        return this.handle.id();
    }

    @Override
    public String getUrl() {
        return this.handle.url();
    }

    @Override
    public String getHash() {
        return this.handle.hash();
    }

    @Override
    public String getPrompt() {
        return this.handle.prompt() == null ? "" : CraftChatMessage.fromComponent(this.handle.prompt());
    }

    @Override
    public boolean isRequired() {
        return this.handle.isRequired();
    }

    @Override
    public String toString() {
        return "CraftResourcePack{id=" + getId() + ",url=" + getUrl() + ",hash=" + getHash()
                + ",prompt=" + getPrompt() + ",required=" + isRequired() + "}";
    }
}
