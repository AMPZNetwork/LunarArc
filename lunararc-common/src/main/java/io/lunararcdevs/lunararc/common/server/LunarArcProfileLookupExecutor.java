package io.lunararcdevs.lunararc.common.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class LunarArcProfileLookupExecutor {
    private static final AtomicInteger THREAD_IDS = new AtomicInteger();
    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(2, 2, 30, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(), runnable -> {
                Thread thread = new Thread(runnable, "LunarArc Profile Lookup-" + THREAD_IDS.incrementAndGet());
                thread.setDaemon(true);
                return thread;
            });

    static {
        EXECUTOR.allowCoreThreadTimeOut(true);
    }

    private LunarArcProfileLookupExecutor() {}

    public static ExecutorService executor() {
        return EXECUTOR;
    }
}
