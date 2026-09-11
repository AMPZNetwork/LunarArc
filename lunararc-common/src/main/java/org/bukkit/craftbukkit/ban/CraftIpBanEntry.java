package org.bukkit.craftbukkit.ban;

import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.time.Instant;
import java.util.Date;
import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.IpBanListEntry;
import org.bukkit.BanEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CraftIpBanEntry implements BanEntry<InetAddress> {
    private static final Date MINOR_DATE = Date.from(Instant.parse("1899-12-31T04:00:00Z"));
    private final IpBanList list;
    private final String target;
    private Date created;
    private String source;
    private Date expiration;
    private String reason;

    public CraftIpBanEntry(String target, IpBanListEntry entry, IpBanList list) {
        this.list = list;
        this.target = target;
        this.created = entry.getCreated() == null ? null : new Date(entry.getCreated().getTime());
        this.source = entry.getSource();
        this.expiration = entry.getExpires() == null ? null : new Date(entry.getExpires().getTime());
        this.reason = entry.getReason();
    }

    @Override public @NotNull String getTarget() { return this.target; }
    @Override public @NotNull InetAddress getBanTarget() { return InetAddresses.forString(this.target); }
    @Override public @Nullable Date getCreated() { return this.created == null ? null : (Date) this.created.clone(); }
    @Override public void setCreated(@NotNull Date created) { this.created = (Date) created.clone(); }
    @Override public @Nullable String getSource() { return this.source; }
    @Override public void setSource(@NotNull String source) { this.source = source; }
    @Override public @Nullable Date getExpiration() { return this.expiration == null ? null : (Date) this.expiration.clone(); }
    @Override public void setExpiration(@Nullable Date expiration) {
        this.expiration = expiration != null && expiration.getTime() == MINOR_DATE.getTime() ? null
                : expiration == null ? null : (Date) expiration.clone();
    }
    @Override public @Nullable String getReason() { return this.reason; }
    @Override public void setReason(@Nullable String reason) { this.reason = reason; }

    @Override
    public void save() {
        this.list.add(new IpBanListEntry(this.target, this.created, this.source, this.expiration, this.reason));
    }

    @Override public void remove() { this.list.remove(this.target); }
}
