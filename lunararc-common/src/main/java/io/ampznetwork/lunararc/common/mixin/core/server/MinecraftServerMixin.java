package io.ampznetwork.lunararc.common.mixin.core.server;

import io.ampznetwork.lunararc.common.bridge.CommandSourceBridge;
import io.ampznetwork.lunararc.common.bridge.MinecraftServerBridge;
import io.ampznetwork.lunararc.common.config.LunarArcConfig;
import io.ampznetwork.lunararc.common.server.LunarArcCommandMap;
import io.ampznetwork.lunararc.common.mod.server.LunarArcServer;
import io.ampznetwork.lunararc.common.mod.server.LunarArcRollingAverage;
import io.ampznetwork.lunararc.common.mod.util.log.LunarArcConsole;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.scheduler.CraftScheduler;
import org.bukkit.plugin.PluginLoadOrder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

@Mixin(value = MinecraftServer.class, priority = Integer.MAX_VALUE)
public abstract class MinecraftServerMixin implements MinecraftServerBridge, CommandSourceBridge {

    @Unique
    private static final Logger lunararc$logger = LoggerFactory.getLogger("LunarArc");

    @Unique
    private final Queue<Runnable> lunararc$taskQueue = new ConcurrentLinkedQueue<>();

    @Unique
    private CraftServer lunararc$craftServer;

    @Unique
    private WorldLoader.DataLoadContext lunararc$dataLoadContext;

    @Unique
    private long lunararc$bukkitStartupStartedNanos;

    @Unique
    private boolean lunararc$serverLoadEventFired;

    @Unique
    private boolean lunararc$startupPluginsEnabled;

    @Unique
    private boolean lunararc$postWorldPluginsEnabled;

    @Unique
    private boolean lunararc$tickingWorlds;

    @Unique
    private long lunararc$tpsSampleStartedNanos = net.minecraft.Util.getNanos();

    @Unique
    private final LunarArcRollingAverage lunararc$tps1 = new LunarArcRollingAverage(60);

    @Unique
    private final LunarArcRollingAverage lunararc$tps5 = new LunarArcRollingAverage(60 * 5);

    @Unique
    private final LunarArcRollingAverage lunararc$tps15 = new LunarArcRollingAverage(60 * 15);

    @Shadow
    private int tickCount;

    @Shadow
    private Map<ResourceKey<Level>, ServerLevel> levels;

    @Override
    public void lunararc$addLevel(ServerLevel level) {
        Objects.requireNonNull(level, "level");
        ServerLevel previous = this.levels.putIfAbsent(level.dimension(), level);
        if (previous != null && previous != level) {
            throw new IllegalStateException("A ServerLevel is already registered for " + level.dimension().location());
        }
        if (previous == null) {
            this.lunararc$loaderLevelLoad(level);
            this.lunararc$loaderMarkLevelsDirty();
        }
    }

    @Override
    public void lunararc$removeLevel(ServerLevel level) {
        Objects.requireNonNull(level, "level");
        if (this.levels.remove(level.dimension(), level)) {
            this.lunararc$loaderLevelUnload(level);
            this.lunararc$loaderMarkLevelsDirty();
        }
    }

