package io.lunararcdevs.lunararc.common.mod.util;

import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.generator.CraftLimitedRegion;
import org.bukkit.generator.BlockPopulator;

import java.util.List;

/** Runs Bukkit block populators against the actual population-time WorldGenLevel. */
public final class LunarArcChunkPopulators {
    private LunarArcChunkPopulators() {}

    public static void populate(WorldGenLevel level, ChunkAccess chunk) {
        CraftServer server = (CraftServer) org.bukkit.Bukkit.getServer();
        net.minecraft.server.level.ServerLevel serverLevel = level instanceof net.minecraft.server.level.ServerLevel direct
                ? direct
                : ((net.minecraft.server.level.WorldGenRegion) level).getLevel();
        CraftWorld world = server.getCraftWorld(serverLevel);
        List<BlockPopulator> populators = world.getPopulators();
        if (populators.isEmpty()) return;

        CraftLimitedRegion limited = new CraftLimitedRegion(level, chunk.getPos());
        try {
            int x = chunk.getPos().x;
            int z = chunk.getPos().z;
            for (BlockPopulator populator : List.copyOf(populators)) {
                WorldgenRandom seeded = new WorldgenRandom(new LegacyRandomSource(level.getSeed()));
                long populationSeed = seeded.setDecorationSeed(level.getSeed(), x, z);
                java.util.Random random = new java.util.Random(populationSeed);
                populator.populate(world, random, x, z, limited);
            }
            limited.saveEntities();
        } finally {
            limited.breakLink();
        }
    }
}
