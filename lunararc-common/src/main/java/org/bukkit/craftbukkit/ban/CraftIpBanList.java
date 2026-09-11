package org.bukkit.craftbukkit.ban;

import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.IpBanListEntry;
import org.bukkit.BanEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"rawtypes", "deprecation"})
public final class CraftIpBanList implements org.bukkit.ban.IpBanList {
    private final IpBanList list;

    public CraftIpBanList(IpBanList list) { this.list = java.util.Objects.requireNonNull(list, "list"); }

    @Override public @Nullable BanEntry<InetAddress> getBanEntry(@NotNull String target) {
        IpBanListEntry entry = this.list.get(java.util.Objects.requireNonNull(target, "target"));
        return entry == null ? null : new CraftIpBanEntry(target, entry, this.list);
    }
    @Override public @Nullable BanEntry<InetAddress> getBanEntry(@NotNull InetAddress target) { return getBanEntry(ip(target)); }

    @Override public @Nullable BanEntry<InetAddress> addBan(@NotNull String target, @Nullable String reason, @Nullable Date expires, @Nullable String source) {
        java.util.Objects.requireNonNull(target, "target");
        IpBanListEntry entry = new IpBanListEntry(target, new Date(), blankToNull(source), expires, blankToNull(reason));
        this.list.add(entry);
        return new CraftIpBanEntry(target, entry, this.list);
    }
    @Override public @Nullable BanEntry<InetAddress> addBan(@NotNull InetAddress target, @Nullable String reason, @Nullable Date expires, @Nullable String source) { return addBan(ip(target), reason, expires, source); }
    @Override public @Nullable BanEntry<InetAddress> addBan(@NotNull InetAddress target, @Nullable String reason, @Nullable Instant expires, @Nullable String source) { return addBan(target, reason, expires == null ? null : Date.from(expires), source); }
    @Override public @Nullable BanEntry<InetAddress> addBan(@NotNull InetAddress target, @Nullable String reason, @Nullable Duration duration, @Nullable String source) { return addBan(target, reason, duration == null ? null : Instant.now().plus(duration), source); }

    @Override public Set getBanEntries() { return new LinkedHashSet<>(getEntries()); }
    @Override public @NotNull Set<BanEntry<InetAddress>> getEntries() {
        Set<BanEntry<InetAddress>> result = new LinkedHashSet<>();
        for (String target : this.list.getUserList()) {
            IpBanListEntry entry = this.list.get(target);
            if (entry != null) result.add(new CraftIpBanEntry(target, entry, this.list));
        }
        return java.util.Collections.unmodifiableSet(result);
    }
    @Override public boolean isBanned(@NotNull String target) { return this.list.isBanned(java.util.Objects.requireNonNull(target, "target")); }
    @Override public boolean isBanned(@NotNull InetAddress target) { return isBanned(ip(target)); }
    @Override public void pardon(@NotNull String target) { this.list.remove(java.util.Objects.requireNonNull(target, "target")); }
    @Override public void pardon(@NotNull InetAddress target) { pardon(ip(target)); }

    private static String ip(InetAddress address) { return InetAddresses.toAddrString(java.util.Objects.requireNonNull(address, "address")); }
    private static String blankToNull(String value) { return value == null || value.isBlank() ? null : value; }
}
