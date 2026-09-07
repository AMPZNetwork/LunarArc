package org.bukkit.craftbukkit.scheduler;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.minecraft.world.entity.Entity;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;


public final class CraftPaperSchedulers {
    private final CraftScheduler bukkit;
    private final Global global = new Global();
    private final Region region = new Region();
    private final Async async = new Async();

    public CraftPaperSchedulers(@NotNull CraftScheduler bukkit) {
        this.bukkit = Objects.requireNonNull(bukkit, "bukkit");
    }

    public @NotNull GlobalRegionScheduler global() {
        return global;
    }

    public @NotNull RegionScheduler region() {
        return region;
    }

    public @NotNull AsyncScheduler async() {
        return async;
    }

    public @NotNull EntityScheduler entity(@NotNull Entity entity) {
        return new EntityTasks(Objects.requireNonNull(entity, "entity"));
    }

    public void cancelTasks(@NotNull Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        // Global/region/entity schedulers use the Bukkit scheduler underneath; the
        // separate Paper async scheduler owns its own queue and must be cancelled too.
        bukkit.cancelTasks(plugin);
        async.cancelTasks(plugin);
    }

    public void shutdown() {
        beginShutdown();
        awaitShutdown();
    }


    public void beginShutdown() {
        async.shutdown();
    }

    public void awaitShutdown() {
        CraftScheduler.awaitSchedulerShutdown(async.executor, "Paper async scheduler");
    }

    private abstract static class PaperTask implements ScheduledTask {
        final Plugin plugin;
        final boolean repeating;
        final AtomicReference<ExecutionState> state = new AtomicReference<>(ExecutionState.IDLE);

        PaperTask(Plugin plugin, boolean repeating) {
            this.plugin = Objects.requireNonNull(plugin, "plugin");
            this.repeating = repeating;
        }

        @Override
        public @NotNull Plugin getOwningPlugin() {
            return plugin;
        }

        @Override
        public boolean isRepeatingTask() {
            return repeating;
        }

        @Override
        public @NotNull ExecutionState getExecutionState() {
            return state.get();
        }

        final boolean beginExecution() {
            for (;;) {
                ExecutionState now = state.get();
                if (now == ExecutionState.CANCELLED || now == ExecutionState.FINISHED || now == ExecutionState.CANCELLED_RUNNING) {
                    return false;
                }
                if (state.compareAndSet(now, ExecutionState.RUNNING)) return true;
            }
        }

        final void finishExecution() {
            for (;;) {
                ExecutionState now = state.get();
                ExecutionState next;
                if (now == ExecutionState.CANCELLED_RUNNING) {
                    next = ExecutionState.CANCELLED;
                } else if (now == ExecutionState.CANCELLED) {
                    return;
                } else {
                    next = repeating ? ExecutionState.IDLE : ExecutionState.FINISHED;
                }
                if (state.compareAndSet(now, next)) return;
            }
        }

        protected abstract void cancelBackingTask();

        @Override
        public @NotNull CancelledState cancel() {
            for (;;) {
                ExecutionState now = state.get();
                if (now == ExecutionState.CANCELLED) return CancelledState.CANCELLED_ALREADY;
                if (now == ExecutionState.FINISHED) return CancelledState.ALREADY_EXECUTED;
                if (now == ExecutionState.CANCELLED_RUNNING) return CancelledState.NEXT_RUNS_CANCELLED_ALREADY;
                if (now == ExecutionState.RUNNING) {
                    if (!repeating) return CancelledState.RUNNING;
                    if (state.compareAndSet(now, ExecutionState.CANCELLED_RUNNING)) {
                        cancelBackingTask();
                        return CancelledState.NEXT_RUNS_CANCELLED;
                    }
                    continue;
                }
                if (state.compareAndSet(now, ExecutionState.CANCELLED)) {
                    cancelBackingTask();
                    return CancelledState.CANCELLED_BY_CALLER;
                }
            }
        }
    }

    private static final class BukkitPaperTask extends PaperTask {
        volatile BukkitTask backing;

        BukkitPaperTask(Plugin plugin, boolean repeating) {
            super(plugin, repeating);
        }

        void setBacking(BukkitTask task) {
            this.backing = task;
            ExecutionState now = state.get();
            if (now == ExecutionState.CANCELLED || now == ExecutionState.CANCELLED_RUNNING) task.cancel();
        }

        @Override
        protected void cancelBackingTask() {
            BukkitTask task = backing;
            if (task != null) task.cancel();
        }
    }

    private static final class AsyncPaperTask extends PaperTask {
        volatile ScheduledFuture<?> backing;
        final Set<AsyncPaperTask> ownerTasks;

        AsyncPaperTask(Plugin plugin, boolean repeating, Set<AsyncPaperTask> ownerTasks) {
            super(plugin, repeating);
            this.ownerTasks = ownerTasks;
        }

