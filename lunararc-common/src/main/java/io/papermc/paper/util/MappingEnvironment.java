package io.papermc.paper.util;

import net.minecraft.world.entity.MobCategory;

import java.io.InputStream;
import java.util.Objects;

public final class MappingEnvironment {

    /** Kill switch Paper honours for its plugin remapper; mirrored so the same flag works here. */
    public static final boolean DISABLE_PLUGIN_REMAPPING = Boolean.getBoolean("paper.disablePluginRemapping");

    /**
     * The versioned CraftBukkit package Spigot-era plugins were compiled against on 1.21.1.
     * Commodore builds {@code org/bukkit/craftbukkit/v1_21_R1/} from this to strip it.
     */
    public static final String LEGACY_CB_VERSION = "v1_21_R1";

    private static final boolean REOBF = checkReobf();
    // A real boot proved this needs the same one-time treatment as REOBF: Commodore calls
    // hasMappings() from getOriginalOrRewrite(), invoked once per ASM visitFrame() while remapping
    // a legacy-mapped plugin's classes - potentially thousands of times per plugin. Each
    // uncached call re-ran ClassLoader.getResourceAsStream() across the whole module classpath
    // (cpw.mods.securejarhandler's ModuleClassLoader.findResourceList scans every jar's contents),
    // which on a ~150-mod-plus-plugins classpath cost real, measured minutes of wall-clock time in
    // a single boot - a genuine tick-watchdog-adjacent hang, not a slow-but-bounded operation.
    // Whether this resource exists cannot change during a run, so compute it exactly once.
    private static final boolean HAS_MAPPINGS = probeMappings();

    private MappingEnvironment() {
    }

    /** {@code true} only if the running Minecraft is reobfuscated (Spigot-mapped). */
    public static boolean reobf() {
        return REOBF;
    }

    /** {@code true} if a Paper-style bundled reobf mapping file is on the classpath. */
    public static boolean hasMappings() {
        return HAS_MAPPINGS;
    }

    public static InputStream mappingsStream() {
        return Objects.requireNonNull(mappingsStreamIfPresent(), "Missing mappings!");
    }

    public static InputStream mappingsStreamIfPresent() {
        return MappingEnvironment.class.getClassLoader().getResourceAsStream("META-INF/mappings/reobf.tiny");
    }

    private static boolean probeMappings() {
        try (InputStream probe = mappingsStreamIfPresent()) {
            return probe != null;
        } catch (java.io.IOException closeFailed) {
            return true;
        }
    }

    private static boolean checkReobf() {
        return isReobfuscatedName(MobCategory.class.getSimpleName());
    }

    // Paper's own probe assumed a Mojang-mapped runtime always keeps MobCategory's readable name -
    // true for NeoForge/Forge, but a real Fabric boot proved Fabric's own intermediary mappings
    // report it as "class_1311" instead (Fabric's official intermediary layer keeps only a subset
    // of well-known top-level classes human-readable; MobCategory isn't one of them). The one name
    // this check actually needs to catch is "EnumCreatureType", the legacy Spigot/CraftBukkit-era
    // reobfuscated name - anything else, readable or intermediary, means a Mojang-structured
    // runtime, which is every loader LunarArc supports. Split out from checkReobf() so the decision
    // itself is testable without needing a second mapping environment to actually boot under.
    public static boolean isReobfuscatedName(String mobCategorySimpleName) {
        return mobCategorySimpleName.equals("EnumCreatureType");
    }
}
