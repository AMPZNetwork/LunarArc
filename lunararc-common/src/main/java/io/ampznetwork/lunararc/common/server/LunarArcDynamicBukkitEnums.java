package io.ampznetwork.lunararc.common.server;

import io.ampznetwork.lunararc.api.EnumHelper;
import io.ampznetwork.lunararc.api.Unsafe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Extends legacy enum-backed Bukkit surfaces from the active loader registries.
 * Modern Paper registry adapters remain backed directly by NMS registries.
 */
public final class LunarArcDynamicBukkitEnums {
    private static final Map<Material, NamespacedKey> MATERIAL_KEYS =
            Collections.synchronizedMap(new IdentityHashMap<>());
    private static final Map<ResourceLocation, Material> MATERIALS_BY_ID = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<ResourceLocation, Material> MATERIALS_VIEW = Collections.unmodifiableMap(MATERIALS_BY_ID);
    private static final Map<ResourceLocation, EntityType> ENTITY_TYPES = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<ResourceLocation, World.Environment> ENVIRONMENTS = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<World.Environment, net.minecraft.resources.ResourceKey<net.minecraft.world.level.dimension.LevelStem>> LEVEL_STEMS =
            Collections.synchronizedMap(new IdentityHashMap<>());
    private static boolean registered;

    private LunarArcDynamicBukkitEnums() {}

    public static synchronized void registerAll(MinecraftServer server) {
        if (registered) return;
        try {
            registerMaterials();
            registerEntities();
            registerKeyedNoArg(Biome.class, server.registryAccess().registryOrThrow(Registries.BIOME).keySet(), "key");
            registerKeyedNoArg(org.bukkit.Fluid.class, BuiltInRegistries.FLUID.keySet(), "key");
            registerParticles();
            registerSounds();
            registerPotionTypes();
            registerEnvironments(server);
            reloadBukkitRegistries();
            registered = true;
        } catch (Throwable throwable) {
            throw new IllegalStateException("Unable to expose loader-owned registries to Bukkit enum surfaces", throwable);
        }
    }

    public static NamespacedKey materialKey(Material material) {
        return MATERIAL_KEYS.get(material);
    }

    /**
     * Forward lookup for CraftMagicNumbers.getMaterial(Block/Item): unlike
     * Material.matchMaterial(), this also finds materials registered dynamically here for
     * modded blocks/items that have no vanilla Bukkit Material entry.
     */
    public static Material material(ResourceLocation id) {
        if (id == null) return null;
        Material dynamic = MATERIALS_BY_ID.get(id);
        if (dynamic != null) return dynamic;
        return materialByKey(key(id));
    }

    /**
     * Every block/item id this server knows a Material for, vanilla and modded alike - a
     * consumer that only wants the modded ones filters on {@code !"minecraft".equals(id.getNamespace())}.
     * Read-only: this map is {@link #registerMaterials() built once} and callers have no business
     * mutating LunarArc's own view of it.
     */
    public static Map<ResourceLocation, Material> materialsById() {
        return MATERIALS_VIEW;
    }

