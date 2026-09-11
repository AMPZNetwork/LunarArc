package io.papermc.paper.datapack;

import io.lunararcdevs.lunararc.common.messaging.LunarArcComponentPipeline;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlags;
import org.bukkit.FeatureFlag;
import org.jetbrains.annotations.NotNull;


public final class PaperDatapack implements Datapack {
    private static final Map<PackSource, DatapackSource> PACK_SOURCES = new ConcurrentHashMap<>();
    static {
        PACK_SOURCES.put(PackSource.DEFAULT, DatapackSource.DEFAULT);
        PACK_SOURCES.put(PackSource.BUILT_IN, DatapackSource.BUILT_IN);
        PACK_SOURCES.put(PackSource.FEATURE, DatapackSource.FEATURE);
        PACK_SOURCES.put(PackSource.WORLD, DatapackSource.WORLD);
        PACK_SOURCES.put(PackSource.SERVER, DatapackSource.SERVER);
    }

    private final MinecraftServer server;
    private final PackRepository repository;
    private final Pack pack;
    private final boolean enabled;

    PaperDatapack(MinecraftServer server, PackRepository repository, Pack pack, boolean enabled) {
        this.server = server;
        this.repository = repository;
        this.pack = pack;
        this.enabled = enabled;
    }

    @Override public @NotNull String getName() { return pack.getId(); }
    @Override public @NotNull Component getTitle() { return LunarArcComponentPipeline.toAdventure(pack.getTitle()); }
    @Override public @NotNull Component getDescription() { return LunarArcComponentPipeline.toAdventure(pack.getDescription()); }
    @Override public boolean isRequired() { return pack.isRequired(); }
    @Override public @NotNull Compatibility getCompatibility() { return Compatibility.valueOf(pack.getCompatibility().name()); }
    @Override public @NotNull Set<FeatureFlag> getRequiredFeatures() { return toBukkitFeatures(pack.getRequestedFeatures()); }
    @Override public boolean isEnabled() { return enabled; }

    @Override
    public void setEnabled(boolean enabled) {
        Pack current = repository.getPack(getName());
        if (current == null) throw new IllegalStateException("Cannot toggle state of pack that doesn't exist: " + getName());
        List<Pack> selected = new ArrayList<>(repository.getSelectedPacks());
        if (enabled == selected.contains(current)) return;
        if (enabled) {
            current.getDefaultPosition().insert(selected, current, Pack::selectionConfig, false);
        } else {
            selected.remove(current);
        }
        java.util.concurrent.CompletableFuture<Void> reload = server.reloadResources(selected.stream().map(Pack::getId).toList());
        reload.thenRun(() -> {
            Runnable fire = () -> org.bukkit.Bukkit.getPluginManager().callEvent(
                    new io.papermc.paper.event.server.ServerResourcesReloadedEvent(
                            io.papermc.paper.event.server.ServerResourcesReloadedEvent.Cause.PLUGIN));
            if (server.isSameThread()) fire.run(); else server.execute(fire);
        });
    }

    @Override
    public @NotNull DatapackSource getSource() {
        PackSource source = pack.location().source();
        return PACK_SOURCES.computeIfAbsent(source, key -> new DatapackSourceImpl(key.toString()));
    }

    @Override public @NotNull Component computeDisplayName() {
        return LunarArcComponentPipeline.toAdventure(pack.getChatLink(enabled));
    }

    public static Set<FeatureFlag> toBukkitFeatures(net.minecraft.world.flag.FeatureFlagSet nativeFlags) {
        LinkedHashSet<FeatureFlag> result = new LinkedHashSet<>();
        for (ResourceLocation key : FeatureFlags.REGISTRY.toNames(nativeFlags)) {
            FeatureFlag flag = findFeatureFlag(key);
            if (flag != null) result.add(flag);
        }
        return Collections.unmodifiableSet(result);
    }

    private static FeatureFlag findFeatureFlag(ResourceLocation key) {
        String expected = key.toString();

        FeatureFlag[] known = { FeatureFlag.VANILLA, FeatureFlag.BUNDLE, FeatureFlag.TRADE_REBALANCE };
        for (FeatureFlag flag : known) {
            if (flag != null && flag.getKey().toString().equals(expected)) return flag;
        }
        return null;
    }
}
