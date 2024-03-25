package fr.modcraftmc.launcher;

import fr.modcraftmc.api.ModcraftApiRequestsExecutor;
import fr.modcraftmc.api.exception.ParsingException;
import fr.modcraftmc.api.exception.RemoteException;
import fr.modcraftmc.api.models.LauncherInfo;
import fr.modcraftmc.launcher.resources.FilesManager;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;

public class SelfUpdater {
    private static String bootstrapPath = System.getProperty("bootstrapPath");

    public record SelfUpdateResult(boolean hasUpdate, String currentVersion, String version, String changelogUrl, String bootstrapPath) {};

    private static final SelfUpdateResult NO_UPDATE_RESULT = new SelfUpdateResult(false, null, null, null, null);
    public static CompletableFuture<SelfUpdateResult> checkUpdate() {
        ModcraftApplication.LOGGER.info("Checking for update");
        if (bootstrapPath == null)
            ModcraftApplication.LOGGER.warning("bootstrapPath is empty!! this is fine if running in dev env.");
        else
            ModcraftApplication.LOGGER.info("current bootstrapPath : " + bootstrapPath);

        return CompletableFuture.supplyAsync(() -> {
            Utils.selfCatchSleep(1500);

            LauncherInfo launcherInfo = getLauncherInfo();

            if (launcherInfo == null || bootstrapPath == null) // if bootstrapPath is null we can't update
                return NO_UPDATE_RESULT;

            if (checkJar(launcherInfo)) {
                return NO_UPDATE_RESULT;
            }

            ModcraftApplication.LOGGER.info("update found!");
            //we got an update
            return new SelfUpdateResult(true, "1.0.0", "1.0.1", "https://www.youtube.com/watch?v=xvFZjo5PgG0", bootstrapPath);
        });
    }

    public static void doUpdate(String bootstrapPath) {
        ModcraftApplication.LOGGER.info("launching bootstrap");
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.directory(FilesManager.LAUNCHER_PATH);
            builder.command(FilesManager.JAVA_PATH.getPath() + "/bin/java", "-jar", bootstrapPath);
            builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

    private static LauncherInfo getLauncherInfo() {
        try {
            return ModcraftApplication.apiClient.executeRequest(ModcraftApiRequestsExecutor.getLauncherInfo());
        } catch (ParsingException | IOException | RemoteException e) {
            // that's ok if the request fail
        }
        return null;
    }

    private static boolean checkJar(LauncherInfo launcherInfo) {
        try {
            MessageDigest md5Digest = MessageDigest.getInstance("SHA1");
            //Get the checksum
            String checksum = Utils.getFileChecksum(md5Digest, FilesManager.LAUNCHER_JAR);
            return checksum.equals(launcherInfo.sha1());
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
