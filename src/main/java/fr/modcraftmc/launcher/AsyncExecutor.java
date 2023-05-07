package fr.modcraftmc.launcher;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

public class AsyncExecutor {

    private static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public static Future<?> runAsyncAtRate(Runnable runnable, int rateInMinutes) {
        return executorService.scheduleAtFixedRate(runnable, 0, rateInMinutes, TimeUnit.MINUTES);
    }
}