        void setBacking(ScheduledFuture<?> task) {
            this.backing = task;
            ExecutionState now = state.get();
            if (now == ExecutionState.CANCELLED || now == ExecutionState.CANCELLED_RUNNING) task.cancel(false);
        }

        @Override
        protected void cancelBackingTask() {
            ScheduledFuture<?> task = backing;
            if (task != null) task.cancel(false);
            ownerTasks.remove(this);
        }
    }

    private BukkitPaperTask scheduleSync(Plugin plugin, Consumer<ScheduledTask> callback, long delay, long period) {
        Objects.requireNonNull(plugin, "plugin");
        Objects.requireNonNull(callback, "callback");
        boolean repeating = period > 0L;
        BukkitPaperTask paper = new BukkitPaperTask(plugin, repeating);
        Runnable run = () -> {
            if (!paper.beginExecution()) return;
            try {
                callback.accept(paper);
            } finally {
                paper.finishExecution();
            }
        };
        long safeDelay = Math.max(1L, delay);
        paper.setBacking(repeating
                ? bukkit.runTaskTimer(plugin, run, safeDelay, Math.max(1L, period))
                : bukkit.runTaskLater(plugin, run, safeDelay));
        return paper;
    }

    private final class Global implements GlobalRegionScheduler {
        @Override
        public void execute(@NotNull Plugin plugin, @NotNull Runnable run) {
            Objects.requireNonNull(run, "run");
            bukkit.runTask(plugin, run);
        }

        @Override
        public @NotNull ScheduledTask run(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task) {
            return scheduleSync(plugin, task, 1L, -1L);
        }

        @Override
        public @NotNull ScheduledTask runDelayed(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task, long delayTicks) {
            return scheduleSync(plugin, task, delayTicks, -1L);
        }

        @Override
        public @NotNull ScheduledTask runAtFixedRate(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task,
                long initialDelayTicks, long periodTicks) {
            if (periodTicks <= 0L) throw new IllegalArgumentException("periodTicks must be greater than zero");
            return scheduleSync(plugin, task, initialDelayTicks, periodTicks);
        }

        @Override
        public void cancelTasks(@NotNull Plugin plugin) {
            bukkit.cancelTasks(plugin);
        }
    }

    private final class Region implements RegionScheduler {
        private void validate(World world) {
            Objects.requireNonNull(world, "world");
        }

        @Override
        public void execute(@NotNull Plugin plugin, @NotNull World world, int chunkX, int chunkZ, @NotNull Runnable run) {
            validate(world);
            Objects.requireNonNull(run, "run");
            bukkit.runTask(plugin, run);
        }

        @Override
        public @NotNull ScheduledTask run(@NotNull Plugin plugin, @NotNull World world, int chunkX, int chunkZ,
                @NotNull Consumer<ScheduledTask> task) {
            validate(world);
            return scheduleSync(plugin, task, 1L, -1L);
        }

        @Override
        public @NotNull ScheduledTask runDelayed(@NotNull Plugin plugin, @NotNull World world, int chunkX, int chunkZ,
                @NotNull Consumer<ScheduledTask> task, long delayTicks) {
            validate(world);
            return scheduleSync(plugin, task, delayTicks, -1L);
        }

        @Override
        public @NotNull ScheduledTask runAtFixedRate(@NotNull Plugin plugin, @NotNull World world, int chunkX, int chunkZ,
                @NotNull Consumer<ScheduledTask> task, long initialDelayTicks, long periodTicks) {
            validate(world);
            if (periodTicks <= 0L) throw new IllegalArgumentException("periodTicks must be greater than zero");
            return scheduleSync(plugin, task, initialDelayTicks, periodTicks);
        }
    }

    private final class EntityTasks implements EntityScheduler {
        private final Entity entity;

        EntityTasks(Entity entity) {
            this.entity = entity;
        }

        private boolean retired() {
            return entity.isRemoved();
        }

        private @Nullable ScheduledTask schedule(Plugin plugin, Consumer<ScheduledTask> task, @Nullable Runnable retired,
                long delay, long period) {
            Objects.requireNonNull(plugin, "plugin");
            Objects.requireNonNull(task, "task");
            if (retired()) return null;
            final AtomicReference<BukkitPaperTask> ref = new AtomicReference<>();
            BukkitPaperTask scheduled = scheduleSync(plugin, ignored -> {
                BukkitPaperTask self = ref.get();
                if (retired()) {
                    if (self != null) self.cancel();
                    if (retired != null) retired.run();
                    return;
                }
                task.accept(self != null ? self : ignored);
            }, delay, period);
            ref.set(scheduled);
            return scheduled;
        }

