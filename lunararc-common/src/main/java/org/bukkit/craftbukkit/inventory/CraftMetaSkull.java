package org.bukkit.craftbukkit.inventory;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Concrete player-head metadata over Minecraft 1.21.1 PROFILE / NOTE_BLOCK_SOUND components. */
public final class CraftMetaSkull extends CraftItemMeta implements SkullMeta {
    private static final int MAX_OWNER_LENGTH = 16;
    private ResolvableProfile profile;
    private ResourceLocation noteBlockSound;

    public CraftMetaSkull() { super(); }
    public CraftMetaSkull(ItemStack nms) {
        super(nms);
        if (nms == null || nms.isEmpty()) return;
        this.profile = nms.get(DataComponents.PROFILE);
        this.noteBlockSound = nms.get(DataComponents.NOTE_BLOCK_SOUND);
    }

    @Override public void applyToNms(ItemStack nms) {
        super.applyToNms(nms);
        if (this.profile == null) nms.remove(DataComponents.PROFILE); else nms.set(DataComponents.PROFILE, this.profile);
        if (this.noteBlockSound == null) nms.remove(DataComponents.NOTE_BLOCK_SOUND); else nms.set(DataComponents.NOTE_BLOCK_SOUND, this.noteBlockSound);
    }

    @Override public boolean hasOwner() { return this.profile != null; }
    @Override public @Nullable String getOwner() { return this.profile == null ? null : this.profile.name().orElse(null); }
    @Override public boolean setOwner(@Nullable String owner) {
        if (owner != null && owner.length() > MAX_OWNER_LENGTH) return false;
        if (owner == null) { this.profile = null; return true; }
        net.minecraft.server.MinecraftServer server = io.lunararcdevs.lunararc.common.mod.server.LunarArcServer.minecraftServer();
        net.minecraft.server.level.ServerPlayer online = server == null ? null : server.getPlayerList().getPlayerByName(owner);
        this.profile = online != null
                ? new ResolvableProfile(online.getGameProfile())
                : new ResolvableProfile(Optional.of(owner), Optional.empty(), new PropertyMap());
        return true;
    }

    @Override public @Nullable OfflinePlayer getOwningPlayer() {
        if (this.profile == null) return null;
        Optional<UUID> id = this.profile.id();
        if (id.isPresent() && !Util.NIL_UUID.equals(id.get())) return Bukkit.getOfflinePlayer(id.get());
        Optional<String> name = this.profile.name();
        return name.filter(s -> !s.isEmpty()).map(Bukkit::getOfflinePlayer).orElse(null);
    }

    @Override public boolean setOwningPlayer(@Nullable OfflinePlayer owner) {
        if (owner == null) { this.profile = null; return true; }
        net.minecraft.server.MinecraftServer server = io.lunararcdevs.lunararc.common.mod.server.LunarArcServer.minecraftServer();
        net.minecraft.server.level.ServerPlayer online = server == null ? null : server.getPlayerList().getPlayer(owner.getUniqueId());
        if (online != null) this.profile = new ResolvableProfile(online.getGameProfile());
        else this.profile = new ResolvableProfile(Optional.ofNullable(owner.getName()), Optional.of(owner.getUniqueId()), new PropertyMap());
        return true;
    }

    @Override public void setPlayerProfile(@Nullable com.destroystokyo.paper.profile.PlayerProfile profile) {
        this.profile = profile == null ? null : fromPaperProfile(profile);
    }
    @Override public @Nullable com.destroystokyo.paper.profile.PlayerProfile getPlayerProfile() {
        return this.profile == null ? null : toPaperProfile(this.profile);
    }
    @Override public @Nullable PlayerProfile getOwnerProfile() { return (PlayerProfile) getPlayerProfile(); }
    @Override public void setOwnerProfile(@Nullable PlayerProfile profile) {
        if (profile == null) { this.profile = null; return; }
        if (profile instanceof com.destroystokyo.paper.profile.PlayerProfile paper) {
            this.profile = fromPaperProfile(paper);
            return;
        }
        UUID id = profile.getUniqueId();
        String name = profile.getName();
        if (id == null && (name == null || name.isBlank())) throw new IllegalArgumentException("PlayerProfile must contain a UUID or name");
        this.profile = new ResolvableProfile(Optional.ofNullable(name), Optional.ofNullable(id), new PropertyMap());
    }

    private static ResolvableProfile fromPaperProfile(com.destroystokyo.paper.profile.PlayerProfile source) {
        UUID id = source.getId();
        String name = source.getName();
        if (id == null && (name == null || name.isBlank())) throw new IllegalArgumentException("PlayerProfile must contain a UUID or name");
        PropertyMap properties = new PropertyMap();
        for (com.destroystokyo.paper.profile.ProfileProperty p : source.getProperties()) {
            properties.put(p.getName(), new Property(p.getName(), p.getValue(), p.getSignature()));
        }
        return new ResolvableProfile(Optional.ofNullable(name), Optional.ofNullable(id), properties);
    }

    private static com.destroystokyo.paper.profile.PlayerProfile toPaperProfile(ResolvableProfile source) {
        io.lunararcdevs.lunararc.common.server.LunarArcPlayerProfile result =
                new io.lunararcdevs.lunararc.common.server.LunarArcPlayerProfile(source.id().orElse(null), source.name().orElse(null));
        for (Property p : source.properties().values()) {
            result.setProperty(new com.destroystokyo.paper.profile.ProfileProperty(p.name(), p.value(), p.signature()));
        }
        return result;
    }

    @Override public void setNoteBlockSound(@Nullable NamespacedKey sound) {
        this.noteBlockSound = sound == null ? null : ResourceLocation.fromNamespaceAndPath(sound.getNamespace(), sound.getKey());
    }
    @Override public @Nullable NamespacedKey getNoteBlockSound() {
        return this.noteBlockSound == null ? null : new NamespacedKey(this.noteBlockSound.getNamespace(), this.noteBlockSound.getPath());
    }
    @Override public @NotNull CraftMetaSkull clone() { return (CraftMetaSkull) super.clone(); }
}
