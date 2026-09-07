package io.ampznetwork.lunararc.common.server;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A from-scratch, mixin-compatible engine for real Paper's anti-xray "HIDE" mode - verified against
 * Paper's own {@code patches/server/0992-Anti-Xray.patch} for the mechanism, config keys, and the
 * exact reveal-trigger algorithm, but architected around what LunarArc can actually intercept
 * rather than ported verbatim.
 *
 * <p>Real Paper threads a per-send {@code ChunkPacketInfo} context through source-patched
 * {@code PalettedContainer}/{@code LevelChunkSection}/{@code ChunkSerializer} constructors so
 * replacement blocks are baked into a chunk's palette at load time and swapped by raw bit index at
 * send time - a micro-optimisation only possible because Paper patches those classes' source
 * directly. LunarArc mixes into an unpatched vanilla/NeoForge runtime instead, so this rebuilds only
 * the observable behaviour: a fresh, temporary, obfuscated copy of a chunk's below-height-limit
 * sections is built on demand, immediately before that chunk's packet is serialised for sending (see
 * the mixin on {@code ClientboundLevelChunkPacketData}), and discarded right after - the persisted
 * chunk and its real block states are never touched. This costs a 4096-block copy per obfuscated
 * section instead of a bit swap, but only runs once per chunk a player newly sees, not per tick, and
 * cannot corrupt the world's actual data the way patching the live palette in place could if gotten
 * wrong.</p>
 *
 * <p>Only engine-mode 1 (HIDE) is implemented - the mode {@code CraftServer}'s generated default
 * already selects. Modes 2/3 (OBFUSCATE / OBFUSCATE_LAYER), which pick a per-position decoy from the
 * hidden-blocks list itself rather than one fixed replacement, are not; selecting either in config
 * disables anti-xray with a log message rather than silently behaving like HIDE.</p>
 *
 * <p>The reveal trigger and its neighbour set are real Paper's own algorithm
 * ({@code ChunkPacketBlockControllerAntiXray#onBlockChange}/{@code #updateNearbyBlocks}/
 * {@code #updateBlock}): a solid block turning non-solid - mining exposes a cavity - rechecks a fixed
 * set of positions up to two orthogonal steps away, and any of them still holding a hidden block is
 * marked dirty so the chunk system resends its real state to every player already tracking that
 * chunk. No custom packet-sending code is needed for the reveal itself - {@code blockChanged(pos)} is
 * the same vanilla primitive an ordinary block update already uses.</p>
 */
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

    /**
     * DISABLED pending a redesign. Always returns the
     * chunk's own real sections unmodified; nothing is hidden right now.
     *
     * <p>The obfuscated copy this used to build was sized however a fresh
     * {@code PalettedContainer} populated via 4096 {@code set(x,y,z,value)} calls happened to come
     * out - there is no way to control the bit-width it chooses through that public API, only to
     * observe what it picked afterward. {@code ClientboundLevelChunkPacketData}'s constructor
     * pre-sizes the network buffer from the chunk's real, unobfuscated sections before this method
     * ever runs - only the one {@code getSections()} call inside {@code extractChunkData} is
     * redirected, not whatever separate size-estimation call happens first - so a rebuilt section
     * whose palette needed a wider bit-width than the original's crashed the server outright:
     * {@code IndexOutOfBoundsException: writerIndex(6378) + minWritableBytes(1) exceeds
     * maxCapacity(6378)} while writing a chunk on entering a modded dimension, confirmed in the
     * crash report with this class's own mixin in the call stack. A heavily-modded dimension is
     * exactly where this was likeliest to happen - more distinct registered block states server-wide
     * pushes more sections toward the palette strategies where bit-width is sensitive to exactly
     * which values got inserted and in what order.</p>
     *
     * <p>Fixing this for real needs either the buffer size estimate and the actual write to agree
     * (redirecting whatever {@code calculateChunkSize} does internally too, which was not
     * verifiable against real source here) or a substitution technique that provably cannot need
     * more bits than the section already used (real Paper's own raw palette-index bit-swap, which
     * needs lower-level access than the public {@code PalettedContainer} API exposes). Guessing
     * either under the same "cannot compile-test, one bad guess crashes the server" constraint that
     * produced this bug once already is not an acceptable way to re-enable it.</p>
     */
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
