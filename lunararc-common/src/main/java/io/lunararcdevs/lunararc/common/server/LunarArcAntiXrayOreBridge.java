package io.lunararcdevs.lunararc.common.server;

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
