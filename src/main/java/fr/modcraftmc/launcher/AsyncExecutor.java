package fr.modcraftmc.launcher;

import java.util.concurrent.*;

public class AsyncExecutor {

    private static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private static ExecutorService normalExecutorService = Executors.newSingleThreadExecutor();

    public static Future<?> runAsyncAtRate(Runnable runnable, int rateInMinutes) {
        return runAsyncAtRate(runnable, rateInMinutes, TimeUnit.MINUTES);
    }

    public static Future<?> runAsyncAtRate(Runnable runnable, int rate, TimeUnit unit) {
        return executorService.scheduleAtFixedRate(runnable, 0, rate, unit);
    }

    public static void runAsync(Runnable runnable) {
        normalExecutorService.execute(runnable);
    }

    public static void shutdown() {
        executorService.shutdownNow();
        normalExecutorService.shutdownNow();
    }
}
