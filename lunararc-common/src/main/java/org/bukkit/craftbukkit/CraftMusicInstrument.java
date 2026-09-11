package org.bukkit.craftbukkit;

import net.minecraft.world.item.Instrument;
import org.bukkit.MusicInstrument;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class CraftMusicInstrument extends MusicInstrument implements org.bukkit.craftbukkit.util.Handleable<Instrument> {
    private final NamespacedKey key;
    private final Instrument handle;

    public CraftMusicInstrument(@NotNull NamespacedKey key, @NotNull Instrument handle) {
        this.key = Objects.requireNonNull(key, "key");
        this.handle = Objects.requireNonNull(handle, "handle");
    }

    public @NotNull Instrument getHandle() { return this.handle; }
    @Override public @NotNull NamespacedKey getKey() { return this.key; }

    @Override public boolean equals(Object other) {
        return this == other || (other instanceof MusicInstrument instrument && this.key.equals(instrument.getKey()));
    }
    @Override public int hashCode() { return this.key.hashCode(); }
    @Override public String toString() { return "CraftMusicInstrument[" + this.key + "]"; }

    // CraftBukkit's conversion pair for this registry type. Plugins call these directly to cross
    // between the Bukkit handle and the NMS object, so they carry CraftBukkit's names verbatim.
    public static MusicInstrument minecraftToBukkit(Instrument minecraft) {
        return org.bukkit.craftbukkit.CraftRegistry.minecraftToBukkit(minecraft, net.minecraft.core.registries.Registries.INSTRUMENT, org.bukkit.Registry.INSTRUMENT);
    }

    public static MusicInstrument minecraftHolderToBukkit(net.minecraft.core.Holder<Instrument> minecraft) {
        return CraftMusicInstrument.minecraftToBukkit(minecraft.value());
    }

    public static Instrument bukkitToMinecraft(MusicInstrument bukkit) {
        return org.bukkit.craftbukkit.CraftRegistry.bukkitToMinecraft(bukkit);
    }

    public static net.minecraft.core.Holder<Instrument> bukkitToMinecraftHolder(MusicInstrument bukkit) {
        com.google.common.base.Preconditions.checkArgument(bukkit != null);

        net.minecraft.core.Registry<Instrument> registry = org.bukkit.craftbukkit.CraftRegistry.getMinecraftRegistry(net.minecraft.core.registries.Registries.INSTRUMENT);

        if (registry.wrapAsHolder(CraftMusicInstrument.bukkitToMinecraft(bukkit)) instanceof net.minecraft.core.Holder.Reference<Instrument> holder) {
            return holder;
        }

        throw new IllegalArgumentException("No Reference holder found for " + bukkit
                + ", this can happen if a plugin creates its own instrument without properly registering it.");
    }

    public static String bukkitToString(MusicInstrument bukkit) {
        com.google.common.base.Preconditions.checkArgument(bukkit != null);

        return bukkit.getKey().toString();
    }

    public static MusicInstrument stringToBukkit(String string) {
        com.google.common.base.Preconditions.checkArgument(string != null);

        return org.bukkit.Registry.INSTRUMENT.get(NamespacedKey.fromString(string));
    }
}
