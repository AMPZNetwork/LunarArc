package org.bukkit.craftbukkit;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.server.ServerLinks;
import net.minecraft.server.dedicated.DedicatedServer;
import org.bukkit.craftbukkit.util.CraftChatMessage;

public final class CraftServerLinks implements org.bukkit.ServerLinks {
    private final DedicatedServer server;
    private ServerLinks links;

    public CraftServerLinks(DedicatedServer server) {
        this.server = Objects.requireNonNull(server, "server");
    }

    public CraftServerLinks(ServerLinks links) {
        this.server = null;
        this.links = Objects.requireNonNull(links, "links");
    }

    @Override
    public ServerLink getLink(Type type) {
        Objects.requireNonNull(type, "type");
        return getHandle().findKnownType(toNms(type)).map(CraftServerLink::new).orElse(null);
    }

    @Override
    public List<ServerLink> getLinks() {
        return getHandle().entries().stream().map(entry -> (ServerLink) new CraftServerLink(entry)).toList();
    }

    @Override
    public ServerLink setLink(Type type, URI url) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(url, "url");
        ServerLink existing = getLink(type);
        if (existing != null) removeLink(existing);
        return addLink(type, url);
    }

    @Override
    public ServerLink addLink(Type type, URI url) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(url, "url");
        CraftServerLink link = new CraftServerLink(ServerLinks.Entry.knownType(toNms(type), url));
        add(link);
        return link;
    }

    @Override
    public ServerLink addLink(net.kyori.adventure.text.Component displayName, URI url) {
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(url, "url");
        CraftServerLink link = new CraftServerLink(ServerLinks.Entry.custom(
                io.lunararcdevs.lunararc.common.messaging.LunarArcComponentPipeline.fromAdventure(displayName), url));
        add(link);
        return link;
    }

    @Override
    @Deprecated
    public ServerLink addLink(String displayName, URI url) {
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(url, "url");
        CraftServerLink link = new CraftServerLink(ServerLinks.Entry.custom(CraftChatMessage.fromStringOrNull(displayName), url));
        add(link);
        return link;
    }

    private void add(CraftServerLink link) {
        List<ServerLinks.Entry> entries = new ArrayList<>(getHandle().entries());
        entries.add(link.handle);
        setHandle(new ServerLinks(entries));
    }

    @Override
    public boolean removeLink(ServerLink link) {
        Objects.requireNonNull(link, "link");
        if (!(link instanceof CraftServerLink craftLink)) {
            throw new IllegalArgumentException("ServerLink was not created by this server");
        }
        List<ServerLinks.Entry> entries = new ArrayList<>(getHandle().entries());
        boolean removed = entries.remove(craftLink.handle);
        if (removed) setHandle(new ServerLinks(entries));
        return removed;
    }

    @Override
    public org.bukkit.ServerLinks copy() {
        return new CraftServerLinks(getHandle());
    }

    public ServerLinks getHandle() {
        if (server != null) {
            return server.serverLinks;
        }
        return links;
    }

    private void setHandle(ServerLinks links) {
        if (server != null) {
            server.serverLinks = links;
        } else {
            this.links = links;
        }
    }

    private static ServerLinks.KnownLinkType toNms(Type type) {
        return ServerLinks.KnownLinkType.values()[type.ordinal()];
    }

    private static Type toBukkit(ServerLinks.KnownLinkType type) {
        return Type.values()[type.ordinal()];
    }

    public static final class CraftServerLink implements ServerLink {
        private final ServerLinks.Entry handle;

        private CraftServerLink(ServerLinks.Entry handle) {
            this.handle = handle;
        }

        @Override
        public Type getType() {
            return handle.type().left().map(CraftServerLinks::toBukkit).orElse(null);
        }

        @Override
        public net.kyori.adventure.text.Component displayName() {
            return io.lunararcdevs.lunararc.common.messaging.LunarArcComponentPipeline.toAdventure(handle.displayName());
        }

        @Override
        @Deprecated
        public String getDisplayName() {
            return io.lunararcdevs.lunararc.common.messaging.LunarArcComponentPipeline.toLegacy(displayName());
        }

        @Override
        public URI getUrl() {
            return handle.link();
        }
    }

    /** CraftBukkit's name for {@link #getHandle}; both return the live NMS ServerLinks. */
    public ServerLinks getServerLinks() {
        return this.getHandle();
    }
}
