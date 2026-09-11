package org.bukkit.craftbukkit.packs;

import io.papermc.paper.datapack.PaperDatapack;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import org.bukkit.FeatureFlag;
import org.bukkit.NamespacedKey;
import org.bukkit.packs.DataPack;
import org.jetbrains.annotations.NotNull;


@SuppressWarnings("removal")
public final class CraftDataPack implements DataPack {
    private final Pack pack;
    private final boolean enabled;

    CraftDataPack(Pack pack, boolean enabled) {
        this.pack = pack;
        this.enabled = enabled;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        String id = pack.getId();
        NamespacedKey parsed = NamespacedKey.fromString(id);
        if (parsed != null) return parsed;
        String safe = id.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9/._-]", "_");
        return new NamespacedKey(NamespacedKey.MINECRAFT, safe);
    }

    @Override public @NotNull String getTitle() { return pack.getTitle().getString(); }
    @Override public @NotNull String getDescription() { return pack.getDescription().getString(); }

    @Override
    public int getPackFormat() {
        return SharedConstants.getCurrentVersion().getPackVersion(net.minecraft.server.packs.PackType.SERVER_DATA);
    }

    @Override public int getMinSupportedPackFormat() { return getPackFormat(); }
    @Override public int getMaxSupportedPackFormat() { return getPackFormat(); }
    @Override public boolean isEnabled() { return enabled; }
    @Override public boolean isRequired() { return pack.isRequired(); }
    @Override public @NotNull Compatibility getCompatibility() { return Compatibility.valueOf(pack.getCompatibility().name()); }
    @Override public @NotNull Set<FeatureFlag> getRequestedFeatures() { return PaperDatapack.toBukkitFeatures(pack.getRequestedFeatures()); }

    @Override
    public @NotNull Source getSource() {
        PackSource source = pack.location().source();
        if (source == PackSource.DEFAULT) return Source.DEFAULT;
        if (source == PackSource.BUILT_IN) return Source.BUILT_IN;
        if (source == PackSource.FEATURE) return Source.FEATURE;
        if (source == PackSource.WORLD) return Source.WORLD;
        return Source.SERVER;
    }

    public net.minecraft.server.packs.repository.Pack getHandle() {
        return this.pack;
    }

    public String getRawId() {
        return this.getHandle().getId();
    }
}
