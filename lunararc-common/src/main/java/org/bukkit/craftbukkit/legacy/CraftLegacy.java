package org.bukkit.craftbukkit.legacy;

import com.mojang.serialization.Dynamic;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.util.datafix.fixes.ItemIdFix;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Deprecated
public final class CraftLegacy {
    private static final Map<MaterialData, BlockState> MATERIAL_TO_STATE = new HashMap<>(4096);
    private static final Map<BlockState, MaterialData> STATE_TO_MATERIAL = new HashMap<>(4096);
    private static final Map<MaterialData, Block> MATERIAL_TO_BLOCK = new HashMap<>(4096);
    private static final Map<Block, MaterialData> BLOCK_TO_MATERIAL = new HashMap<>(1024);
    private static final Map<MaterialData, Item> MATERIAL_TO_ITEM = new HashMap<>(16384);
    private static final Map<Item, MaterialData> ITEM_TO_MATERIAL = new HashMap<>(1024);

    private CraftLegacy() {
    }

    public static Material toLegacy(Material material) {
        if (material == null || material.isLegacy()) return material;
        return toLegacyData(material).getItemType();
    }

    public static MaterialData toLegacyData(Material material) {
        return toLegacyData(material, false);
    }

    public static MaterialData toLegacyData(Material material, boolean itemPriority) {
        Objects.requireNonNull(material, "material");
        if (material.isLegacy()) throw new IllegalArgumentException("toLegacy on legacy Material: " + material);

        MaterialData mapped = null;
        if (itemPriority) {
            Item item = itemFor(material);
            if (item != null) mapped = ITEM_TO_MATERIAL.get(item);
        }
        if (mapped == null && material.isBlock()) {
            Block block = blockFor(material);
            if (block != null) {
                mapped = STATE_TO_MATERIAL.get(block.defaultBlockState());
                if (mapped == null) mapped = BLOCK_TO_MATERIAL.get(block);
                if (mapped == null) mapped = ITEM_TO_MATERIAL.get(block.asItem());
            }
        }
        if (mapped == null && !itemPriority) {
            Item item = itemFor(material);
            if (item != null) mapped = ITEM_TO_MATERIAL.get(item);
        }
        return mapped != null ? mapped : new MaterialData(Material.LEGACY_AIR);
    }

    public static BlockState fromLegacyData(Material material, byte data) {
        Objects.requireNonNull(material, "material");
        if (!material.isLegacy()) throw new IllegalArgumentException("fromLegacyData on modern Material: " + material);
        MaterialData key = new MaterialData(material, data);
        BlockState state = MATERIAL_TO_STATE.get(key);
        if (state != null) return state;
        Block block = MATERIAL_TO_BLOCK.get(key);
        return block != null ? block.defaultBlockState() : Blocks.AIR.defaultBlockState();
    }


    public static Item fromLegacyData(Material material, short data) {
        Objects.requireNonNull(material, "material");
        if (!material.isLegacy()) throw new IllegalArgumentException("fromLegacyData on modern Material: " + material);
        MaterialData key = new MaterialData(material, (byte) data);
        Item item = MATERIAL_TO_ITEM.get(key);
        if (item != null) return item;
        BlockState state = MATERIAL_TO_STATE.get(key);
        if (state != null) return state.getBlock().asItem();
        Block block = MATERIAL_TO_BLOCK.get(key);
        return block != null ? block.asItem() : Items.AIR;
    }

    public static MaterialData toLegacy(BlockState state) {
        Objects.requireNonNull(state, "state");
        MaterialData mapped = STATE_TO_MATERIAL.get(state);
        if (mapped == null) mapped = BLOCK_TO_MATERIAL.get(state.getBlock());
        return mapped != null ? mapped : new MaterialData(Material.LEGACY_AIR);
    }

    public static byte toLegacyData(BlockState state) {
        return toLegacy(state).getData();
    }

    public static Material toLegacyMaterial(BlockState state) {
        return toLegacy(state).getItemType();
    }

    public static Material fromLegacy(Material material) {
        if (material == null || !material.isLegacy()) return material;
        return fromLegacy(new MaterialData(material), false);
    }

