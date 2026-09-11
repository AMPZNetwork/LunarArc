package io.lunararcdevs.lunararc.common.server;

import io.lunararcdevs.lunararc.common.LunarArcServerAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Concrete Bukkit registry entry factories. Dynamic/data-pack registries are
 * always read from the active loader-owned MinecraftServer; static registries
 * are read from BuiltInRegistries. No synthetic vanilla key lists are kept
 * here, so mod/datapack registry additions remain visible to plugins.
 */
final class LunarArcRegistryEntries {
    private LunarArcRegistryEntries() {}

    private static RegistryAccess activeRegistries() {
        return LunarArcServerAccess.getMinecraftServer().registryAccess();
    }

    private static NamespacedKey bukkitKey(ResourceLocation location) {
        return new NamespacedKey(location.getNamespace(), location.getPath());
    }

    // These Bukkit types are enum/class-backed in Paper 1.21.1, so arbitrary
    // loader registry entries cannot be represented as synthetic subclasses.
    // Expose the canonical Bukkit registries for exact 1.21.1 compatibility.
    static Registry<? extends Keyed> createAttributeRegistry() { return Registry.ATTRIBUTE; }
    static Registry<? extends Keyed> createFluidRegistry() { return Registry.FLUID; }
    static Registry<? extends Keyed> createSoundRegistry() { return Registry.SOUNDS; }
    static Registry<? extends Keyed> createBiomeRegistry() { return Registry.BIOME; }
    static Registry<? extends Keyed> createArtRegistry() { return Registry.ART; }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static Registry<? extends Keyed> createMemoryKeyRegistry() {
        return LunarArcBukkitRegistry.fromValues((java.util.Collection) org.bukkit.entity.memory.MemoryKey.values());
    }

    static Registry<? extends Keyed> createEnchantmentRegistry() {
        return LunarArcBukkitRegistry.lazy(() -> {
            net.minecraft.core.Registry<net.minecraft.world.item.enchantment.Enchantment> nms =
                    activeRegistries().registryOrThrow(Registries.ENCHANTMENT);
            List<Keyed> values = new ArrayList<>();
            for (Map.Entry<net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment>,
                    net.minecraft.world.item.enchantment.Enchantment> entry : nms.entrySet()) {
                values.add(new org.bukkit.craftbukkit.enchantments.CraftEnchantment(
                        bukkitKey(entry.getKey().location()), entry.getValue(), nms));
            }
            return values;
        });
    }

    static Registry<? extends Keyed> createGameEventRegistry() {
        return LunarArcBukkitRegistry.lazy(() -> {
            List<Keyed> values = new ArrayList<>();
            for (Map.Entry<net.minecraft.resources.ResourceKey<net.minecraft.world.level.gameevent.GameEvent>,
                    net.minecraft.world.level.gameevent.GameEvent> entry : BuiltInRegistries.GAME_EVENT.entrySet()) {
                values.add(new CraftGameEvent(bukkitKey(entry.getKey().location()), entry.getValue()));
            }
            return values;
        });
    }

    private static final class CraftGameEvent extends org.bukkit.GameEvent {
        private final NamespacedKey key;
        private final net.minecraft.world.level.gameevent.GameEvent handle;

        CraftGameEvent(NamespacedKey key, net.minecraft.world.level.gameevent.GameEvent handle) {
            this.key = key;
            this.handle = handle;
        }

        public net.minecraft.world.level.gameevent.GameEvent getHandle() { return this.handle; }
        @Override public NamespacedKey getKey() { return this.key; }
        @Override public int getRange() { return this.handle.notificationRadius(); }
        @Override public int getVibrationLevel() { return this.handle.notificationRadius(); }
        @Override public int hashCode() { return this.key.hashCode(); }
        @Override public boolean equals(Object other) {
            return this == other || (other instanceof org.bukkit.GameEvent event && this.key.equals(event.getKey()));
        }
        @Override public String toString() { return "CraftGameEvent[" + this.key + "]"; }
    }