    public static EntityType entityType(ResourceLocation id) {
        EntityType dynamic = ENTITY_TYPES.get(id);
        if (dynamic != null) return dynamic;
        try {
            return org.bukkit.Registry.ENTITY_TYPE.get(new NamespacedKey(id.getNamespace(), id.getPath()));
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** Returns the exact Bukkit environment associated with a loader-owned dimension key. */
    public static World.Environment environment(ResourceLocation dimension) {
        if (dimension == null) return World.Environment.NORMAL;
        World.Environment environment = ENVIRONMENTS.get(dimension);
        if (environment != null) return environment;
        if (net.minecraft.world.level.Level.NETHER.location().equals(dimension)) return World.Environment.NETHER;
        if (net.minecraft.world.level.Level.END.location().equals(dimension)) return World.Environment.THE_END;
        if (net.minecraft.world.level.Level.OVERWORLD.location().equals(dimension)) return World.Environment.NORMAL;
        return World.Environment.CUSTOM;
    }

    /** Resolves a Bukkit environment back to the loader-owned level stem that created it. */
    public static net.minecraft.resources.ResourceKey<net.minecraft.world.level.dimension.LevelStem> levelStem(World.Environment environment) {
        if (environment == null) return null;
        net.minecraft.resources.ResourceKey<net.minecraft.world.level.dimension.LevelStem> key = LEVEL_STEMS.get(environment);
        if (key != null) return key;
        if (environment == World.Environment.NORMAL) return net.minecraft.world.level.dimension.LevelStem.OVERWORLD;
        if (environment == World.Environment.NETHER) return net.minecraft.world.level.dimension.LevelStem.NETHER;
        if (environment == World.Environment.THE_END) return net.minecraft.world.level.dimension.LevelStem.END;
        return null;
    }

    private static void registerMaterials() throws Throwable {
        Set<ResourceLocation> ids = new LinkedHashSet<>();
        ids.addAll(BuiltInRegistries.BLOCK.keySet());
        ids.addAll(BuiltInRegistries.ITEM.keySet());
        List<Material> additions = new ArrayList<>();
        int ordinal = Material.values().length;
        for (ResourceLocation id : ids) {
            NamespacedKey key = key(id);
            Material existing = materialByKey(key);
            if (existing != null) {
                MATERIAL_KEYS.put(existing, key);
                MATERIALS_BY_ID.put(id, existing);
                continue;
            }
            String enumName = uniqueName(Material.class, id, additions.stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()));
            Material material = construct(
                    Material.class, enumName, ordinal++, new Class<?>[]{int.class}, new Object[]{-1});
            setObject(material, "key", key);
            setBoolean(material, "isModBlock", BuiltInRegistries.BLOCK.containsKey(id));
            setBoolean(material, "isModItem", BuiltInRegistries.ITEM.containsKey(id));
            MATERIAL_KEYS.put(material, key);
            MATERIALS_BY_ID.put(id, material);
            additions.add(material);
            putStaticMap(Material.class, "BY_NAME", enumName, material);
            putStaticMap(Material.class, "BY_KEY", key.toString(), material);
        }
        append(Material.class, additions);
    }

    private static void registerEntities() throws Throwable {
        List<EntityType> additions = new ArrayList<>();
        int ordinal = EntityType.values().length;
        for (ResourceLocation id : BuiltInRegistries.ENTITY_TYPE.keySet()) {
            EntityType existing = registryValue(org.bukkit.Registry.ENTITY_TYPE, id);
            if (existing != null && existing != EntityType.UNKNOWN) {
                ENTITY_TYPES.put(id, existing);
                continue;
            }
            String enumName = uniqueName(EntityType.class, id, additions.stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()));
            EntityType type = construct(
                    EntityType.class, enumName, ordinal++,
                    new Class<?>[]{String.class, Class.class, int.class, boolean.class},
                    new Object[]{id.getPath(), org.bukkit.entity.Entity.class, -1, true});
            setObject(type, "key", key(id));
            additions.add(type);
            ENTITY_TYPES.put(id, type);
            putStaticMap(EntityType.class, "NAME_MAP", id.getPath().toLowerCase(Locale.ROOT), type);
        }
        append(EntityType.class, additions);
    }

