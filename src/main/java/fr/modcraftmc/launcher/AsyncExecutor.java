package fr.modcraftmc.launcher;

import java.util.concurrent.*;

public class AsyncExecutor {

    private static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private static ExecutorService normalExecutorService = Executors.newSingleThreadExecutor();


    public static Future<?> runAsyncAtRate(Runnable runnable, int rateInMinutes) {
        return executorService.scheduleAtFixedRate(runnable, 0, rateInMinutes, TimeUnit.MINUTES);
    }

    public static void runAsync(Runnable runnable) {
        normalExecutorService.execute(runnable);
    }
}