    static Registry<? extends Keyed> createMusicInstrumentRegistry() {
        return LunarArcBukkitRegistry.lazy(() -> {
            net.minecraft.core.Registry<net.minecraft.world.item.Instrument> nms =
                    activeRegistries().registryOrThrow(Registries.INSTRUMENT);
            List<Keyed> values = new ArrayList<>();
            for (Map.Entry<net.minecraft.resources.ResourceKey<net.minecraft.world.item.Instrument>,
                    net.minecraft.world.item.Instrument> entry : nms.entrySet()) {
                values.add(new org.bukkit.craftbukkit.CraftMusicInstrument(
                        bukkitKey(entry.getKey().location()), entry.getValue()));
            }
            return values;
        });
    }

    static Registry<? extends Keyed> createStructureTypeRegistry() {
        return LunarArcBukkitRegistry.lazy(() -> {
            List<Keyed> values = new ArrayList<>();
            for (Map.Entry<net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.structure.StructureType<?>>,
                    net.minecraft.world.level.levelgen.structure.StructureType<?>> entry : BuiltInRegistries.STRUCTURE_TYPE.entrySet()) {
                values.add(new org.bukkit.craftbukkit.generator.structure.CraftStructureType(
                        bukkitKey(entry.getKey().location()), entry.getValue()));
            }
            return values;
        });
    }

    static Registry<? extends Keyed> createStructureRegistry() {
        return LunarArcBukkitRegistry.lazy(() -> {
            net.minecraft.core.Registry<net.minecraft.world.level.levelgen.structure.Structure> structures =
                    activeRegistries().registryOrThrow(Registries.STRUCTURE);
            List<Keyed> values = new ArrayList<>();
            for (Map.Entry<net.minecraft.resources.ResourceKey<net.minecraft.world.level.levelgen.structure.Structure>,
                    net.minecraft.world.level.levelgen.structure.Structure> entry : structures.entrySet()) {
                net.minecraft.world.level.levelgen.structure.Structure nmsStructure = entry.getValue();
                ResourceLocation typeId = BuiltInRegistries.STRUCTURE_TYPE.getKey(nmsStructure.type());
                if (typeId == null) {
                    throw new IllegalStateException("Unregistered StructureType for structure " + entry.getKey().location());
                }
                org.bukkit.generator.structure.StructureType type =
                        new org.bukkit.craftbukkit.generator.structure.CraftStructureType(
                                bukkitKey(typeId), nmsStructure.type());
                values.add(new org.bukkit.craftbukkit.generator.structure.CraftStructure(
                        bukkitKey(entry.getKey().location()), nmsStructure, type));
            }
            return values;
        });
    }

    /**
     * OldEnum-style built-in registries backed by the real NMS entry. The
     * Bukkit compatibility name/ordinal are derived metadata only; the handle
     * remains authoritative so modloader-added entries are not synthetic.
     */
    static Registry<? extends Keyed> createKeyedNmsRegistry(Class<? extends Keyed> apiType,
                                                              net.minecraft.core.Registry<?> nms) {
        // Bukkit's OldEnum-style registry interfaces (for example
        // Villager.Profession) resolve their legacy constants through the matching
        // Registry field during interface initialisation. Building our concrete
        // adapters here eagerly would initialise that interface before
        // Registry.VILLAGER_PROFESSION has been assigned, creating a circular
        // <clinit> dependency. Keep the adapter concrete, but defer entry creation
        // until the registry is first queried after the Registry field exists.
        return LunarArcBukkitRegistry.lazy(() -> {
            List<Keyed> values = new ArrayList<>();
            int ordinal = 0;
            for (Map.Entry<?, ?> raw : nms.entrySet()) {
                Object keyObject = raw.getKey();
                if (!(keyObject instanceof net.minecraft.resources.ResourceKey<?> resourceKey)) continue;
                Keyed value = keyedVariant(apiType, bukkitKey(resourceKey.location()), ordinal++, raw.getValue());
                if (value != null) values.add(value);
            }
            return values;
        });
    }

