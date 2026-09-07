package io.ampznetwork.lunararc.common.server;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.CraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Keeps {@code config/paper-world-defaults.yml}'s anti-xray {@code hidden-blocks} list current
 * with modded ores as mods are added to or removed from the server.
 *
 * <p>{@code CraftServer#loadConfigurations()} seeds that list with real Paper's own vanilla-only
 * defaults (verified against {@code patches/server/0005-Paper-config-files.patch}) before this
 * runs. This is a second, separate pass over the file it already wrote:</p>
 * <ul>
 *   <li><b>Adds</b> any block tagged {@code c:ores} - NeoForge's common convention tag every
 *       ore-adjacent mod block is expected to carry - whose namespace is not {@code minecraft} and
 *       whose id is not already in the list.</li>
 *   <li><b>Removes</b> any entry whose id no longer names a registered block at all - i.e. the mod
 *       that added it is no longer installed. A vanilla entry can never hit this: vanilla blocks are
 *       never unregistered, so the seeded vanilla list is untouched by construction, not by a
 *       namespace check. A modded entry that still resolves to a real block just no longer tagged
 *       {@code c:ores} is left alone too - that could be a server owner's own deliberate addition of
 *       a block their tag-based detection would never have picked in the first place, and removing
 *       it out from under them would be exactly the kind of "your own edits vanish on reboot"
 *       surprise a config-mutating feature must not spring.</li>
 * </ul>
 * <p>Nothing is written back when nothing actually changed, so a server whose ore mods are
 * unchanged since last boot does not churn this file every time.</p>
 *
 * <p>{@link net.minecraft.core.DefaultedRegistry}'s {@code get(ResourceLocation)} never returns
 * null for an unmapped key - it silently returns the registry's default ({@code Blocks.AIR}) - so
 * "does this id still exist" is checked with {@code containsKey}, not a null check on {@code get}.</p>
 *
 * <p>This must run after tags are bound (after world load, alongside
 * {@link LunarArcEssentialsItemBridge}) - {@code c:ores} membership does not resolve yet at
 * {@code CraftServer} construction time, when {@code loadConfigurations()} itself runs.</p>
 */
public final class LunarArcAntiXrayOreBridge {

    private static final Logger LOGGER = LoggerFactory.getLogger(LunarArcAntiXrayOreBridge.class);
    private static final String HIDDEN_BLOCKS_PATH = "anticheat.anti-xray.hidden-blocks";
    private static final TagKey<Block> COMMON_ORES =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "ores"));

    private LunarArcAntiXrayOreBridge() {}

    public static void mergeModdedOres(CraftServer craftServer) {
        File file = new File("config", "paper-world-defaults.yml");
        if (!file.isFile()) return;

        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (Exception ex) {
            LOGGER.warn("Unable to read {} to merge modded ores into anti-xray's hidden-blocks list", file.getPath(), ex);
            return;
        }

        List<String> current = config.getStringList(HIDDEN_BLOCKS_PATH);
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        int removed = 0;
        for (String id : current) {
            ResourceLocation location = ResourceLocation.tryParse(id);
            if (location == null || !BuiltInRegistries.BLOCK.containsKey(location)) {
                removed++;
                continue;
            }
            merged.add(id);
        }

        int beforeAdd = merged.size();
        for (ResourceLocation id : BuiltInRegistries.BLOCK.keySet()) {
            if (id.getNamespace().equals("minecraft")) continue; // vanilla ores are already seeded
            Block block = BuiltInRegistries.BLOCK.get(id);
            if (block == null || !block.builtInRegistryHolder().is(COMMON_ORES)) continue;
            merged.add(id.toString());
        }
        int added = merged.size() - beforeAdd;

        if (added == 0 && removed == 0) return;

        config.set(HIDDEN_BLOCKS_PATH, new ArrayList<>(merged));
        try {
            config.save(file);
            craftServer.reloadPaperWorldConfigurations();
            LOGGER.info("Anti-xray hidden-blocks: added {} modded ore block(s), removed {} orphaned entr{} in {}",
                    added, removed, removed == 1 ? "y" : "ies", file.getPath());
        } catch (Exception ex) {
            LOGGER.warn("Unable to save {} after merging modded ores", file.getPath(), ex);
        }
    }
}