    @Override
    public void lunararc$initializeDynamicLevel(ServerLevel level, PrimaryLevelData data, boolean bonusChest) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(data, "data");
        if (!data.isInitialized()) {
            lunararc$setInitialSpawn(level, data, bonusChest, data.isDebugWorld());
            data.setInitialized(true);
        }
        CraftServer craftServer = this.lunararc$requireCraftServer();
        craftServer.getPluginManager().callEvent(new org.bukkit.event.world.WorldInitEvent(craftServer.getCraftWorld(level)));
    }

    @Override
    public void lunararc$prepareDynamicLevel(ServerLevel level, ChunkProgressListener listener) {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(listener, "listener");
        this.lunararc$loaderMarkLevelsDirty();
        BlockPos spawn = level.getSharedSpawnPos();
        listener.updateSpawnPos(new ChunkPos(spawn));

        int radius = level.getGameRules().getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS);
        if (radius > 0) {

            level.getChunk(spawn.getX() >> 4, spawn.getZ() >> 4);
        }

        ForcedChunksSavedData forced = level.getDataStorage().get(ForcedChunksSavedData.factory(), "chunks");
        if (forced != null) {
            it.unimi.dsi.fastutil.longs.LongIterator iterator = forced.getChunks().iterator();
            while (iterator.hasNext()) {
                level.getChunkSource().updateChunkForced(new ChunkPos(iterator.nextLong()), true);
            }
            this.lunararc$loaderReinstatePersistentChunks(level, forced);
        }
        listener.stop();
        level.setSpawnSettings(true, true);
    }

    @Unique
    private static void lunararc$setInitialSpawn(ServerLevel level, net.minecraft.world.level.storage.ServerLevelData data,
            boolean bonusChest, boolean debug) {
        if (debug) {
            data.setSpawn(BlockPos.ZERO.above(80), 0.0F);
            return;
        }
        ServerChunkCache chunks = level.getChunkSource();
        ChunkPos origin = new ChunkPos(chunks.randomState().sampler().findSpawnPosition());
        int height = chunks.getGenerator().getSpawnHeight(level);
        if (height < level.getMinBuildHeight()) {
            BlockPos pos = origin.getWorldPosition();
            height = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX() + 8, pos.getZ() + 8);
        }
        data.setSpawn(origin.getWorldPosition().offset(8, height, 8), 0.0F);
        int x = 0, z = 0, dx = 0, dz = -1;
        for (int i = 0; i < Mth.square(11); ++i) {
            if (x >= -5 && x <= 5 && z >= -5 && z <= 5) {
                BlockPos candidate = PlayerRespawnLogic.getSpawnPosInChunk(level, new ChunkPos(origin.x + x, origin.z + z));
                if (candidate != null) {
                    data.setSpawn(candidate, 0.0F);
                    break;
                }
            }
            if (x == z || x < 0 && x == -z || x > 0 && x == 1 - z) {
                int oldDx = dx;
                dx = -dz;
                dz = oldDx;
            }
            x += dx;
            z += dz;
        }
        if (bonusChest) {
            level.registryAccess().registry(net.minecraft.core.registries.Registries.CONFIGURED_FEATURE)
                    .flatMap(reg -> reg.getHolder(net.minecraft.data.worldgen.features.MiscOverworldFeatures.BONUS_CHEST))
                    .ifPresent(feature -> feature.value().place(level, chunks.getGenerator(), level.random, data.getSpawnPos()));
        }
    }

    @Override
    public void lunararc$queueTask(Runnable runnable) {
        this.lunararc$taskQueue.add(Objects.requireNonNull(runnable, "runnable"));
    }

    @Override
    public CraftServer lunararc$getCraftServer() {
        return this.lunararc$craftServer;
    }

    @Override
    public void lunararc$setCraftServer(CraftServer craftServer) {
        Objects.requireNonNull(craftServer, "craftServer");
        if (this.lunararc$craftServer != null && this.lunararc$craftServer != craftServer) {
            throw new IllegalStateException("CraftServer already attached to this MinecraftServer");
        }
        this.lunararc$craftServer = craftServer;
    }

    @Override
    public boolean lunararc$isTickingWorlds() {
        return this.lunararc$tickingWorlds;
    }

    @Override
    public WorldLoader.DataLoadContext lunararc$getDataLoadContext() {
        WorldLoader.DataLoadContext context = this.lunararc$dataLoadContext;
        if (context == null) {
            throw new IllegalStateException("WorldLoader.DataLoadContext is not available");
        }
        return context;
    }

    /**
     * CraftBukkit's {@code MinecraftServer.recentTps}, under its real name.
     *
     * <p>The rolling averages above are LunarArc-named, so nothing outside this class can see
     * them. Plugins that read TPS overwhelmingly do not go through Bukkit's getTPS(): they reach
     * for {@code ((CraftServer) Bukkit.getServer()).getServer().recentTps}, which is the field
     * CraftBukkit adds to MinecraftServer, and a reflective lookup for it found nothing here -
     * "No reflective mapping found for field DedicatedServer#recentTps".</p>
     *
     * <p>Declared non-final where CraftBukkit has it final. Nothing links against finality - the
     * descriptor is [D either way - and a final field with an initialiser has to be merged into
     * every target constructor by Mixin, which is a risk with no benefit. Level.world, added for
     * ProtocolLib, is non-final for the same reason.</p>
     */
    public double[] recentTps = new double[3];

    /**
     * A fake {@code nextTickTime} on {@code MinecraftServer}, matching Mohist/Youer's own
     * compatibility field verbatim (their comment: "Add fake tickField worldedit need this").
     *
     * <p>Vanilla renamed its real tick-timing field to the nanosecond-based
     * {@code nextTickTimeNanos} years ago, but WorldEdit's Bukkit adapter layer still reflects for
     * the pre-rename {@code long nextTickTime} field CraftBukkit used to carry, to estimate time
     * until the next tick for its fast/async edit throttling. Without it here, that reflective
     * lookup fails - "No reflective mapping found for field MinecraftServer#nextTickTime" - which
     * is also why WorldEdit falls back to running with no Bukkit adapter at all right afterward:
     * its adapter validation treats a missing field the same as an incompatible one.</p>
     *
     * <p>Never written to, same as upstream - its only job is to exist under this name so the
     * reflective probe succeeds. Real tick pacing stays entirely on vanilla's own
     * {@code nextTickTimeNanos}.</p>
     */
    public long nextTickTime = 0L;

    @Override
    public double[] lunararc$getTps() {
        // Built element by element rather than with recentTps.clone(). An array's clone() is an
        // INVOKEVIRTUAL whose owner is the array descriptor itself, "[D", and Mixin's applicator
        // resolves every method owner it rewrites through ClassInfo.forName - which has no class
        // to return for an array type. It NPEs there and the whole mixin fails to apply, taking
        // the server down before it starts:
        //
        //   Apply Methods -> ()[D:lunararc$getTps -> Transform Instructions
        //   -> INVOKEVIRTUAL [D::clone()Ljava/lang/Object;
        //   Caused by: NullPointerException: ... ClassInfo.forName(String) is null
        //
        // This is also what CraftServer.getTPS() does with the same field, so the shape matches
        // CraftBukkit rather than merely avoiding the crash. Copying still matters: the array is
        // published to plugins, and handing out the live one lets a caller rewrite the server's
        // own TPS record.
        return new double[] { this.recentTps[0], this.recentTps[1], this.recentTps[2] };
    }

    @Inject(method = "tickChildren", at = @At("HEAD"))
    private void lunararc$beginTickChildren(CallbackInfo ci) {
        this.lunararc$tickingWorlds = true;
    }

    @Inject(method = "tickChildren", at = @At("TAIL"))
    private void lunararc$processTasks(CallbackInfo ci) {
        this.lunararc$tickingWorlds = false;
        if (this.tickCount > 0 && this.tickCount % 20 == 0) {
            long now = net.minecraft.Util.getNanos();
            long elapsed = Math.max(1L, now - this.lunararc$tpsSampleStartedNanos);
            BigDecimal currentTps = BigDecimal.valueOf(20_000_000_000L)
                    .divide(BigDecimal.valueOf(elapsed), 30, RoundingMode.HALF_UP);
            this.lunararc$tps1.add(currentTps, elapsed);
            this.lunararc$tps5.add(currentTps, elapsed);
            this.lunararc$tps15.add(currentTps, elapsed);
            this.lunararc$tpsSampleStartedNanos = now;
            // Publish to the CraftBukkit-named field in the same place, so a plugin reading
            // recentTps directly and one calling getTPS() can never disagree.
            this.recentTps[0] = this.lunararc$tps1.getAverage();
            this.recentTps[1] = this.lunararc$tps5.getAverage();
            this.recentTps[2] = this.lunararc$tps15.getAverage();
        }

        Runnable task;
        while ((task = this.lunararc$taskQueue.poll()) != null) {
            try {
                task.run();
            } catch (Exception exception) {
                lunararc$logger.error("Error executing queued LunarArc task", exception);
            }
        }

        CraftServer craftServer = this.lunararc$craftServer;
        if (craftServer != null) {
            ((CraftScheduler) craftServer.getScheduler()).mainThreadHeartbeat(this.tickCount);
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void lunararc$onInit(CallbackInfo ci) {
        io.ampznetwork.lunararc.common.server.LunarArcTimings.markServerStart();
        long initStart = io.ampznetwork.lunararc.common.server.LunarArcTimings.phaseStart();

        this.lunararc$dataLoadContext = io.ampznetwork.lunararc.common.mod.util.LunarArcWorldLoaderCapture.take();
        LunarArcServer.attach((MinecraftServer) (Object) this);

        io.ampznetwork.lunararc.common.LunarArcPaths.initialize();
        io.ampznetwork.lunararc.common.LunarArcPaths.platformRuntime(
                io.ampznetwork.lunararc.common.mod.server.LunarArcServer.platformName());
        LunarArcConfig.load();
        io.ampznetwork.lunararc.api.LunarArcServer.init();

        io.ampznetwork.lunararc.common.server.LunarArcTimings.recordStartupPhase("Server Init", initStart);
    }

    @Inject(method = "loadLevel", at = @At("HEAD"))
    private void lunararc$beforeWorldLoad(CallbackInfo ci) {
        if (this.lunararc$startupPluginsEnabled) return;
        this.lunararc$startupPluginsEnabled = true;

        if (System.getProperty("worldedit.bukkit.adapter") == null) {
            System.setProperty("worldedit.bukkit.adapter",
                    "com.sk89q.worldedit.bukkit.adapter.impl.v1_21.PaperweightAdapter");
        }

        CraftServer craftServer = this.lunararc$requireCraftServer();
        this.lunararc$bukkitStartupStartedNanos = System.nanoTime();

        long loadStart = io.ampznetwork.lunararc.common.server.LunarArcTimings.phaseStart();
        try {
            craftServer.loadPlugins();
        } catch (io.ampznetwork.lunararc.common.config.IncompatibleSoftwareException fatal) {
            // The plugin provider layer has already printed the operator-facing fatal block.
            // Mark the Minecraft server as no longer running as well as rethrowing so that
            // higher-level loader/crash guards cannot accidentally continue world startup
            // with an empty/partially populated Bukkit plugin registry.
            ((MinecraftServer) (Object) this).halt(false);
            throw fatal;
        }
        io.ampznetwork.lunararc.common.server.LunarArcTimings.recordStartupPhase("Plugin Load", loadStart);

        long enableStart = io.ampznetwork.lunararc.common.server.LunarArcTimings.phaseStart();
        this.lunararc$enablePlugins(craftServer, PluginLoadOrder.STARTUP);
        io.ampznetwork.lunararc.common.server.LunarArcTimings.recordStartupPhase("Plugin Enable STARTUP", enableStart);
    }


    @Inject(method = "loadLevel", at = @At("TAIL"))
    private void lunararc$afterWorldLoad(CallbackInfo ci) {
        if (this.lunararc$postWorldPluginsEnabled) return;
        this.lunararc$postWorldPluginsEnabled = true;

        MinecraftServer minecraftServer = (MinecraftServer) (Object) this;
        CraftServer craftServer = this.lunararc$requireCraftServer();

        long worldInitStart = io.ampznetwork.lunararc.common.server.LunarArcTimings.phaseStart();
        for (net.minecraft.server.level.ServerLevel level : minecraftServer.getAllLevels()) {
            if (!craftServer.worldLoadEventFired.add(level.dimension())) continue;
            long worldStart = io.ampznetwork.lunararc.common.server.LunarArcTimings.phaseStart();
            org.bukkit.craftbukkit.CraftWorld craftWorld = craftServer.getCraftWorld(level);
            craftServer.getPluginManager().callEvent(new org.bukkit.event.world.WorldInitEvent(craftWorld));
            craftServer.getPluginManager().callEvent(new org.bukkit.event.world.WorldLoadEvent(craftWorld));
            io.ampznetwork.lunararc.common.server.LunarArcTimings.recordStartup(
                    "World Init", level.dimension().location().toString(), worldStart);
        }
        io.ampznetwork.lunararc.common.server.LunarArcTimings.recordStartupPhase("World Init Events", worldInitStart);

        long enableStart = io.ampznetwork.lunararc.common.server.LunarArcTimings.phaseStart();
        this.lunararc$enablePlugins(craftServer, PluginLoadOrder.POSTWORLD);
        io.ampznetwork.lunararc.common.server.LunarArcTimings.recordStartupPhase("Plugin Enable POSTWORLD", enableStart);

        long commandSyncStarted = System.nanoTime();
        this.lunararc$firePaperCommandLifecycle(minecraftServer);
        this.lunararc$syncCommands(craftServer, minecraftServer);
        long commandSyncMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - commandSyncStarted);
        LunarArcConsole.success(lunararc$logger, "Bukkit command tree finalized in {}ms", commandSyncMillis);
        io.ampznetwork.lunararc.common.server.LunarArcTimings.recordStartupPhase("Command Sync", commandSyncStarted);

        if (!this.lunararc$serverLoadEventFired) {
            this.lunararc$serverLoadEventFired = true;
            craftServer.getPluginManager().callEvent(new org.bukkit.event.server.ServerLoadEvent(
                    org.bukkit.event.server.ServerLoadEvent.LoadType.STARTUP));
        }

        long bridgeStart = io.ampznetwork.lunararc.common.server.LunarArcTimings.phaseStart();

        long tier3ProbeStart = io.ampznetwork.lunararc.common.server.LunarArcTimings.phaseStart();
        io.ampznetwork.lunararc.common.server.LunarArcTier3RuntimeProbe.run(craftServer);
        io.ampznetwork.lunararc.common.server.LunarArcTimings.recordStartup(
                "Compatibility Bridge", "Tier3 Runtime Probe", tier3ProbeStart);

        long essentialsBridgeStart = io.ampznetwork.lunararc.common.server.LunarArcTimings.phaseStart();
        io.ampznetwork.lunararc.common.server.LunarArcEssentialsItemBridge.populateModdedItems(craftServer);
        io.ampznetwork.lunararc.common.server.LunarArcTimings.recordStartup(
                "Compatibility Bridge", "Essentials Items", essentialsBridgeStart);

        long antiXrayBridgeStart = io.ampznetwork.lunararc.common.server.LunarArcTimings.phaseStart();
        io.ampznetwork.lunararc.common.server.LunarArcAntiXrayOreBridge.mergeModdedOres(craftServer);
        io.ampznetwork.lunararc.common.server.LunarArcTimings.recordStartup(
                "Compatibility Bridge", "Anti-Xray Ores", antiXrayBridgeStart);

        io.ampznetwork.lunararc.common.server.LunarArcTimings.recordStartupPhase("Compatibility Bridges", bridgeStart);

        long startupMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - this.lunararc$bukkitStartupStartedNanos);
        LunarArcConsole.success(lunararc$logger, "Bukkit/Paper 1.21.1 compatibility layer ready ({}ms)", startupMillis);

        io.ampznetwork.lunararc.common.server.LunarArcTimings.logStartupSummary();
    }

    @Unique
    private void lunararc$enablePlugins(CraftServer craftServer, PluginLoadOrder order) {
        LunarArcConsole.info(lunararc$logger, "Beginning plugin enable phase {}", order);
        craftServer.enablePlugins(order);
        LunarArcConsole.success(lunararc$logger, "Completed plugin enable phase {}", order);
    }

    @Unique
    private void lunararc$firePaperCommandLifecycle(MinecraftServer minecraftServer) {
        net.minecraft.commands.Commands minecraftCommands = minecraftServer.getCommands();
        com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher =
                minecraftCommands.getDispatcher();
        io.ampznetwork.lunararc.common.server.LunarArcPaperCommands registrar =
                new io.ampznetwork.lunararc.common.server.LunarArcPaperCommands(dispatcher);
        io.ampznetwork.lunararc.common.server.LunarArcReloadableRegistrarEvent<io.papermc.paper.command.brigadier.Commands> event =
                new io.ampznetwork.lunararc.common.server.LunarArcReloadableRegistrarEvent<>(
                        registrar,
                        io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent.Cause.INITIAL);
        io.ampznetwork.lunararc.common.server.LunarArcLifecycleEventRunner.fire(
                io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents.COMMANDS, event);
    }

    @Unique
    private void lunararc$syncCommands(CraftServer craftServer, MinecraftServer minecraftServer) {
        net.minecraft.commands.Commands commands = minecraftServer.getCommands();
        com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher =
                commands.getDispatcher();

        if (craftServer.getCommandMap() instanceof LunarArcCommandMap lunarArcMap) {
            lunarArcMap.syncToBrigadier(dispatcher);
        }

        for (net.minecraft.server.level.ServerPlayer player : minecraftServer.getPlayerList().getPlayers()) {
            commands.sendCommands(player);
        }
    }

    @Override
    public org.bukkit.command.CommandSender lunararc$getBukkitSender(net.minecraft.commands.CommandSourceStack stack) {
        return this.lunararc$requireCraftServer().getConsoleSender();
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void lunararc$onStop(CallbackInfo ci) {
        io.ampznetwork.lunararc.common.server.LunarArcTimings.markShutdownStart();

        CraftServer craftServer = this.lunararc$craftServer;
        if (craftServer != null) {
            long disableStart = io.ampznetwork.lunararc.common.server.LunarArcTimings.phaseStart();
            craftServer.disablePlugins();
            io.ampznetwork.lunararc.common.server.LunarArcTimings.recordShutdownPhase("Plugin Disable", disableStart);

            long clearStart = io.ampznetwork.lunararc.common.server.LunarArcTimings.phaseStart();
            craftServer.clearPluginsForShutdown();
            craftServer.shutdownSchedulers();
            io.ampznetwork.lunararc.common.server.LunarArcTimings.recordShutdownPhase("Scheduler/Cleanup", clearStart);
        }

        long cleanupStart = io.ampznetwork.lunararc.common.server.LunarArcTimings.phaseStart();
        io.ampznetwork.lunararc.common.network.LunarArcPluginMessageOwnership.clear();
        io.ampznetwork.lunararc.common.server.LunarArcLifecycleEventRunner.resetServerState();
        org.bukkit.plugin.java.PluginClassLoader.shutdownSharedLoaders();
        io.ampznetwork.lunararc.common.server.LunarArcContext.clearServerReferences();
        io.ampznetwork.lunararc.common.server.LunarArcTimings.recordShutdownPhase("Server Cleanup", cleanupStart);
    }

    @Inject(method = "stopServer", at = @At("RETURN"))
    private void lunararc$afterStop(CallbackInfo ci) {
        io.ampznetwork.lunararc.common.server.LunarArcProfileCacheWriter.flush();
        io.ampznetwork.lunararc.common.server.LunarArcTimings.logShutdownSummary();
        io.ampznetwork.lunararc.common.server.LunarArcTimings.reset();
    }

}