        @Override
        public boolean execute(@NotNull Plugin plugin, @NotNull Runnable run, @Nullable Runnable retired, long delay) {
            Objects.requireNonNull(run, "run");
            if (retired()) return false;
            return schedule(plugin, ignored -> run.run(), retired, Math.max(1L, delay), -1L) != null;
        }

        @Override
        public @Nullable ScheduledTask run(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task,
                @Nullable Runnable retired) {
            return schedule(plugin, task, retired, 1L, -1L);
        }

        @Override
        public @Nullable ScheduledTask runDelayed(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task,
                @Nullable Runnable retired, long delayTicks) {
            return schedule(plugin, task, retired, Math.max(1L, delayTicks), -1L);
        }

        @Override
        public @Nullable ScheduledTask runAtFixedRate(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task,
                @Nullable Runnable retired, long initialDelayTicks, long periodTicks) {
            if (periodTicks <= 0L) throw new IllegalArgumentException("periodTicks must be greater than zero");
            return schedule(plugin, task, retired, Math.max(1L, initialDelayTicks), periodTicks);
        }
    }

    private final class Async implements AsyncScheduler {
        private final ScheduledThreadPoolExecutor executor = createExecutor();

        private ScheduledThreadPoolExecutor createExecutor() {
            ScheduledThreadPoolExecutor created = new ScheduledThreadPoolExecutor(
                    Math.max(2, Runtime.getRuntime().availableProcessors() / 2), runnable -> {
                        Thread thread = new Thread(runnable, "LunarArc Paper Async Scheduler");
                        thread.setDaemon(true);
                        return thread;
                    });
            created.setRemoveOnCancelPolicy(true);
            created.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
            created.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
            return created;
        }
        private final Set<AsyncPaperTask> tasks = ConcurrentHashMap.newKeySet();

        private AsyncPaperTask schedule(Plugin plugin, Consumer<ScheduledTask> callback, long initialDelay, long period, TimeUnit unit) {
            Objects.requireNonNull(plugin, "plugin");
            Objects.requireNonNull(callback, "callback");
            Objects.requireNonNull(unit, "unit");
            if (!plugin.isEnabled()) {
                throw new IllegalPluginAccessException("Plugin attempted to register async task while disabled");
            }
            if (initialDelay < 0L) throw new IllegalArgumentException("delay cannot be negative");
            if (period == 0L) throw new IllegalArgumentException("period must be greater than zero");
            boolean repeating = period > 0L;
            AsyncPaperTask paper = new AsyncPaperTask(plugin, repeating, tasks);
            tasks.add(paper);
            if (!plugin.isEnabled()) {
                paper.cancel();
                throw new IllegalPluginAccessException("Plugin attempted to register async task while disabled");
            }
            Runnable run = () -> {
                if (!plugin.isEnabled()) paper.cancel();
                if (!paper.beginExecution()) {
                    tasks.remove(paper);
                    return;
                }
                try {
                    callback.accept(paper);
                } catch (Throwable throwable) {
                    plugin.getLogger().log(java.util.logging.Level.SEVERE, "Async scheduler task generated an exception", throwable);
                } finally {
                    paper.finishExecution();
                    ScheduledTask.ExecutionState state = paper.getExecutionState();
                    if (state == ScheduledTask.ExecutionState.FINISHED || state == ScheduledTask.ExecutionState.CANCELLED) tasks.remove(paper);
                }
            };
            paper.setBacking(repeating
                    ? executor.scheduleAtFixedRate(run, initialDelay, period, unit)
                    : executor.schedule(run, initialDelay, unit));
            return paper;
        }

        @Override
        public @NotNull ScheduledTask runNow(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task) {
            return schedule(plugin, task, 0L, -1L, TimeUnit.NANOSECONDS);
        }

        @Override
        public @NotNull ScheduledTask runDelayed(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task,
                long delay, @NotNull TimeUnit unit) {
            return schedule(plugin, task, delay, -1L, unit);
        }

        @Override
        public @NotNull ScheduledTask runAtFixedRate(@NotNull Plugin plugin, @NotNull Consumer<ScheduledTask> task,
                long initialDelay, long period, @NotNull TimeUnit unit) {
            if (period <= 0L) throw new IllegalArgumentException("period must be greater than zero");
            return schedule(plugin, task, initialDelay, period, unit);
        }

        @Override
        public void cancelTasks(@NotNull Plugin plugin) {
            Objects.requireNonNull(plugin, "plugin");
            for (AsyncPaperTask task : Set.copyOf(tasks)) {
                if (task.plugin.equals(plugin)) task.cancel();
            }
            executor.purge();
        }

        private void shutdown() {
            for (AsyncPaperTask task : Set.copyOf(tasks)) task.cancel();
            tasks.clear();
            executor.shutdownNow();
        }
    }
}