    private static Keyed keyedVariant(Class<? extends Keyed> apiType, NamespacedKey key, int ordinal, Object handle) {
        return switch (apiType.getName()) {
            case "org.bukkit.entity.Cat$Type" -> new CraftCatType(key, ordinal,
                    (net.minecraft.world.entity.animal.CatVariant) handle);
            case "org.bukkit.entity.Frog$Variant" -> new CraftFrogVariant(key, ordinal,
                    (net.minecraft.world.entity.animal.FrogVariant) handle);
            case "org.bukkit.entity.Villager$Profession" -> new CraftVillagerProfession(key, ordinal,
                    (net.minecraft.world.entity.npc.VillagerProfession) handle);
            case "org.bukkit.entity.Villager$Type" -> new CraftVillagerType(key, ordinal,
                    (net.minecraft.world.entity.npc.VillagerType) handle);
            default -> null;
        };
    }

    private static String oldEnumName(NamespacedKey key) {
        return NamespacedKey.MINECRAFT.equals(key.getNamespace())
                ? key.getKey().toUpperCase(java.util.Locale.ROOT)
                : key.toString();
    }

    private static final class CraftCatType implements org.bukkit.entity.Cat.Type {
        private final NamespacedKey key; private final int ordinal;
        private final net.minecraft.world.entity.animal.CatVariant handle;
        CraftCatType(NamespacedKey key, int ordinal, net.minecraft.world.entity.animal.CatVariant handle) {
            this.key = key; this.ordinal = ordinal; this.handle = handle;
        }
        public net.minecraft.world.entity.animal.CatVariant getHandle() { return this.handle; }
        @Override public NamespacedKey getKey() { return this.key; }
        @Override public String name() { return oldEnumName(this.key); }
        @Override public int ordinal() { return this.ordinal; }
        @Override public int compareTo(org.bukkit.entity.Cat.Type other) { return Integer.compare(this.ordinal, other.ordinal()); }
        @Override public boolean equals(Object other) { return this == other || (other instanceof org.bukkit.entity.Cat.Type type && this.key.equals(type.getKey())); }
        @Override public int hashCode() { return this.key.hashCode(); }
        @Override public String toString() { return name(); }
    }

    private static final class CraftFrogVariant implements org.bukkit.entity.Frog.Variant {
        private final NamespacedKey key; private final int ordinal;
        private final net.minecraft.world.entity.animal.FrogVariant handle;
        CraftFrogVariant(NamespacedKey key, int ordinal, net.minecraft.world.entity.animal.FrogVariant handle) {
            this.key = key; this.ordinal = ordinal; this.handle = handle;
        }
        public net.minecraft.world.entity.animal.FrogVariant getHandle() { return this.handle; }
        @Override public NamespacedKey getKey() { return this.key; }
        @Override public String name() { return oldEnumName(this.key); }
        @Override public int ordinal() { return this.ordinal; }
        @Override public int compareTo(org.bukkit.entity.Frog.Variant other) { return Integer.compare(this.ordinal, other.ordinal()); }
        @Override public boolean equals(Object other) { return this == other || (other instanceof org.bukkit.entity.Frog.Variant type && this.key.equals(type.getKey())); }
        @Override public int hashCode() { return this.key.hashCode(); }
        @Override public String toString() { return name(); }
    }

    private static final class CraftVillagerType implements org.bukkit.entity.Villager.Type {
        private final NamespacedKey key; private final int ordinal;
        private final net.minecraft.world.entity.npc.VillagerType handle;
        CraftVillagerType(NamespacedKey key, int ordinal, net.minecraft.world.entity.npc.VillagerType handle) {
            this.key = key; this.ordinal = ordinal; this.handle = handle;
        }
        public net.minecraft.world.entity.npc.VillagerType getHandle() { return this.handle; }
        @Override public NamespacedKey getKey() { return this.key; }
        @Override public String name() { return oldEnumName(this.key); }
        @Override public int ordinal() { return this.ordinal; }
        @Override public int compareTo(org.bukkit.entity.Villager.Type other) { return Integer.compare(this.ordinal, other.ordinal()); }
        @Override public boolean equals(Object other) { return this == other || (other instanceof org.bukkit.entity.Villager.Type type && this.key.equals(type.getKey())); }
        @Override public int hashCode() { return this.key.hashCode(); }
        @Override public String toString() { return name(); }
    }

