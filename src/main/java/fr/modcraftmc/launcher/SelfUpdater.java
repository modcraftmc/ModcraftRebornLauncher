package fr.modcraftmc.launcher;

import java.util.concurrent.CompletableFuture;

public class SelfUpdater {

    public record SelfUpdateResult(boolean hasUpdate, String currentVersion, String version, String changelogUrl) {};
    public static CompletableFuture<SelfUpdateResult> checkUpdate() {
        return CompletableFuture.supplyAsync(() -> {



            return new SelfUpdateResult(false, "1.0.0", "1.0.1", "https://www.youtube.com/watch?v=xvFZjo5PgG0");
        });
    }
}