    public static Material fromLegacy(MaterialData materialData) {
        return fromLegacy(materialData, false);
    }

    public static Material fromLegacy(MaterialData materialData, boolean itemPriority) {
        Objects.requireNonNull(materialData, "materialData");
        Material material = materialData.getItemType();
        if (material == null || !material.isLegacy()) return material;

        Material mapped = null;
        if (itemPriority) {
            Item item = MATERIAL_TO_ITEM.get(materialData);
            if (item != null) mapped = materialFor(item);
        }
        if (mapped == null) {
            BlockState state = MATERIAL_TO_STATE.get(materialData);
            if (state != null) mapped = materialFor(state.getBlock());
            if (mapped == null) {
                Block block = MATERIAL_TO_BLOCK.get(materialData);
                if (block != null) mapped = materialFor(block);
            }
        }
        if (!itemPriority && mapped == null) {
            Item item = MATERIAL_TO_ITEM.get(materialData);
            if (item != null) mapped = materialFor(item);
        }
        return mapped != null ? mapped : Material.AIR;
    }


    public static Material[] values() {
        Material[] values = Material.values();
        return java.util.Arrays.copyOfRange(values, Material.LEGACY_AIR.ordinal(), values.length);
    }

    public static Material valueOf(String name) {
        Objects.requireNonNull(name, "name");
        return Material.valueOf(name.startsWith("LEGACY_") ? name : "LEGACY_" + name);
    }

    public static Material getMaterial(String name) {
        Objects.requireNonNull(name, "name");
        return Material.getMaterial(name.startsWith("LEGACY_") ? name : "LEGACY_" + name);
    }

    public static Material matchMaterial(String name) {
        Objects.requireNonNull(name, "name");
        return Material.matchMaterial(name.startsWith("LEGACY_") ? name : "LEGACY_" + name);
    }

    public static int ordinal(Material material) {
        Objects.requireNonNull(material, "material");
        if (!material.isLegacy()) throw new IllegalArgumentException("ordinal on modern Material: " + material);
        return material.ordinal() - Material.LEGACY_AIR.ordinal();
    }

    public static String name(Material material) {
        Objects.requireNonNull(material, "material");
        if (!material.isLegacy()) throw new IllegalArgumentException("name on modern Material: " + material);
        return material.name().substring("LEGACY_".length());
    }

    public static String toString(Material material) {
        return name(material);
    }

    public static void init() {
    }

    private static Block blockFor(Material material) {
        ResourceLocation key = ResourceLocation.withDefaultNamespace(material.name().toLowerCase(java.util.Locale.ROOT));
        return BuiltInRegistries.BLOCK.containsKey(key) ? BuiltInRegistries.BLOCK.get(key) : null;
    }

    private static Item itemFor(Material material) {
        ResourceLocation key = ResourceLocation.withDefaultNamespace(material.name().toLowerCase(java.util.Locale.ROOT));
        return BuiltInRegistries.ITEM.containsKey(key) ? BuiltInRegistries.ITEM.get(key) : null;
    }