    private static final class CraftVillagerProfession implements org.bukkit.entity.Villager.Profession {
        private final NamespacedKey key; private final int ordinal;
        private final net.minecraft.world.entity.npc.VillagerProfession handle;
        CraftVillagerProfession(NamespacedKey key, int ordinal, net.minecraft.world.entity.npc.VillagerProfession handle) {
            this.key = key; this.ordinal = ordinal; this.handle = handle;
        }
        public net.minecraft.world.entity.npc.VillagerProfession getHandle() { return this.handle; }
        @Override public NamespacedKey getKey() { return this.key; }
        @Override public String translationKey() { return "entity.minecraft.villager." + this.key.getKey(); }
        @Override public String name() { return oldEnumName(this.key); }
        @Override public int ordinal() { return this.ordinal; }
        @Override public int compareTo(org.bukkit.entity.Villager.Profession other) { return Integer.compare(this.ordinal, other.ordinal()); }
        @Override public boolean equals(Object other) { return this == other || (other instanceof org.bukkit.entity.Villager.Profession type && this.key.equals(type.getKey())); }
        @Override public int hashCode() { return this.key.hashCode(); }
        @Override public String toString() { return name(); }
    }

    static Registry<? extends Keyed> createWolfVariantRegistry() {
        return LunarArcBukkitRegistry.lazy(() -> {
            net.minecraft.core.Registry<net.minecraft.world.entity.animal.WolfVariant> nms =
                    activeRegistries().registryOrThrow(Registries.WOLF_VARIANT);
            List<Keyed> values = new ArrayList<>();
            for (Map.Entry<net.minecraft.resources.ResourceKey<net.minecraft.world.entity.animal.WolfVariant>,
                    net.minecraft.world.entity.animal.WolfVariant> entry : nms.entrySet()) {
                values.add(new CraftWolfVariant(bukkitKey(entry.getKey().location()), entry.getValue()));
            }
            return values;
        });
    }

    private static final class CraftWolfVariant implements org.bukkit.entity.Wolf.Variant {
        private final NamespacedKey key;
        private final net.minecraft.world.entity.animal.WolfVariant handle;
        CraftWolfVariant(NamespacedKey key, net.minecraft.world.entity.animal.WolfVariant handle) {
            this.key = key; this.handle = handle;
        }
        public net.minecraft.world.entity.animal.WolfVariant getHandle() { return this.handle; }
        @Override public NamespacedKey getKey() { return this.key; }
        @Override public boolean equals(Object other) {
            return this == other || (other instanceof org.bukkit.entity.Wolf.Variant variant && this.key.equals(variant.getKey()));
        }
        @Override public int hashCode() { return this.key.hashCode(); }
        @Override public String toString() { return "CraftWolfVariant[" + this.key + "]"; }
    }

    static Registry<? extends Keyed> createPatternTypeRegistry() {
        return LunarArcBukkitRegistry.lazy(() -> {
            net.minecraft.core.Registry<net.minecraft.world.level.block.entity.BannerPattern> nms =
                    activeRegistries().registryOrThrow(Registries.BANNER_PATTERN);
            List<Keyed> values = new ArrayList<>();
            for (Map.Entry<net.minecraft.resources.ResourceKey<net.minecraft.world.level.block.entity.BannerPattern>,
                    net.minecraft.world.level.block.entity.BannerPattern> entry : nms.entrySet()) {
                values.add(new org.bukkit.craftbukkit.block.banner.CraftPatternType(
                        bukkitKey(entry.getKey().location()), entry.getValue()));
            }
            return values;
        });
    }

