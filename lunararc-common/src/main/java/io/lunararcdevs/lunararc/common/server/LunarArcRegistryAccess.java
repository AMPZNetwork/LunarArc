package io.lunararcdevs.lunararc.common.server;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LunarArcRegistryAccess implements RegistryAccess {
    public static final RegistryAccess INSTANCE = new LunarArcRegistryAccess();
    private final Map<Class<?>, Registry<?>> registries = new ConcurrentHashMap<>();

    public LunarArcRegistryAccess() {}

    public static RegistryAccess provider() {
        return INSTANCE;
    }

    @Override
    public <T extends Keyed> @NotNull Registry<T> getRegistry(@NotNull RegistryKey<T> key) {
        Class<T> type = resolveType(key);
        if (type == null) {
            throw LunarArcMissingAdapterException.forSurface("registry " + key);
        }
        return getRegistry(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Keyed> @NotNull Registry<T> getRegistry(@NotNull Class<T> type) {
        Registry<?> existing = registries.get(type);
        if (existing != null) return (Registry<T>) existing;

        Registry<T> created;
        try {
            created = createRegistry(type);
        } catch (LunarArcMissingAdapterException missing) {
            throw missing;
        } catch (Throwable throwable) {
            throw new IllegalStateException("Unable to create concrete Bukkit registry adapter for " + type.getName(), throwable);
        }
        if (created == null) {
            throw LunarArcMissingAdapterException.forSurface("registry " + type.getName());
        }
        Registry<?> raced = registries.putIfAbsent(type, created);
        return raced == null ? created : (Registry<T>) raced;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Keyed> Registry<T> createRegistry(Class<T> type) {
        if ("org.bukkit.damage.DamageType".equals(type.getName()) && type.isInterface()) {
            return (Registry<T>) LunarArcRegistryEntries.createDamageTypeRegistry();
        }
        if ("org.bukkit.potion.PotionEffectType".equals(type.getName())) {
            Registry<? extends Keyed> effects = createPotionEffectTypeRegistry(type);
            if (effects != null) return (Registry<T>) effects;
        }


        if ("org.bukkit.inventory.ItemType".equals(type.getName()) && type.isInterface()) {
            return (Registry<T>) createMinecraftTypeRegistry((Class<? extends Keyed>) type, true);
        }
        if ("org.bukkit.block.BlockType".equals(type.getName()) && type.isInterface()) {
            return (Registry<T>) createMinecraftTypeRegistry((Class<? extends Keyed>) type, false);
        }


        if ("org.bukkit.inventory.MenuType".equals(type.getName()) && type.isInterface()) {
            return (Registry<T>) createMenuTypeRegistry((Class<? extends Keyed>) type);
        }


        switch (type.getName()) {
            case "org.bukkit.enchantments.Enchantment":
                return (Registry<T>) LunarArcRegistryEntries.createEnchantmentRegistry();
            case "org.bukkit.attribute.Attribute":
                return (Registry<T>) LunarArcRegistryEntries.createAttributeRegistry();
            case "org.bukkit.Fluid":
                return (Registry<T>) LunarArcRegistryEntries.createFluidRegistry();
            case "org.bukkit.Sound":
                return (Registry<T>) LunarArcRegistryEntries.createSoundRegistry();
            case "org.bukkit.block.Biome":
                return (Registry<T>) LunarArcRegistryEntries.createBiomeRegistry();
            case "org.bukkit.Art":
                return (Registry<T>) LunarArcRegistryEntries.createArtRegistry();
            case "org.bukkit.entity.memory.MemoryKey":
                return (Registry<T>) LunarArcRegistryEntries.createMemoryKeyRegistry();
            case "org.bukkit.GameEvent":
                return (Registry<T>) LunarArcRegistryEntries.createGameEventRegistry();
            case "org.bukkit.MusicInstrument":
                return (Registry<T>) LunarArcRegistryEntries.createMusicInstrumentRegistry();
            case "org.bukkit.generator.structure.Structure":
                return (Registry<T>) LunarArcRegistryEntries.createStructureRegistry();
            case "org.bukkit.generator.structure.StructureType":
                return (Registry<T>) LunarArcRegistryEntries.createStructureTypeRegistry();
            case "org.bukkit.block.banner.PatternType":
                return type.isInterface()
                        ? (Registry<T>) LunarArcRegistryEntries.createPatternTypeRegistry()
                        : LunarArcBukkitRegistry.forType(type);
            case "org.bukkit.inventory.meta.trim.TrimMaterial":
                return type.isInterface()
                        ? (Registry<T>) LunarArcRegistryEntries.createTrimRegistry((Class<? extends Keyed>) type)
                        : LunarArcBukkitRegistry.forType(type);
            case "org.bukkit.inventory.meta.trim.TrimPattern":
                return type.isInterface()
                        ? (Registry<T>) LunarArcRegistryEntries.createTrimRegistry((Class<? extends Keyed>) type)
                        : LunarArcBukkitRegistry.forType(type);
            case "org.bukkit.JukeboxSong":
                return type.isInterface()
                        ? (Registry<T>) LunarArcRegistryEntries.createJukeboxSongRegistry()
                        : LunarArcBukkitRegistry.forType(type);
            case "org.bukkit.map.MapCursor$Type":
                return type.isInterface()
                        ? (Registry<T>) LunarArcRegistryEntries.createMapCursorTypeRegistry()
                        : LunarArcBukkitRegistry.forType(type);
            case "org.bukkit.entity.Cat$Type":
                return type.isInterface()
                        ? (Registry<T>) LunarArcRegistryEntries.createKeyedNmsRegistry(
                                (Class<? extends Keyed>) type, net.minecraft.core.registries.BuiltInRegistries.CAT_VARIANT)
                        : LunarArcBukkitRegistry.forType(type);
            case "org.bukkit.entity.Frog$Variant":
                return type.isInterface()
                        ? (Registry<T>) LunarArcRegistryEntries.createKeyedNmsRegistry(
                                (Class<? extends Keyed>) type, net.minecraft.core.registries.BuiltInRegistries.FROG_VARIANT)
                        : LunarArcBukkitRegistry.forType(type);
            case "org.bukkit.entity.Villager$Profession":
                return type.isInterface()
                        ? (Registry<T>) LunarArcRegistryEntries.createKeyedNmsRegistry(
                                (Class<? extends Keyed>) type, net.minecraft.core.registries.BuiltInRegistries.VILLAGER_PROFESSION)
                        : LunarArcBukkitRegistry.forType(type);
            case "org.bukkit.entity.Villager$Type":
                return type.isInterface()
                        ? (Registry<T>) LunarArcRegistryEntries.createKeyedNmsRegistry(
                                (Class<? extends Keyed>) type, net.minecraft.core.registries.BuiltInRegistries.VILLAGER_TYPE)
                        : LunarArcBukkitRegistry.forType(type);
            case "org.bukkit.entity.Wolf$Variant":
                return type.isInterface()
                        ? (Registry<T>) LunarArcRegistryEntries.createWolfVariantRegistry()
                        : LunarArcBukkitRegistry.forType(type);
            default:
                return LunarArcBukkitRegistry.forType(type);
        }
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Registry<? extends Keyed> createMenuTypeRegistry(Class<? extends Keyed> type) {
        net.minecraft.core.Registry<net.minecraft.world.inventory.MenuType<?>> nms =
                net.minecraft.core.registries.BuiltInRegistries.MENU;
        return LunarArcBukkitRegistry.lazy(() -> {
            java.util.List<Keyed> values = new java.util.ArrayList<>();
            for (java.util.Map.Entry<net.minecraft.resources.ResourceKey<net.minecraft.world.inventory.MenuType<?>>, net.minecraft.world.inventory.MenuType<?>> entry : nms.entrySet()) {
                net.minecraft.resources.ResourceLocation location = entry.getKey().location();
                org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey(location.getNamespace(), location.getPath());
                values.add(new org.bukkit.craftbukkit.inventory.CraftMenuType<>(key, entry.getValue(), viewClassFor(location.getPath())));
            }
            return values;
        });
    }

    private static Class<? extends org.bukkit.inventory.InventoryView> viewClassFor(String path) {
        return switch (path) {
            case "anvil" -> org.bukkit.inventory.view.AnvilView.class;
            case "beacon" -> org.bukkit.inventory.view.BeaconView.class;
            case "blast_furnace", "furnace", "smoker" -> org.bukkit.inventory.view.FurnaceView.class;
            case "brewing_stand" -> org.bukkit.inventory.view.BrewingStandView.class;
            case "crafter_3x3" -> org.bukkit.inventory.view.CrafterView.class;
            case "enchantment" -> org.bukkit.inventory.view.EnchantmentView.class;
            case "lectern" -> org.bukkit.inventory.view.LecternView.class;
            case "loom" -> org.bukkit.inventory.view.LoomView.class;
            case "merchant" -> org.bukkit.inventory.view.MerchantView.class;
            case "stonecutter" -> org.bukkit.inventory.view.StonecutterView.class;
            default -> org.bukkit.inventory.InventoryView.class;
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Registry<? extends Keyed> createMinecraftTypeRegistry(Class<? extends Keyed> type, boolean itemRegistry) {
        return LunarArcBukkitRegistry.lazy(() -> {
            java.util.List<Keyed> values = new java.util.ArrayList<>();
            if (itemRegistry) {
                for (java.util.Map.Entry<net.minecraft.resources.ResourceKey<net.minecraft.world.item.Item>, net.minecraft.world.item.Item> entry
                        : net.minecraft.core.registries.BuiltInRegistries.ITEM.entrySet()) {
                    net.minecraft.resources.ResourceLocation id = entry.getKey().location();
                    values.add(new org.bukkit.craftbukkit.inventory.CraftItemType<>(
                            new org.bukkit.NamespacedKey(id.getNamespace(), id.getPath()), entry.getValue()));
                }
            } else {
                for (java.util.Map.Entry<net.minecraft.resources.ResourceKey<net.minecraft.world.level.block.Block>, net.minecraft.world.level.block.Block> entry
                        : net.minecraft.core.registries.BuiltInRegistries.BLOCK.entrySet()) {
                    net.minecraft.resources.ResourceLocation id = entry.getKey().location();
                    values.add(new org.bukkit.craftbukkit.block.CraftBlockType<>(
                            new org.bukkit.NamespacedKey(id.getNamespace(), id.getPath()), entry.getValue()));
                }
            }
            return values;
        });
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Registry<? extends Keyed> createPotionEffectTypeRegistry(Class<?> apiType) {
        return LunarArcBukkitRegistry.lazy(() -> {
            java.util.List<Keyed> values = new java.util.ArrayList<>();
            var nmsRegistry = net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT;
            for (var location : nmsRegistry.keySet()) {
                net.minecraft.world.effect.MobEffect effect = nmsRegistry.get(location);
                if (effect == null) continue;
                values.add(new org.bukkit.craftbukkit.potion.CraftPotionEffectType(
                        new org.bukkit.NamespacedKey(location.getNamespace(), location.getPath()), effect));
            }
            return values;
        });
    }

    @SuppressWarnings("unchecked")
    private static <T extends Keyed> Class<T> resolveType(RegistryKey<T> key) {
        // Paper 1.21.1 registry keys only. Keep this explicit so registry lookup does
        // not depend on runtime generic/reflection discovery.
        if (key == RegistryKey.GAME_EVENT) return (Class<T>) org.bukkit.GameEvent.class;
        if (key == RegistryKey.STRUCTURE_TYPE) return (Class<T>) org.bukkit.generator.structure.StructureType.class;
        if (key == RegistryKey.MOB_EFFECT) return (Class<T>) org.bukkit.potion.PotionEffectType.class;
        if (key == RegistryKey.BLOCK) return (Class<T>) org.bukkit.block.BlockType.class;
        if (key == RegistryKey.ITEM) return (Class<T>) org.bukkit.inventory.ItemType.class;
        if (key == RegistryKey.CAT_VARIANT) return (Class<T>) org.bukkit.entity.Cat.Type.class;
        if (key == RegistryKey.FROG_VARIANT) return (Class<T>) org.bukkit.entity.Frog.Variant.class;
        if (key == RegistryKey.VILLAGER_PROFESSION) return (Class<T>) org.bukkit.entity.Villager.Profession.class;
        if (key == RegistryKey.VILLAGER_TYPE) return (Class<T>) org.bukkit.entity.Villager.Type.class;
        if (key == RegistryKey.MAP_DECORATION_TYPE) return (Class<T>) org.bukkit.map.MapCursor.Type.class;
        if (key == RegistryKey.MENU) return (Class<T>) org.bukkit.inventory.MenuType.class;
        if (key == RegistryKey.ATTRIBUTE) return (Class<T>) org.bukkit.attribute.Attribute.class;
        if (key == RegistryKey.FLUID) return (Class<T>) org.bukkit.Fluid.class;
        if (key == RegistryKey.SOUND_EVENT) return (Class<T>) org.bukkit.Sound.class;
        if (key == RegistryKey.BIOME) return (Class<T>) org.bukkit.block.Biome.class;
        if (key == RegistryKey.STRUCTURE) return (Class<T>) org.bukkit.generator.structure.Structure.class;
        if (key == RegistryKey.TRIM_MATERIAL) return (Class<T>) org.bukkit.inventory.meta.trim.TrimMaterial.class;
        if (key == RegistryKey.TRIM_PATTERN) return (Class<T>) org.bukkit.inventory.meta.trim.TrimPattern.class;
        if (key == RegistryKey.DAMAGE_TYPE) return (Class<T>) org.bukkit.damage.DamageType.class;
        if (key == RegistryKey.WOLF_VARIANT) return (Class<T>) org.bukkit.entity.Wolf.Variant.class;
        if (key == RegistryKey.ENCHANTMENT) return (Class<T>) org.bukkit.enchantments.Enchantment.class;
        if (key == RegistryKey.JUKEBOX_SONG) return (Class<T>) org.bukkit.JukeboxSong.class;
        if (key == RegistryKey.BANNER_PATTERN) return (Class<T>) org.bukkit.block.banner.PatternType.class;
        if (key == RegistryKey.PAINTING_VARIANT) return (Class<T>) org.bukkit.Art.class;
        if (key == RegistryKey.INSTRUMENT) return (Class<T>) org.bukkit.MusicInstrument.class;
        if (key == RegistryKey.ENTITY_TYPE) return (Class<T>) org.bukkit.entity.EntityType.class;
        if (key == RegistryKey.PARTICLE_TYPE) return (Class<T>) org.bukkit.Particle.class;
        if (key == RegistryKey.POTION) return (Class<T>) org.bukkit.potion.PotionType.class;
        if (key == RegistryKey.MEMORY_MODULE_TYPE) return (Class<T>) org.bukkit.entity.memory.MemoryKey.class;
        return null;
    }

}
