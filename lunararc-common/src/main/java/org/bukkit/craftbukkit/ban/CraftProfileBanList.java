package org.bukkit.craftbukkit.ban;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import org.bukkit.BanEntry;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"rawtypes", "deprecation"})
public final class CraftProfileBanList implements org.bukkit.ban.ProfileBanList {
    private final UserBanList list;
    public CraftProfileBanList(UserBanList list) { this.list = java.util.Objects.requireNonNull(list, "list"); }

    @Override public @Nullable BanEntry<com.destroystokyo.paper.profile.PlayerProfile> getBanEntry(@NotNull String target) { return getBanEntry(profile(target)); }
    @Override public @Nullable BanEntry<com.destroystokyo.paper.profile.PlayerProfile> getBanEntry(@NotNull PlayerProfile target) { return getBanEntry(toGameProfile(target)); }
    @Override public @Nullable BanEntry<com.destroystokyo.paper.profile.PlayerProfile> getBanEntry(@NotNull com.destroystokyo.paper.profile.PlayerProfile target) { return getBanEntry(toGameProfile(target)); }

    private BanEntry<com.destroystokyo.paper.profile.PlayerProfile> getBanEntry(GameProfile profile) {
        if (profile == null) return null;
        UserBanListEntry entry = this.list.get(profile);
        return entry == null ? null : new CraftProfileBanEntry(profile, entry, this.list);
    }

    @Override public @Nullable BanEntry<com.destroystokyo.paper.profile.PlayerProfile> addBan(@NotNull String target, @Nullable String reason, @Nullable Date expires, @Nullable String source) { return addBan(profileByName(target), reason, expires, source); }
    @Override public @Nullable BanEntry<com.destroystokyo.paper.profile.PlayerProfile> addBan(@NotNull PlayerProfile target, @Nullable String reason, @Nullable Date expires, @Nullable String source) { return addBan(toGameProfile(target), reason, expires, source); }
    @Override public @Nullable BanEntry<com.destroystokyo.paper.profile.PlayerProfile> addBan(@NotNull PlayerProfile target, @Nullable String reason, @Nullable Instant expires, @Nullable String source) { return addBan(target, reason, expires == null ? null : Date.from(expires), source); }
    @Override public @Nullable BanEntry<com.destroystokyo.paper.profile.PlayerProfile> addBan(@NotNull PlayerProfile target, @Nullable String reason, @Nullable Duration duration, @Nullable String source) { return addBan(target, reason, duration == null ? null : Instant.now().plus(duration), source); }
    @Override public @Nullable BanEntry<com.destroystokyo.paper.profile.PlayerProfile> addBan(@NotNull com.destroystokyo.paper.profile.PlayerProfile target, @Nullable String reason, @Nullable Date expires, @Nullable String source) { return addBan(toGameProfile(target), reason, expires, source); }
    @Override public @Nullable BanEntry<com.destroystokyo.paper.profile.PlayerProfile> addBan(@NotNull com.destroystokyo.paper.profile.PlayerProfile target, @Nullable String reason, @Nullable Instant expires, @Nullable String source) { return addBan(target, reason, expires == null ? null : Date.from(expires), source); }
    @Override public @Nullable BanEntry<com.destroystokyo.paper.profile.PlayerProfile> addBan(@NotNull com.destroystokyo.paper.profile.PlayerProfile target, @Nullable String reason, @Nullable Duration duration, @Nullable String source) { return addBan(target, reason, duration == null ? null : Instant.now().plus(duration), source); }

    private BanEntry<com.destroystokyo.paper.profile.PlayerProfile> addBan(GameProfile profile, String reason, Date expires, String source) {
        if (profile == null || profile.getId() == null) return null;
        UserBanListEntry entry = new UserBanListEntry(profile, new Date(), blankToNull(source), expires, blankToNull(reason));
        this.list.add(entry);
        return new CraftProfileBanEntry(profile, entry, this.list);
    }

    @Override public Set getBanEntries() { return new LinkedHashSet<>(getEntries()); }
    @Override public @NotNull Set<BanEntry<com.destroystokyo.paper.profile.PlayerProfile>> getEntries() {
        Set<BanEntry<com.destroystokyo.paper.profile.PlayerProfile>> result = new LinkedHashSet<>();
        for (UserBanListEntry entry : this.list.getEntries()) result.add(new CraftProfileBanEntry(entry.getUser(), entry, this.list));
        return java.util.Collections.unmodifiableSet(result);
    }
    @Override public boolean isBanned(@NotNull String target) { return isBanned(profile(target)); }
    @Override public boolean isBanned(@NotNull PlayerProfile target) { return isBanned(toGameProfile(target)); }
    @Override public boolean isBanned(@NotNull com.destroystokyo.paper.profile.PlayerProfile target) { return isBanned(toGameProfile(target)); }
    private boolean isBanned(GameProfile profile) { return profile != null && this.list.isBanned(profile); }
    @Override public void pardon(@NotNull String target) { pardon(profile(target)); }
    @Override public void pardon(@NotNull PlayerProfile target) { pardon(toGameProfile(target)); }
    @Override public void pardon(@NotNull com.destroystokyo.paper.profile.PlayerProfile target) { pardon(toGameProfile(target)); }
    private void pardon(GameProfile profile) { if (profile != null) this.list.remove(profile); }

    static GameProfile profile(String target) {
        java.util.Objects.requireNonNull(target, "target");
        try { return profileByUuid(UUID.fromString(target)); } catch (IllegalArgumentException ignored) { return profileByName(target); }
    }
    static GameProfile profileByUuid(UUID uuid) {
        MinecraftServer server = io.lunararcdevs.lunararc.common.mod.server.LunarArcServer.minecraftServer();
        return server == null || server.getProfileCache() == null ? null : server.getProfileCache().get(uuid).orElse(null);
    }
    static GameProfile profileByName(String name) {
        MinecraftServer server = io.lunararcdevs.lunararc.common.mod.server.LunarArcServer.minecraftServer();
        return server == null || server.getProfileCache() == null ? null : server.getProfileCache().get(name).orElse(null);
    }

    static GameProfile toGameProfile(PlayerProfile target) {
        if (target == null || target.getUniqueId() == null) return null;
        GameProfile profile = new GameProfile(target.getUniqueId(), target.getName());
        if (target instanceof com.destroystokyo.paper.profile.PlayerProfile paper) copyProperties(paper, profile);
        return profile;
    }
    static GameProfile toGameProfile(com.destroystokyo.paper.profile.PlayerProfile target) {
        if (target == null || target.getId() == null) return null;
        GameProfile profile = new GameProfile(target.getId(), target.getName());
        copyProperties(target, profile);
        return profile;
    }
    private static void copyProperties(com.destroystokyo.paper.profile.PlayerProfile source, GameProfile target) {
        for (com.destroystokyo.paper.profile.ProfileProperty property : source.getProperties()) {
            target.getProperties().put(property.getName(), new Property(property.getName(), property.getValue(), property.getSignature()));
        }
    }
    static com.destroystokyo.paper.profile.PlayerProfile toBukkit(GameProfile profile) {
        io.lunararcdevs.lunararc.common.server.LunarArcPlayerProfile result = new io.lunararcdevs.lunararc.common.server.LunarArcPlayerProfile(profile.getId(), profile.getName());
        for (Property property : profile.getProperties().values()) {
            result.setProperty(new com.destroystokyo.paper.profile.ProfileProperty(property.name(), property.value(), property.signature()));
        }
        return result;
    }
    private static String blankToNull(String value) { return value == null || value.isBlank() ? null : value; }
}
