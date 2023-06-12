package fr.modcraftmc.libs.updater;

import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.logger.LogManager;
import fr.modcraftmc.libs.updater.phases.GameDownload;

import java.nio.file.Path;
import java.util.logging.Logger;

public class GameUpdater {

    public static GameUpdater INSTANCE;
    private final String updateServer;
    private final Path updateDirectory;
    private final ProgressCallback progressCallback;

    public static String MANIFEST_ENDPOINT = "/mods.json";
    public static String IGNORELIST_ENDPOINT = "/metadata/ignorelist.txt";

    public static Logger LOGGER = LogManager.createLogger("Updater");
    public GameUpdater(String updateServer, Path updateDirectory, ProgressCallback progressCallback) {
        INSTANCE = this;
        this.updateServer = updateServer;
        this.updateDirectory = updateDirectory;
        this.progressCallback = progressCallback;
    }

    public void update(Runnable onUpdateFinished) {
            if (!GameDownload.isUpToDate())
                GameDownload.download();

            ModcraftApplication.LOGGER.info("finished update");

            onUpdateFinished.run();
    }

    public static GameUpdater get() {
        if (INSTANCE == null) {
            //TODO: crash reporter
            throw new IllegalStateException("GameUpdater is not initialised !");
        }
        return INSTANCE;
    }

    public String getUpdateServer() {
        return updateServer;
    }

    public Path getUpdateDirectory() {
        return updateDirectory;
    }

    public ProgressCallback getProgressCallback() {
        return progressCallback;
    }
}
