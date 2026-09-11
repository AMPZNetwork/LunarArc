package org.bukkit.craftbukkit.packs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.flag.FeatureElement;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.BlockType;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemType;
import org.bukkit.packs.DataPack;
import org.bukkit.packs.DataPackManager;
import org.bukkit.craftbukkit.CraftWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@SuppressWarnings("removal")
public final class CraftDataPackManager implements DataPackManager {
    private final PackRepository repository;

    public CraftDataPackManager(MinecraftServer server) {
        this.repository = Objects.requireNonNull(server, "server").getPackRepository();
    }

    @Override
    public @NotNull Collection<DataPack> getDataPacks() {
        Collection<Pack> selected = repository.getSelectedPacks();
        List<DataPack> result = new ArrayList<>();
        for (Pack pack : repository.getAvailablePacks()) result.add(new CraftDataPack(pack, selected.contains(pack)));
        return Collections.unmodifiableList(result);
    }

    @Override
    public @Nullable DataPack getDataPack(@NotNull NamespacedKey dataPackKey) {
        Objects.requireNonNull(dataPackKey, "dataPackKey");
        Pack pack = repository.getPack(dataPackKey.toString());
        if (pack == null && NamespacedKey.MINECRAFT.equals(dataPackKey.getNamespace())) {
            pack = repository.getPack(dataPackKey.getKey());
        }
        return pack == null ? null : new CraftDataPack(pack, repository.getSelectedPacks().contains(pack));
    }

    @Override
    public @NotNull Collection<DataPack> getEnabledDataPacks(@NotNull World world) {
        Objects.requireNonNull(world, "world");
        List<DataPack> result = new ArrayList<>();
        for (Pack pack : repository.getSelectedPacks()) result.add(new CraftDataPack(pack, true));
        return Collections.unmodifiableList(result);
    }

    @Override
    public @NotNull Collection<DataPack> getDisabledDataPacks(@NotNull World world) {
        Objects.requireNonNull(world, "world");
        Collection<Pack> selected = repository.getSelectedPacks();
        List<DataPack> result = new ArrayList<>();
        for (Pack pack : repository.getAvailablePacks()) if (!selected.contains(pack)) result.add(new CraftDataPack(pack, false));
        return Collections.unmodifiableList(result);
    }

    @Override
    public boolean isEnabledByFeature(@NotNull Material material, @NotNull World world) {
        Objects.requireNonNull(material, "material");
        if (material.isItem()) {
            var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(material.getKey().toString()));
            return isEnabled(item, world);
        }
        if (material.isBlock()) {
            var block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(material.getKey().toString()));
            return isEnabled(block, world);
        }
        throw new IllegalArgumentException("Material is neither an item nor a block: " + material);
    }

    @Override
    public boolean isEnabledByFeature(@NotNull ItemType itemType, @NotNull World world) {
        var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemType.getKey().toString()));
        return isEnabled(item, world);
    }

    @Override
    public boolean isEnabledByFeature(@NotNull BlockType blockType, @NotNull World world) {
        var block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockType.getKey().toString()));
        return isEnabled(block, world);
    }

    @Override
    public boolean isEnabledByFeature(@NotNull EntityType entityType, @NotNull World world) {
        var type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entityType.getKey().toString()));
        return isEnabled(type, world);
    }

    private static boolean isEnabled(Object object, World world) {
        Objects.requireNonNull(world, "world");
        if (!(world instanceof CraftWorld craftWorld)) throw new IllegalArgumentException("World is not a CraftWorld");
        if (!(object instanceof FeatureElement element)) return true;
        return element.isEnabled(craftWorld.getHandle().enabledFeatures());
    }

    public net.minecraft.server.packs.repository.PackRepository getHandle() {
        return this.repository;
    }
}
