package org.bukkit.craftbukkit.scheduler;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitWorker;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class CraftScheduler implements BukkitScheduler {
    private final AtomicInteger taskCounter = new AtomicInteger(1);
    private final ConcurrentMap<Integer, CraftTask> tasks = new ConcurrentHashMap<>();
    private final java.util.LinkedHashMap<Integer, CraftTask> syncTasks = new java.util.LinkedHashMap<>();
    private CraftTask[] scheduledSnapshot;
    private final ConcurrentMap<Thread, CraftWorker> activeWorkers = new ConcurrentHashMap<>();
    private final ScheduledThreadPoolExecutor asyncExecutor = createAsyncExecutor();

    private static ScheduledThreadPoolExecutor createAsyncExecutor() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
                Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
                runnable -> {
                    Thread thread = new Thread(runnable, "LunarArc Scheduler");
                    thread.setDaemon(true);
                    return thread;
                });
        // Cancelled delayed/repeating plugin tasks must not remain in the delay queue
        // retaining plugin instances/classloaders until their original deadline.
        executor.setRemoveOnCancelPolicy(true);
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        return executor;
    }
    private volatile int currentTick;

    public void mainThreadHeartbeat(int tick) {
        this.currentTick = tick;

        for (CraftTask task : scheduledSnapshot()) {
            if (task.isCancelled()) {
                finish(task);
                continue;
            }
            if (!task.shouldRun(tick)) continue;

            if (!task.isSync()) {
                if (!task.isRepeating()) removeScheduled(task);
                else task.setNextRun(tick + task.getPeriod());
                asyncExecutor.execute(() -> {
                    execute(task);
                    if (task.isCancelled() || !task.isRepeating()) finish(task);
                });
                continue;
            }

            execute(task);

            if (task.isCancelled() || !task.isRepeating()) {
                finish(task);
            } else {
                task.setNextRun(tick + task.getPeriod());
            }
        }
    }

    private BukkitTask schedule(CraftTask task, long delay) {
        validatePlugin(task.getOwner());
        if (delay < 0) delay = 0;
        tasks.put(task.getTaskId(), task);

        task.setNextRun(currentTick + safeTicks(delay));
        synchronized (syncTasks) {
            syncTasks.put(task.getTaskId(), task);
            scheduledSnapshot = null;
        }
        return task;
    }

    private void execute(CraftTask task) {
        task.setRunning(true);
        CraftWorker worker = null;

        if (!task.isSync()) {
            worker = new CraftWorker(task, Thread.currentThread());
            activeWorkers.put(Thread.currentThread(), worker);
        }

        Thread thread = Thread.currentThread();
        ClassLoader previous = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(task.getOwner().getClass().getClassLoader());
            task.execute();
        } catch (Throwable throwable) {
            task.getOwner().getLogger().log(
                    java.util.logging.Level.SEVERE,
                    "Task #" + task.getTaskId() + " for " + task.getOwner().getName() + " generated an exception",
                    throwable
            );
        } finally {
            thread.setContextClassLoader(previous);
            task.setRunning(false);
            if (worker != null) activeWorkers.remove(Thread.currentThread(), worker);
        }
    }

    private void finish(CraftTask task) {
        removeScheduled(task);
        tasks.remove(task.getTaskId(), task);
    }

    private CraftTask[] scheduledSnapshot() {
        synchronized (syncTasks) {
            if (scheduledSnapshot == null) scheduledSnapshot = syncTasks.values().toArray(CraftTask[]::new);
            return scheduledSnapshot;
        }
    }

    private void removeScheduled(CraftTask task) {
        synchronized (syncTasks) {
            if (syncTasks.remove(task.getTaskId(), task)) scheduledSnapshot = null;
        }
    }

    private static void validatePlugin(Plugin plugin) {
        java.util.Objects.requireNonNull(plugin, "plugin");
        if (!plugin.isEnabled()) {
            throw new IllegalPluginAccessException("Plugin attempted to register task while disabled");
        }
    }

    private static int safeTicks(long ticks) {
        return ticks > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) ticks;
    }

    @Override
    public int scheduleSyncDelayedTask(@NotNull Plugin plugin, @NotNull Runnable task, long delay) {
        return runTaskLater(plugin, task, delay).getTaskId();
    }

    @Override
    public int scheduleSyncDelayedTask(@NotNull Plugin plugin, @NotNull Runnable task) {
        return runTask(plugin, task).getTaskId();
    }

    @Override
    public int scheduleSyncRepeatingTask(@NotNull Plugin plugin, @NotNull Runnable task, long delay, long period) {
        return runTaskTimer(plugin, task, delay, period).getTaskId();
    }

    @Override
    public int scheduleAsyncDelayedTask(@NotNull Plugin plugin, @NotNull Runnable task, long delay) {
        return runTaskLaterAsynchronously(plugin, task, delay).getTaskId();
    }

    @Override
    public int scheduleAsyncDelayedTask(@NotNull Plugin plugin, @NotNull Runnable task) {
        return runTaskAsynchronously(plugin, task).getTaskId();
    }

    @Override
    public int scheduleAsyncRepeatingTask(@NotNull Plugin plugin, @NotNull Runnable task, long delay, long period) {
        return runTaskTimerAsynchronously(plugin, task, delay, period).getTaskId();
    }

    @Override
    public @NotNull BukkitTask runTask(@NotNull Plugin plugin, @NotNull Runnable runnable) {
        return schedule(new CraftTask(plugin, ignored -> runnable.run(), taskCounter.getAndIncrement(), true), 0);
    }

    @Override
    public void runTask(@NotNull Plugin plugin, @NotNull Consumer<? super BukkitTask> task) {
        schedule(new CraftTask(plugin, task, taskCounter.getAndIncrement(), true), 0);
    }

    @Override
    public @NotNull BukkitTask runTaskAsynchronously(@NotNull Plugin plugin, @NotNull Runnable runnable) {
        return schedule(new CraftTask(plugin, ignored -> runnable.run(), taskCounter.getAndIncrement(), false), 0);
    }

    @Override
    public void runTaskAsynchronously(@NotNull Plugin plugin, @NotNull Consumer<? super BukkitTask> task) {
        schedule(new CraftTask(plugin, task, taskCounter.getAndIncrement(), false), 0);
    }

    @Override
    public @NotNull BukkitTask runTaskLater(@NotNull Plugin plugin, @NotNull Runnable runnable, long delay) {
        return schedule(new CraftTask(plugin, ignored -> runnable.run(), taskCounter.getAndIncrement(), true), delay);
    }

    @Override
    public void runTaskLater(@NotNull Plugin plugin, @NotNull Consumer<? super BukkitTask> task, long delay) {
        schedule(new CraftTask(plugin, task, taskCounter.getAndIncrement(), true), delay);
    }

    @Override
    public @NotNull BukkitTask runTaskLaterAsynchronously(@NotNull Plugin plugin, @NotNull Runnable runnable, long delay) {
        return schedule(new CraftTask(plugin, ignored -> runnable.run(), taskCounter.getAndIncrement(), false), delay);
    }

    @Override
    public void runTaskLaterAsynchronously(@NotNull Plugin plugin, @NotNull Consumer<? super BukkitTask> task, long delay) {
        schedule(new CraftTask(plugin, task, taskCounter.getAndIncrement(), false), delay);
    }

    @Override
    public @NotNull BukkitTask runTaskTimer(@NotNull Plugin plugin, @NotNull Runnable runnable, long delay, long period) {
        return schedule(repeating(plugin, ignored -> runnable.run(), true, period), delay);
    }

    @Override
    public void runTaskTimer(@NotNull Plugin plugin, @NotNull Consumer<? super BukkitTask> task, long delay, long period) {
        schedule(repeating(plugin, task, true, period), delay);
    }

    @Override
    public @NotNull BukkitTask runTaskTimerAsynchronously(@NotNull Plugin plugin, @NotNull Runnable runnable, long delay, long period) {
        return schedule(repeating(plugin, ignored -> runnable.run(), false, period), delay);
    }

    @Override
    public void runTaskTimerAsynchronously(@NotNull Plugin plugin, @NotNull Consumer<? super BukkitTask> task, long delay, long period) {
        schedule(repeating(plugin, task, false, period), delay);
    }

    private CraftTask repeating(Plugin plugin, Consumer<? super BukkitTask> task, boolean sync, long period) {
        if (period <= 0) throw new IllegalArgumentException("Period must be greater than zero");
        CraftTask craftTask = new CraftTask(plugin, task, taskCounter.getAndIncrement(), sync);
        craftTask.setPeriod(safeTicks(period));
        return craftTask;
    }

    public int scheduleSyncDelayedTask(@NotNull Plugin plugin, @NotNull BukkitRunnable task, long delay) {
        return runTaskLater(plugin, task, delay).getTaskId();
    }

    public int scheduleSyncDelayedTask(@NotNull Plugin plugin, @NotNull BukkitRunnable task) {
        return runTask(plugin, task).getTaskId();
    }

    public int scheduleSyncRepeatingTask(@NotNull Plugin plugin, @NotNull BukkitRunnable task, long delay, long period) {
        return runTaskTimer(plugin, task, delay, period).getTaskId();
    }

    public int scheduleAsyncDelayedTask(@NotNull Plugin plugin, @NotNull BukkitRunnable task, long delay) {
        return runTaskLaterAsynchronously(plugin, task, delay).getTaskId();
    }

    public int scheduleAsyncDelayedTask(@NotNull Plugin plugin, @NotNull BukkitRunnable task) {
        return runTaskAsynchronously(plugin, task).getTaskId();
    }

    public int scheduleAsyncRepeatingTask(@NotNull Plugin plugin, @NotNull BukkitRunnable task, long delay, long period) {
        return runTaskTimerAsynchronously(plugin, task, delay, period).getTaskId();
    }

    public @NotNull BukkitTask runTask(@NotNull Plugin plugin, @NotNull BukkitRunnable runnable) {
        return runTask(plugin, (Runnable) runnable);
    }

    public @NotNull BukkitTask runTaskAsynchronously(@NotNull Plugin plugin, @NotNull BukkitRunnable runnable) {
        return runTaskAsynchronously(plugin, (Runnable) runnable);
    }

    public @NotNull BukkitTask runTaskLater(@NotNull Plugin plugin, @NotNull BukkitRunnable runnable, long delay) {
        return runTaskLater(plugin, (Runnable) runnable, delay);
    }

    public @NotNull BukkitTask runTaskLaterAsynchronously(@NotNull Plugin plugin, @NotNull BukkitRunnable runnable, long delay) {
        return runTaskLaterAsynchronously(plugin, (Runnable) runnable, delay);
    }

    public @NotNull BukkitTask runTaskTimer(@NotNull Plugin plugin, @NotNull BukkitRunnable runnable, long delay, long period) {
        return runTaskTimer(plugin, (Runnable) runnable, delay, period);
    }

    public @NotNull BukkitTask runTaskTimerAsynchronously(@NotNull Plugin plugin, @NotNull BukkitRunnable runnable, long delay, long period) {
        return runTaskTimerAsynchronously(plugin, (Runnable) runnable, delay, period);
    }


    public void shutdown() {
        beginShutdown();
        awaitShutdown();
    }

    public void beginShutdown() {
        for (CraftTask task : new ArrayList<>(tasks.values())) task.cancel();
        synchronized (syncTasks) {
            syncTasks.clear();
            scheduledSnapshot = null;
        }
        tasks.clear();
        asyncExecutor.shutdownNow();
    }

    public void awaitShutdown() {
        awaitSchedulerShutdown(asyncExecutor, "Bukkit async scheduler");
        activeWorkers.clear();
    }

    static void awaitSchedulerShutdown(java.util.concurrent.ExecutorService executor, String name) {
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                org.slf4j.LoggerFactory.getLogger("LunarArc").info(
                        "{} still had tasks running after being interrupted; leaving them to the "
                                + "daemon threads rather than delaying shutdown.", name);
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void cancelTask(int taskId) {
        CraftTask task = tasks.get(taskId);
        if (task != null) task.cancel();
    }

    @Override
    public void cancelTasks(@NotNull Plugin plugin) {
        for (CraftTask task : new ArrayList<>(tasks.values())) {
            if (task.getOwner().equals(plugin)) task.cancel();
        }
        asyncExecutor.purge();
    }

    @Override
    public boolean isQueued(int taskId) {
        CraftTask task = tasks.get(taskId);
        return task != null && !task.isCancelled() && !task.isRunning();
    }

    @Override
    public boolean isCurrentlyRunning(int taskId) {
        CraftTask task = tasks.get(taskId);
        return task != null && task.isRunning();
    }

    @Override
    public @NotNull List<BukkitWorker> getActiveWorkers() {
        return new ArrayList<>(activeWorkers.values());
    }

    @Override
    public @NotNull <T> Future<T> callSyncMethod(@NotNull Plugin plugin, @NotNull Callable<T> task) {
        java.util.concurrent.atomic.AtomicReference<CraftTask> backing = new java.util.concurrent.atomic.AtomicReference<>();
        FutureTask<T> future = new FutureTask<>(task) {
            @Override
            protected void done() {
                CraftTask scheduled = backing.get();
                if (isCancelled() && scheduled != null) scheduled.cancel();
            }
        };
        CraftTask scheduled = new CraftTask(plugin, ignored -> future.run(), taskCounter.getAndIncrement(), true);
        backing.set(scheduled);
        scheduled.setFuture(future);
        schedule(scheduled, 0);
        return future;
    }

    @Override
    public @NotNull List<BukkitTask> getPendingTasks() {
        List<BukkitTask> pending = new ArrayList<>();
        for (CraftTask task : tasks.values()) {
            if (!task.isCancelled()) pending.add(task);
        }
        return pending;
    }

    @Override
    public @NotNull Executor getMainThreadExecutor(@NotNull Plugin plugin) {
        return runnable -> runTask(plugin, runnable);
    }

    private final class CraftTask implements BukkitTask {
        private final Plugin owner;
        private final Consumer<? super BukkitTask> task;
        private final int id;
        private final boolean sync;
        private volatile int nextRun;
        private volatile int period = -1;
        private volatile boolean cancelled;
        private final AtomicInteger running = new AtomicInteger();
        private volatile Future<?> future;

        private CraftTask(Plugin owner, Consumer<? super BukkitTask> task, int id, boolean sync) {
            this.owner = owner;
            this.task = task;
            this.id = id;
            this.sync = sync;
        }

        private void execute() {
            if (!cancelled) task.accept(this);
        }

        private void setNextRun(int nextRun) {
            this.nextRun = nextRun;
        }

        private void setPeriod(int period) {
            this.period = period;
        }

        private int getPeriod() {
            return period;
        }

        private boolean isRepeating() {
            return period > 0;
        }

        private boolean shouldRun(int tick) {
            return tick >= nextRun;
        }

        private void setFuture(Future<?> future) {
            this.future = future;
            // Cancellation may race with ScheduledExecutorService returning the
            // future. Publish first, then re-check so a cancelled repeating task
            // can never resurrect and retain its plugin/classloader in the queue.
            if (cancelled) future.cancel(false);
        }

        private void setRunning(boolean running) {
            if (running) this.running.incrementAndGet();
            else this.running.decrementAndGet();
        }

        private boolean isRunning() {
            return running.get() > 0;
        }

        @Override
        public int getTaskId() {
            return id;
        }

        @Override
        public @NotNull Plugin getOwner() {
            return owner;
        }

        @Override
        public boolean isSync() {
            return sync;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void cancel() {
            if (cancelled) return;
            cancelled = true;
            Future<?> scheduled = future;
            if (scheduled != null) scheduled.cancel(false);
            finish(this);
        }
    }

    private static final class CraftWorker implements BukkitWorker {
        private final CraftTask task;
        private final Thread thread;

        private CraftWorker(CraftTask task, Thread thread) {
            this.task = task;
            this.thread = thread;
        }

        @Override
        public int getTaskId() {
            return task.getTaskId();
        }

        @Override
        public @NotNull Plugin getOwner() {
            return task.getOwner();
        }

        @Override
        public @NotNull Thread getThread() {
            return thread;
        }
    }
}
