package fr.modcraftmc.launcher;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncExecutor {

    private static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1, new ModcraftThreadFactory());
    private static ExecutorService normalExecutorService = Executors.newSingleThreadExecutor(new ModcraftThreadFactory());

    public static Future<?> runAsyncAtRate(Runnable runnable, int rateInMinutes) {
        return runAsyncAtRate(runnable, rateInMinutes, TimeUnit.MINUTES);
    }

    public static Future<?> runAsyncAtRate(Runnable runnable, int rate, TimeUnit unit) {
        return executorService.scheduleAtFixedRate(runnable, 0, rate, unit);
    }

    public static Future<?> runAsyncAtRate(Runnable runnable,int initialdelay, int rate, TimeUnit unit) {
        return executorService.scheduleAtFixedRate(runnable, initialdelay, rate, unit);
    }

    public static void runAsync(Runnable runnable) {
        normalExecutorService.execute(runnable);
    }

    public static void shutdown() {
        executorService.shutdownNow();
        normalExecutorService.shutdownNow();
    }

    static class ModcraftThreadFactory implements ThreadFactory {

        private AtomicInteger COUNTER = new AtomicInteger();
        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("Modcraft Async Runner #" + COUNTER.incrementAndGet());
            return thread;
        }
    }
}
