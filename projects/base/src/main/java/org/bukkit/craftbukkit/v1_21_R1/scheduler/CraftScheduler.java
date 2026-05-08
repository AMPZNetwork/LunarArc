package org.bukkit.craftbukkit.v1_21_R1.scheduler;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitWorker;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class CraftScheduler implements BukkitScheduler {
    private final AtomicInteger taskCounter = new AtomicInteger();
    private final List<CraftTask> pendingTasks = new CopyOnWriteArrayList<>();
    private final ExecutorService asyncExecutor = Executors.newCachedThreadPool();
    private int currentTick = 0;

    public void mainThreadHeartbeat(int tick) {
        this.currentTick = tick;
        Iterator<CraftTask> it = pendingTasks.iterator();
        while (it.hasNext()) {
            CraftTask task = it.next();
            if (task.isSync() && task.shouldRun(tick)) {
                try {
                    task.run();
                } catch (Throwable t) {
                    task.getOwner().getLogger().log(java.util.logging.Level.SEVERE, "Next tick task failed", t);
                }
                if (!task.isRepeating()) {
                    pendingTasks.remove(task);
                }
            }
        }
    }

    private BukkitTask handle(CraftTask task, long delay) {
        task.setNextRun(currentTick + (int) delay);
        if (task.isSync()) {
            pendingTasks.add(task);
        } else {
            if (delay <= 0) {
                asyncExecutor.execute(task);
            } else {
                CompletableFuture.runAsync(() -> {
                    try {
                        // Very rough delay for async tasks
                        Thread.sleep(delay * 50);
                        asyncExecutor.execute(task);
                    } catch (InterruptedException ignored) {}
                });
            }
        }
        return task;
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
        return handle(new CraftTask(plugin, runnable, taskCounter.getAndIncrement(), true), 0);
    }

    @Override
    public void runTask(@NotNull Plugin plugin, @NotNull Consumer<? super BukkitTask> task) {
        runTask(plugin, () -> task.accept(null));
    }

    @Override
    public @NotNull BukkitTask runTaskAsynchronously(@NotNull Plugin plugin, @NotNull Runnable runnable) {
        return handle(new CraftTask(plugin, runnable, taskCounter.getAndIncrement(), false), 0);
    }

    @Override
    public void runTaskAsynchronously(@NotNull Plugin plugin, @NotNull Consumer<? super BukkitTask> task) {
        runTaskAsynchronously(plugin, () -> task.accept(null));
    }

    @Override
    public @NotNull BukkitTask runTaskLater(@NotNull Plugin plugin, @NotNull Runnable runnable, long delay) {
        return handle(new CraftTask(plugin, runnable, taskCounter.getAndIncrement(), true), delay);
    }

    @Override
    public void runTaskLater(@NotNull Plugin plugin, @NotNull Consumer<? super BukkitTask> task, long delay) {
        runTaskLater(plugin, () -> task.accept(null), delay);
    }

    @Override
    public @NotNull BukkitTask runTaskLaterAsynchronously(@NotNull Plugin plugin, @NotNull Runnable runnable, long delay) {
        return handle(new CraftTask(plugin, runnable, taskCounter.getAndIncrement(), false), delay);
    }

    @Override
    public void runTaskLaterAsynchronously(@NotNull Plugin plugin, @NotNull Consumer<? super BukkitTask> task, long delay) {
        runTaskLaterAsynchronously(plugin, () -> task.accept(null), delay);
    }

    @Override
    public @NotNull BukkitTask runTaskTimer(@NotNull Plugin plugin, @NotNull Runnable runnable, long delay, long period) {
        CraftTask task = new CraftTask(plugin, runnable, taskCounter.getAndIncrement(), true);
        task.setPeriod((int) period);
        return handle(task, delay);
    }

    @Override
    public void runTaskTimer(@NotNull Plugin plugin, @NotNull Consumer<? super BukkitTask> task, long delay, long period) {
        runTaskTimer(plugin, () -> task.accept(null), delay, period);
    }

    @Override
    public @NotNull BukkitTask runTaskTimerAsynchronously(@NotNull Plugin plugin, @NotNull Runnable runnable, long delay, long period) {
        CraftTask task = new CraftTask(plugin, runnable, taskCounter.getAndIncrement(), false);
        task.setPeriod((int) period);
        return handle(task, delay);
    }

    @Override
    public void runTaskTimerAsynchronously(@NotNull Plugin plugin, @NotNull Consumer<? super BukkitTask> task, long delay, long period) {
        runTaskTimerAsynchronously(plugin, () -> task.accept(null), delay, period);
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

    @Override
    public void cancelTask(int taskId) {
        pendingTasks.removeIf(task -> task.getTaskId() == taskId);
    }

    @Override
    public void cancelTasks(@NotNull Plugin plugin) {
        pendingTasks.removeIf(task -> task.getOwner().equals(plugin));
    }

    @Override
    public boolean isQueued(int taskId) {
        return pendingTasks.stream().anyMatch(t -> t.getTaskId() == taskId);
    }

    @Override
    public boolean isCurrentlyRunning(int taskId) {
        return false;
    }

    @Override
    public @NotNull List<BukkitWorker> getActiveWorkers() {
        return new ArrayList<>();
    }

    @Override
    public @NotNull <T> Future<T> callSyncMethod(@NotNull Plugin plugin, @NotNull Callable<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();
        runTask(plugin, () -> {
            try {
                future.complete(task.call());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public @NotNull List<BukkitTask> getPendingTasks() {
        return new ArrayList<>(pendingTasks);
    }

    @Override
    public @NotNull Executor getMainThreadExecutor(@NotNull Plugin plugin) {
        return runnable -> runTask(plugin, runnable);
    }

    // --- Inner Task Class ---
    private static class CraftTask implements BukkitTask, Runnable {
        private final Plugin owner;
        private final Runnable task;
        private final int id;
        private final boolean sync;
        private int nextRun;
        private int period = -1;

        public CraftTask(Plugin owner, Runnable task, int id, boolean sync) {
            this.owner = owner;
            this.task = task;
            this.id = id;
            this.sync = sync;
        }

        public void setNextRun(int nextRun) { this.nextRun = nextRun; }
        public void setPeriod(int period) { this.period = period; }
        public boolean isRepeating() { return period > 0; }
        public boolean shouldRun(int currentTick) { return currentTick >= nextRun; }

        @Override public int getTaskId() { return id; }
        @Override public @NotNull Plugin getOwner() { return owner; }
        @Override public boolean isSync() { return sync; }
        @Override public boolean isCancelled() { return false; }
        @Override public void cancel() {}

        @Override
        public void run() {
            try {
                task.run();
            } finally {
                if (isRepeating()) {
                    nextRun += period;
                }
            }
        }
    }
}
