package io.lunararcdevs.lunararc.common.compat.worldedit;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * WorldEdit's PaperweightAdapter (com.sk89q.worldedit.bukkit.adapter.impl.v1_21) builds a
 * throwaway ServerLevel for //regen through a constructor overload with 3 trailing Bukkit-only
 * params (World.Environment, ChunkGenerator, BiomeProvider) that Paper adds by literally
 * recompiling ServerLevel from patched source. That overload doesn't exist on this platform, and
 * Mixin can't add it either - Mixin can only inject into a constructor a class already has, never
 * add a brand-new overload (confirmed: the attempt fails at plain javac compile time, not just at
 * runtime). A plain subclass has no such restriction, since it isn't a Mixin merge target - it can
 * declare any constructor it likes as long as it ends by delegating to a real constructor its
 * parent actually has. LunarArcIntegratedPatcher retargets WorldEdit's compiled NEW/INVOKESPECIAL
 * pair from ServerLevel to this class at plugin-load time, keeping the exact same 15-argument
 * call site so none of WorldEdit's own argument-computing bytecode needs touching.
 *
 * The 3 extra params only matter when the *live* world being regenerated already has a
 * plugin-supplied custom Bukkit ChunkGenerator/BiomeProvider - see CraftServer.createWorld's
 * CustomChunkGenerator/CustomWorldChunkManager wrapping for what would need reproducing here.
 * Unsupported for now, so this throws rather than silently ignoring a generator override a
 * plugin actually asked for.
 */
public final class LunarArcWorldEditRegenLevel extends ServerLevel {

    public LunarArcWorldEditRegenLevel(
            MinecraftServer server,
            Executor dispatcher,
            LevelStorageSource.LevelStorageAccess access,
            PrimaryLevelData levelData,
            ResourceKey<Level> dimension,
            LevelStem levelStem,
            ChunkProgressListener progressListener,
            boolean isDebug,
            long seed,
            List<CustomSpawner> customSpawners,
            boolean tickTime,
            RandomSequences randomSequences,
            org.bukkit.World.Environment env,
            org.bukkit.generator.ChunkGenerator gen,
            org.bukkit.generator.BiomeProvider biomeProvider) {
        super(server, dispatcher, access, levelData, dimension, levelStem, progressListener,
                isDebug, seed, customSpawners, tickTime, randomSequences);
        if (gen != null || biomeProvider != null) {
            throw new UnsupportedOperationException(
                    "LunarArc cannot regenerate a region whose world uses a custom Bukkit "
                            + "ChunkGenerator/BiomeProvider plugin yet - only plain/vanilla-mapped "
                            + "world generation is supported for //regen right now.");
        }
    }
}
