package io.lunararcdevs.lunararc.common.server;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class LunarArcAntiXrayEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(LunarArcAntiXrayEngine.class);
    private static final java.util.Map<ServerLevel, LunarArcAntiXrayEngine> ENGINES = new ConcurrentHashMap<>();
    private static final Set<Block> SOLID_EXEMPT = Set.of(
            Blocks.SPAWNER, Blocks.BARRIER, Blocks.SHULKER_BOX, Blocks.SLIME_BLOCK, Blocks.MANGROVE_ROOTS);

    private final boolean enabled;
    private final int maxBlockHeight;
    private final int updateRadius;
    private final boolean lavaObscures;
    private final Set<BlockState> hiddenStates;

    private LunarArcAntiXrayEngine(ServerLevel level) {
        var config = io.papermc.paper.configuration.WorldConfiguration.forLevel(level).anticheat.antiXray;
        boolean configuredEnabled = config.enabled;
        int engineMode = config.engineMode;
        if (configuredEnabled && engineMode != 1) {
            LOGGER.warn("anticheat.anti-xray.engine-mode {} is not implemented yet (only 1/HIDE is) "
                    + "- anti-xray is disabled for {}", engineMode, level.dimension().location());
            configuredEnabled = false;
        }

        this.enabled = configuredEnabled;
        this.maxBlockHeight = config.maxBlockHeight;
        this.updateRadius = config.updateRadius;
        this.lavaObscures = config.lavaObscures;

        Set<BlockState> states = new HashSet<>();
        if (this.enabled) {
            for (String id : config.hiddenBlocks) {
                ResourceLocation location = ResourceLocation.tryParse(id);
                Block block = location == null ? null : BuiltInRegistries.BLOCK.get(location);
                if (block == null || block.defaultBlockState().isAir()) continue;
                states.addAll(block.getStateDefinition().getPossibleStates());
            }
            if (states.isEmpty()) {
                LOGGER.warn("anticheat.anti-xray.hidden-blocks resolved to no real blocks - "
                        + "anti-xray is enabled but has nothing to hide for {}", level.dimension().location());
            }
        }
        this.hiddenStates = states;

        if (this.enabled) {
            LOGGER.info("Anti-xray HIDE engine active for {}: {} hidden block state(s), "
                            + "max-block-height={}, update-radius={}",
                    level.dimension().location(), states.size(), maxBlockHeight, updateRadius);
        }
    }

    public static LunarArcAntiXrayEngine forLevel(ServerLevel level) {
        return ENGINES.computeIfAbsent(level, LunarArcAntiXrayEngine::new);
    }

    /** Clears the cached engine for a level so a config edit takes effect on its next (re)load. */
    public static void invalidate(ServerLevel level) {
        ENGINES.remove(level);
    }

    public boolean isEnabled() {
        return enabled;
    }

    private boolean isHidden(BlockState state) {
        return hiddenStates.contains(state);
    }

    private boolean isSolidForReveal(BlockState state) {
        if (state.isAir()) return false;
        if (lavaObscures && state == Blocks.LAVA.defaultBlockState()) return true;
        if (SOLID_EXEMPT.contains(state.getBlock())) return false;
        return state.blocksMotion();
    }

    public LevelChunkSection[] obfuscateForSend(LevelChunk chunk) {
        return chunk.getSections();
    }

    public void onBlockChange(ServerLevel level, BlockPos pos, BlockState newState, BlockState oldState) {
        if (!enabled || oldState == null) return;
        if (!(isSolidForReveal(oldState) && !isSolidForReveal(newState))) return;
        if (pos.getY() > maxBlockHeight + updateRadius - 1) return;
        revealNear(level, pos);
    }

    public void onBlockInteractStart(ServerLevel level, BlockPos pos) {
        if (!enabled) return;
        if (pos.getY() > maxBlockHeight + updateRadius - 1) return;
        revealNear(level, pos);
    }

    private void revealNear(ServerLevel level, BlockPos pos) {
        for (BlockPos neighbor : neighborsWithinTwo(pos)) {
            // getBlockStateIfLoaded is a Paper addition, not vanilla - getChunkNow is the plain
            // vanilla way to read a chunk only if it is already loaded, without loading/generating
            // it (confirmed via CraftWorld's own getChunkNow(x, z) usage).
            net.minecraft.world.level.chunk.LevelChunk chunk =
                    level.getChunkSource().getChunkNow(neighbor.getX() >> 4, neighbor.getZ() >> 4);
            BlockState state = chunk == null ? null : chunk.getBlockState(neighbor);
            if (state != null && isHidden(state)) {
                level.getChunkSource().blockChanged(neighbor);
            }
        }
    }

    private List<BlockPos> neighborsWithinTwo(BlockPos pos) {
        if (updateRadius <= 0) return List.of();
        if (updateRadius == 1) {
            return List.of(pos.west(), pos.east(), pos.below(), pos.above(), pos.north(), pos.south());
        }
        // updateRadius >= 2: real Paper's fixed "up to two orthogonal steps" neighbour set.
        BlockPos west = pos.west();
        BlockPos east = pos.east();
        BlockPos below = pos.below();
        BlockPos above = pos.above();
        return List.of(
                west, west.west(), west.below(), west.above(), west.north(), west.south(),
                east, east.east(), east.below(), east.above(), east.north(), east.south(),
                below, below.below(), below.north(), below.south(),
                above, above.above(), above.north(), above.south(),
                pos.north(), pos.north().north(),
                pos.south(), pos.south().south());
    }
}
