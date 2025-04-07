package fr.modcraftmc.launcher;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncExecutor {

    private static final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new ModcraftThreadFactory("Scheduled task"));
    private static final ExecutorService normalExecutorService = Executors.newFixedThreadPool(10, new ModcraftThreadFactory("Normal task"));

    public static Future<?> runAsyncAtRate(Runnable runnable, int rate, TimeUnit unit) {
        return scheduledExecutorService.scheduleAtFixedRate(runnable, 0, rate, unit);
    }

    public static Future<?> runAsyncAtRate(Runnable runnable, int initialdelay, int rate, TimeUnit unit) {
        return scheduledExecutorService.scheduleAtFixedRate(runnable, initialdelay, rate, unit);
    }

    public static void runAsync(Runnable runnable) {
        normalExecutorService.execute(runnable);
    }

    public static Future<?> submitAsync(Runnable runnable) {
        return normalExecutorService.submit(runnable);
    }

    public static void shutdown() {
        scheduledExecutorService.shutdownNow();
        normalExecutorService.shutdownNow();
    }

    static class ModcraftThreadFactory implements ThreadFactory {

        private final AtomicInteger COUNTER = new AtomicInteger();
        private final String name;

        public ModcraftThreadFactory(String scheduledExecutor) {
            this.name = scheduledExecutor;
        }

        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(String.format("ModcraftLauncher Async Runner (%s) # %s", name, COUNTER.getAndIncrement()));
            thread.setUncaughtExceptionHandler((t, e) -> {ModcraftApplication.LOGGER.severe(e.getMessage());});
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
        }
    }
}