    private static <E extends Enum<E> & Keyed> void registerKeyedNoArg(
            Class<E> enumType, Iterable<ResourceLocation> ids, String keyField) throws Throwable {
        List<E> additions = new ArrayList<>();
        int ordinal = enumType.getEnumConstants().length;
        for (ResourceLocation id : ids) {
            if (findByKey(enumType, id) != null) continue;
            String enumName = uniqueName(enumType, id, additions.stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()));
            E value = construct(enumType, enumName, ordinal++, new Class<?>[0], new Object[0]);
            setObject(value, keyField, key(id));
            additions.add(value);
        }
        append(enumType, additions);
    }

    private static void registerParticles() throws Throwable {
        List<Particle> additions = new ArrayList<>();
        int ordinal = Particle.values().length;
        for (ResourceLocation id : BuiltInRegistries.PARTICLE_TYPE.keySet()) {
            if (findByKey(Particle.class, id) != null) continue;
            String enumName = uniqueName(Particle.class, id, additions.stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()));
            Particle particle = construct(
                    Particle.class, enumName, ordinal++,
                    new Class<?>[]{String.class, Class.class, boolean.class},
                    new Object[]{id.getPath(), Void.class, true});
            setObject(particle, "key", key(id));
            additions.add(particle);
        }
        append(Particle.class, additions);
    }

    private static void registerSounds() throws Throwable {
        List<Sound> additions = new ArrayList<>();
        int ordinal = Sound.values().length;
        for (ResourceLocation id : BuiltInRegistries.SOUND_EVENT.keySet()) {
            if (findByKey(Sound.class, id) != null) continue;
            String enumName = uniqueName(Sound.class, id, additions.stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()));
            Sound sound = construct(
                    Sound.class, enumName, ordinal++, new Class<?>[]{String.class}, new Object[]{id.getPath()});
            setObject(sound, "key", key(id));
            additions.add(sound);
        }
        append(Sound.class, additions);
    }

    private static void registerPotionTypes() throws Throwable {
        List<org.bukkit.potion.PotionType> additions = new ArrayList<>();
        int ordinal = org.bukkit.potion.PotionType.values().length;
        for (ResourceLocation id : BuiltInRegistries.POTION.keySet()) {
            if (findByKey(org.bukkit.potion.PotionType.class, id) != null) continue;
            String enumName = uniqueName(org.bukkit.potion.PotionType.class, id,
                    additions.stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()));
            org.bukkit.potion.PotionType potion = construct(
                    org.bukkit.potion.PotionType.class, enumName, ordinal++,
                    new Class<?>[]{String.class}, new Object[]{id.getPath()});
            setObject(potion, "key", key(id));
            additions.add(potion);
        }
        append(org.bukkit.potion.PotionType.class, additions);
    }

    private static void registerEnvironments(MinecraftServer server) throws Throwable {
        ENVIRONMENTS.put(net.minecraft.world.level.Level.OVERWORLD.location(), World.Environment.NORMAL);
        ENVIRONMENTS.put(net.minecraft.world.level.Level.NETHER.location(), World.Environment.NETHER);
        ENVIRONMENTS.put(net.minecraft.world.level.Level.END.location(), World.Environment.THE_END);
        LEVEL_STEMS.put(World.Environment.NORMAL, net.minecraft.world.level.dimension.LevelStem.OVERWORLD);
        LEVEL_STEMS.put(World.Environment.NETHER, net.minecraft.world.level.dimension.LevelStem.NETHER);
        LEVEL_STEMS.put(World.Environment.THE_END, net.minecraft.world.level.dimension.LevelStem.END);

        net.minecraft.core.Registry<net.minecraft.world.level.dimension.LevelStem> stems =
                server.registryAccess().registryOrThrow(Registries.LEVEL_STEM);
        List<World.Environment> additions = new ArrayList<>();
        int ordinal = World.Environment.values().length;
        int legacyId = ordinal - 1;
        for (ResourceLocation id : stems.keySet()) {
            if (ENVIRONMENTS.containsKey(id)) continue;
            String enumName = uniqueName(World.Environment.class, id,
                    additions.stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()));
            World.Environment environment = construct(
                    World.Environment.class, enumName, ordinal++,
                    new Class<?>[]{int.class}, new Object[]{legacyId++});
            additions.add(environment);
            ENVIRONMENTS.put(id, environment);
            LEVEL_STEMS.put(environment, net.minecraft.resources.ResourceKey.create(Registries.LEVEL_STEM, id));
            putStaticMap(World.Environment.class, "lookup", environment.getId(), environment);
        }
        append(World.Environment.class, additions);
    }

    private static Material materialByKey(NamespacedKey key) {
        for (Material material : Material.values()) {
            try {
                if (key.equals(material.getKey())) return material;
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private static <E extends Enum<E> & Keyed> E findByKey(Class<E> type, ResourceLocation id) {
        NamespacedKey key = key(id);
        for (E value : type.getEnumConstants()) {
            try {
                if (key.equals(value.getKey())) return value;
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private static <T extends Keyed> T registryValue(org.bukkit.Registry<T> registry, ResourceLocation id) {
        try {
            return registry.get(key(id));
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void reloadBukkitRegistries() {
        for (Field field : org.bukkit.Registry.class.getFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;
            try {
                Object value = field.get(null);
                if (value instanceof org.bukkit.Registry<?> registry) {
                    try {
                        registry.getClass().getMethod("reload").invoke(registry);
                    } catch (NoSuchMethodException ignored) {
                        // Older exact 1.21.1 API builds may not expose reload publicly.
                    }
                }
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Unable to reload Bukkit registry " + field.getName(), exception);
            }
        }
    }

    private static NamespacedKey key(ResourceLocation id) {
        return new NamespacedKey(id.getNamespace(), id.getPath());
    }

    private static String uniqueName(Class<? extends Enum<?>> type, ResourceLocation id, Set<String> pendingNames) {
        String base = ("minecraft".equals(id.getNamespace()) ? id.getPath() : id.getNamespace() + "_" + id.getPath())
                .toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9_]", "_");
        if (base.isEmpty() || Character.isDigit(base.charAt(0))) base = "MOD_" + base;
        String candidate = base;
        int salt = 0;
        while (enumNameExists(type, candidate) || pendingNames.contains(candidate)) {
            candidate = base + "_" + Integer.toHexString(id.toString().hashCode() + ++salt).toUpperCase(Locale.ROOT);
        }
        return candidate;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static boolean enumNameExists(Class<? extends Enum<?>> type, String name) {
        try {
            Enum.valueOf((Class) type, name);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    // --- Real io.ampznetwork.lunararc.api.EnumHelper/Unsafe adapters ---
    // Replaces the previous ad-hoc LunarArcEnumExtender (now removed): these thin wrappers keep
    // every call site above unchanged in shape while routing construction/append through the
    // real, ported EnumHelper (which — unlike the old extender — calls
    // Unsafe.ensureClassInitialized before constructing, and resets all three enum caches
    // enumConstantDirectory/enumConstants/enumVars, not just two).

    private static <E extends Enum<E>> E construct(
            Class<E> enumType, String name, int ordinal, Class<?>[] paramTypes, Object[] params) {
        return EnumHelper.makeEnum(enumType, name, ordinal, java.util.Arrays.asList(paramTypes), java.util.Arrays.asList(params));
    }

    private static <E extends Enum<E>> void append(Class<E> enumType, List<E> additions) {
        if (additions.isEmpty()) return;
        EnumHelper.addEnums(enumType, additions);
    }

    private static void setObject(Object target, String fieldName, Object value) {
        Field field = findField(target.getClass(), fieldName);
        if (field == null) return;
        Unsafe.putObject(target, Unsafe.objectFieldOffset(field), value);
    }

    private static void setBoolean(Object target, String fieldName, boolean value) {
        Field field = findField(target.getClass(), fieldName);
        if (field == null || field.getType() != boolean.class) return;
        Unsafe.putBoolean(target, Unsafe.objectFieldOffset(field), value);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void putStaticMap(Class<?> owner, String fieldName, Object key, Object value) {
        try {
            Field field = owner.getDeclaredField(fieldName);
            Object base = Unsafe.staticFieldBase(field);
            Object map = Unsafe.getObject(base, Unsafe.staticFieldOffset(field));
            if (map instanceof java.util.Map target) target.put(key, value);
        } catch (NoSuchFieldException ignored) {
            // Paper/Bukkit revisions may not expose every legacy lookup map.
        }
    }

    private static Field findField(Class<?> type, String name) {
        for (Class<?> cursor = type; cursor != null; cursor = cursor.getSuperclass()) {
            try {
                return cursor.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {}
        }
        return null;
    }
}
