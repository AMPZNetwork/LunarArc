package org.bukkit.craftbukkit.ban;

import com.mojang.authlib.GameProfile;
import java.time.Instant;
import java.util.Date;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import org.bukkit.BanEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CraftProfileBanEntry implements BanEntry<com.destroystokyo.paper.profile.PlayerProfile> {
    private static final Date MINOR_DATE = Date.from(Instant.parse("1899-12-31T04:00:00Z"));
    private final UserBanList list;
    private final GameProfile profile;
    private Date created;
    private String source;
    private Date expiration;
    private String reason;

    public CraftProfileBanEntry(GameProfile profile, UserBanListEntry entry, UserBanList list) {
        this.profile = profile;
        this.list = list;
        this.created = entry.getCreated() == null ? null : new Date(entry.getCreated().getTime());
        this.source = entry.getSource();
        this.expiration = entry.getExpires() == null ? null : new Date(entry.getExpires().getTime());
        this.reason = entry.getReason();
    }

    @Override public @NotNull String getTarget() { return this.profile.getName() == null ? this.profile.getId().toString() : this.profile.getName(); }
    @Override public @NotNull com.destroystokyo.paper.profile.PlayerProfile getBanTarget() { return CraftProfileBanList.toBukkit(this.profile); }
    @Override public @Nullable Date getCreated() { return created == null ? null : (Date) created.clone(); }
    @Override public void setCreated(@NotNull Date created) { this.created = (Date) created.clone(); }
    @Override public @Nullable String getSource() { return source; }
    @Override public void setSource(@NotNull String source) { this.source = source; }
    @Override public @Nullable Date getExpiration() { return expiration == null ? null : (Date) expiration.clone(); }
    @Override public void setExpiration(@Nullable Date expiration) { this.expiration = expiration != null && expiration.getTime() == MINOR_DATE.getTime() ? null : expiration == null ? null : (Date) expiration.clone(); }
    @Override public @Nullable String getReason() { return reason; }
    @Override public void setReason(@Nullable String reason) { this.reason = reason; }
    @Override public void save() { this.list.add(new UserBanListEntry(this.profile, this.created, this.source, this.expiration, this.reason)); }
    @Override public void remove() { this.list.remove(this.profile); }
}
