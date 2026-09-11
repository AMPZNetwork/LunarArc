package org.bukkit.craftbukkit;

import net.minecraft.network.chat.contents.TranslatableContents;
import org.bukkit.JukeboxSong;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class CraftJukeboxSong implements JukeboxSong, org.bukkit.craftbukkit.util.Handleable<net.minecraft.world.item.JukeboxSong> {
    private final NamespacedKey key;
    private final net.minecraft.world.item.JukeboxSong handle;

    public CraftJukeboxSong(@NotNull NamespacedKey key, @NotNull net.minecraft.world.item.JukeboxSong handle) {
        this.key = Objects.requireNonNull(key, "key");
        this.handle = Objects.requireNonNull(handle, "handle");
    }

    public @NotNull net.minecraft.world.item.JukeboxSong getHandle() { return this.handle; }
    @Override public @NotNull NamespacedKey getKey() { return this.key; }
    @Override public @NotNull String getTranslationKey() {
        if (this.handle.description().getContents() instanceof TranslatableContents translatable) return translatable.getKey();
        return net.minecraft.Util.makeDescriptionId(
                "jukebox_song",
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(key.getNamespace(), key.getKey()));
    }
    @Override public String toString() { return "CraftJukeboxSong[" + this.key + "]"; }

    // CraftBukkit's conversion pair for this registry type. Plugins call these directly to cross
    // between the Bukkit handle and the NMS object, so they carry CraftBukkit's names verbatim.
    public static JukeboxSong minecraftToBukkit(net.minecraft.world.item.JukeboxSong minecraft) {
        return org.bukkit.craftbukkit.CraftRegistry.minecraftToBukkit(minecraft, net.minecraft.core.registries.Registries.JUKEBOX_SONG, org.bukkit.Registry.JUKEBOX_SONG);
    }

    public static JukeboxSong minecraftHolderToBukkit(net.minecraft.core.Holder<net.minecraft.world.item.JukeboxSong> minecraft) {
        return CraftJukeboxSong.minecraftToBukkit(minecraft.value());
    }

    public static net.minecraft.world.item.JukeboxSong bukkitToMinecraft(JukeboxSong bukkit) {
        return org.bukkit.craftbukkit.CraftRegistry.bukkitToMinecraft(bukkit);
    }

    public static net.minecraft.core.Holder<net.minecraft.world.item.JukeboxSong> bukkitToMinecraftHolder(JukeboxSong bukkit) {
        com.google.common.base.Preconditions.checkArgument(bukkit != null);

        net.minecraft.core.Registry<net.minecraft.world.item.JukeboxSong> registry = org.bukkit.craftbukkit.CraftRegistry.getMinecraftRegistry(net.minecraft.core.registries.Registries.JUKEBOX_SONG);

        if (registry.wrapAsHolder(CraftJukeboxSong.bukkitToMinecraft(bukkit)) instanceof net.minecraft.core.Holder.Reference<net.minecraft.world.item.JukeboxSong> holder) {
            return holder;
        }

        throw new IllegalArgumentException("No Reference holder found for " + bukkit
                + ", this can happen if a plugin creates its own jukebox song without properly registering it.");
    }
}