    private static Material materialFor(Block block) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        return key == null ? null : Material.matchMaterial(key.toString());
    }

    private static Material materialFor(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        return key == null ? null : Material.matchMaterial(key.toString());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static BlockState applyProperties(BlockState state, CompoundTag properties) {
        StateDefinition<?, ?> definition = state.getBlock().getStateDefinition();
        for (String name : properties.getAllKeys()) {
            Property property = definition.getProperty(name);
            if (property == null) continue;
            String raw = properties.getString(name);
            Optional<?> value = property.getValue(raw);
            if (value.isPresent()) state = state.setValue(property, (Comparable) value.get());
        }
        return state;
    }

    private static int dataVersion() {
        return net.minecraft.SharedConstants.getCurrentVersion().getDataVersion().getVersion();
    }

    static {
        for (Material material : Material.values()) {
            if (!material.isLegacy()) continue;

            if (material.getId() >= 0 && material.getId() < 256) {
                for (byte data = 0; data < 16; data++) {
                    MaterialData legacy = new MaterialData(material, data);
                    var dynamic = BlockStateData.getTag(material.getId() << 4 | data);
                    var fixed = DataFixers.getDataFixer().update(References.BLOCK_STATE, dynamic, 100, dataVersion());
                    if (!(fixed.getValue() instanceof CompoundTag tag)) continue;

                    String name = tag.getString("Name");
                    if (name.isEmpty() || name.contains("%%FILTER_ME%%")) continue;
                    ResourceLocation key;
                    try {
                        key = ResourceLocation.parse(name);
                    } catch (RuntimeException ex) {
                        continue;
                    }
                    if (!BuiltInRegistries.BLOCK.containsKey(key)) continue;
                    Block block = BuiltInRegistries.BLOCK.get(key);
                    if (block == Blocks.AIR) continue;

                    BlockState state = block.defaultBlockState();
                    if (tag.contains("Properties")) {
                        state = applyProperties(state, tag.getCompound("Properties"));
                    }
                    MATERIAL_TO_STATE.put(legacy, state);
                    STATE_TO_MATERIAL.putIfAbsent(state, legacy);
                    MATERIAL_TO_BLOCK.put(legacy, block);
                    BLOCK_TO_MATERIAL.putIfAbsent(block, legacy);
                }
            }

            int maxData = material == Material.LEGACY_MONSTER_EGG ? 121 : 16;
            String legacyName = material.name();
            if (legacyName.endsWith("_SWORD") || legacyName.endsWith("_SPADE") || legacyName.endsWith("_PICKAXE")
                    || legacyName.endsWith("_AXE") || legacyName.endsWith("_HOE") || legacyName.endsWith("_HELMET")
                    || legacyName.endsWith("_CHESTPLATE") || legacyName.endsWith("_LEGGINGS") || legacyName.endsWith("_BOOTS")
                    || material == Material.LEGACY_FISHING_ROD || material == Material.LEGACY_CARROT_STICK
                    || material == Material.LEGACY_BOW || material == Material.LEGACY_SHEARS
                    || material == Material.LEGACY_FLINT_AND_STEEL || material == Material.LEGACY_SHIELD
                    || material == Material.LEGACY_ELYTRA) {
                maxData = 1;
            }

            if (ItemIdFix.getItem(material.getId()) == null) continue;
            for (byte data = 0; data < maxData; data++) {
                MaterialData legacy = new MaterialData(material, data);
                CompoundTag stack = new CompoundTag();
                stack.putInt("id", material.getId());
                stack.putShort("Damage", data);
                Dynamic<Tag> converted;
                try {
                    converted = DataFixers.getDataFixer().update(
                            References.ITEM_STACK, new Dynamic<>(NbtOps.INSTANCE, stack), -1, dataVersion());
                } catch (Throwable ignored) {
                    continue;
                }
                if (!(converted.getValue() instanceof CompoundTag fixedStack)) continue;
                String itemId = fixedStack.getString("id");
                if ("minecraft:spawn_egg".equals(itemId)) itemId = "minecraft:pig_spawn_egg";
                if (itemId.isEmpty()) continue;
                ResourceLocation key;
                try {
                    key = ResourceLocation.parse(itemId);
                } catch (RuntimeException ex) {
                    continue;
                }
                if (!BuiltInRegistries.ITEM.containsKey(key)) continue;
                Item item = BuiltInRegistries.ITEM.get(key);
                if (item == Items.AIR) continue;
                MATERIAL_TO_ITEM.put(legacy, item);
                ITEM_TO_MATERIAL.putIfAbsent(item, legacy);
            }
        }

        for (org.bukkit.entity.EntityType entityType : org.bukkit.entity.EntityType.values()) {
            short typeId = entityType.getTypeId();
            if (typeId < 0 || typeId > 255) continue;
            Material spawnEgg = Material.matchMaterial(entityType.name() + "_SPAWN_EGG");
            if (spawnEgg == null) continue;
            Item item = itemFor(spawnEgg);
            if (item == null || item == Items.AIR) continue;
            MaterialData legacy = new MaterialData(Material.LEGACY_MONSTER_EGG, (byte) typeId);
            MATERIAL_TO_ITEM.put(legacy, item);
            ITEM_TO_MATERIAL.put(item, legacy);
        }
    }
}
