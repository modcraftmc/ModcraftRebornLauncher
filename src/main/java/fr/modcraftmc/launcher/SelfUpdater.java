package fr.modcraftmc.launcher;

import java.util.concurrent.CompletableFuture;

public class SelfUpdater {

    private static String bootstrapPath = System.getProperty("bootstrapPath");

    public record SelfUpdateResult(boolean hasUpdate, String currentVersion, String version, String changelogUrl, String bootstrapPath) {};
    public static CompletableFuture<SelfUpdateResult> checkUpdate() {

        if (bootstrapPath == null)
            ModcraftApplication.LOGGER.warning("bootstrapPath is empty!! this is fine if running in dev env.");
        else
            ModcraftApplication.LOGGER.info("current bootstrapPath : " + bootstrapPath);

        return CompletableFuture.supplyAsync(() -> {
            Utils.selfCatchSleep(1500);
            return new SelfUpdateResult(false, "1.0.0", "1.0.1", "https://www.youtube.com/watch?v=xvFZjo5PgG0", bootstrapPath);
        });
    }
}