    static Registry<? extends Keyed> createTrimRegistry(Class<? extends Keyed> apiType) {
        if (org.bukkit.inventory.meta.trim.TrimMaterial.class.isAssignableFrom(apiType)) {
            return LunarArcBukkitRegistry.lazy(() -> {
                net.minecraft.core.Registry<net.minecraft.world.item.armortrim.TrimMaterial> nms =
                        activeRegistries().registryOrThrow(Registries.TRIM_MATERIAL);
                List<Keyed> values = new ArrayList<>();
                for (Map.Entry<net.minecraft.resources.ResourceKey<net.minecraft.world.item.armortrim.TrimMaterial>,
                        net.minecraft.world.item.armortrim.TrimMaterial> entry : nms.entrySet()) {
                    values.add(new org.bukkit.craftbukkit.inventory.trim.CraftTrimMaterial(
                            bukkitKey(entry.getKey().location()), entry.getValue()));
                }
                return values;
            });
        }
        if (org.bukkit.inventory.meta.trim.TrimPattern.class.isAssignableFrom(apiType)) {
            return LunarArcBukkitRegistry.lazy(() -> {
                net.minecraft.core.Registry<net.minecraft.world.item.armortrim.TrimPattern> nms =
                        activeRegistries().registryOrThrow(Registries.TRIM_PATTERN);
                List<Keyed> values = new ArrayList<>();
                for (Map.Entry<net.minecraft.resources.ResourceKey<net.minecraft.world.item.armortrim.TrimPattern>,
                        net.minecraft.world.item.armortrim.TrimPattern> entry : nms.entrySet()) {
                    values.add(new org.bukkit.craftbukkit.inventory.trim.CraftTrimPattern(
                            bukkitKey(entry.getKey().location()), entry.getValue()));
                }
                return values;
            });
        }
        throw LunarArcMissingAdapterException.forSurface("trim registry " + apiType.getName());
    }

    static Registry<? extends Keyed> createJukeboxSongRegistry() {
        return LunarArcBukkitRegistry.lazy(() -> {
            net.minecraft.core.Registry<net.minecraft.world.item.JukeboxSong> nms =
                    activeRegistries().registryOrThrow(Registries.JUKEBOX_SONG);
            List<Keyed> values = new ArrayList<>();
            for (Map.Entry<net.minecraft.resources.ResourceKey<net.minecraft.world.item.JukeboxSong>,
                    net.minecraft.world.item.JukeboxSong> entry : nms.entrySet()) {
                values.add(new org.bukkit.craftbukkit.CraftJukeboxSong(
                        bukkitKey(entry.getKey().location()), entry.getValue()));
            }
            return values;
        });
    }

    static Registry<? extends Keyed> createDamageTypeRegistry() {
        return LunarArcBukkitRegistry.lazy(() -> {
            net.minecraft.core.Registry<net.minecraft.world.damagesource.DamageType> nms =
                    activeRegistries().registryOrThrow(Registries.DAMAGE_TYPE);
            List<Keyed> values = new ArrayList<>();
            for (Map.Entry<net.minecraft.resources.ResourceKey<net.minecraft.world.damagesource.DamageType>,
                    net.minecraft.world.damagesource.DamageType> entry : nms.entrySet()) {
                values.add(new org.bukkit.craftbukkit.damage.CraftDamageType(
                        bukkitKey(entry.getKey().location()), entry.getValue()));
            }
            return values;
        });
    }

    static Registry<? extends Keyed> createMapCursorTypeRegistry() {
        List<Keyed> values = new ArrayList<>();
        byte index = 0;
        for (ResourceLocation location : BuiltInRegistries.MAP_DECORATION_TYPE.keySet()) {
            values.add(new LunarArcMapCursorType(bukkitKey(location), index++));
        }
        return LunarArcBukkitRegistry.fromValues(values);
    }

    private record LunarArcMapCursorType(NamespacedKey key, byte value) implements org.bukkit.map.MapCursor.Type {
        @Override public NamespacedKey getKey() { return key; }
        @Override public byte getValue() { return value; }
        @Override public String name() { return key.getKey().toUpperCase(java.util.Locale.ROOT); }
        @Override public int ordinal() { return Byte.toUnsignedInt(value); }
        @Override public int compareTo(org.bukkit.map.MapCursor.Type other) { return Integer.compare(this.ordinal(), other.ordinal()); }
        @Override public String toString() { return name(); }
    }
}
