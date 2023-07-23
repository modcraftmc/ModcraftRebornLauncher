package fr.modcraftmc.libs.updater;

import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.download.DownloadList;
import fr.flowarg.flowupdater.download.IProgressCallback;
import fr.flowarg.flowupdater.download.Step;
import fr.flowarg.flowupdater.utils.ModFileDeleter;
import fr.flowarg.flowupdater.utils.UpdaterOptions;
import fr.flowarg.flowupdater.versions.AbstractForgeVersion;
import fr.flowarg.flowupdater.versions.VanillaVersion;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.logger.LogManager;
import fr.modcraftmc.libs.errors.ErrorsHandler;
import fr.modcraftmc.libs.updater.forge.ModcraftForgeVersionBuilder;

import java.nio.file.Path;
import java.util.logging.Logger;

public class GameUpdater {

    public static GameUpdater INSTANCE;
    private final Path updateDirectory;
    private final ProgressCallback progressCallback;

    public static Logger LOGGER = LogManager.createLogger("Updater");
    public GameUpdater(Path updateDirectory, ProgressCallback progressCallback) {
        INSTANCE = this;
        this.updateDirectory = updateDirectory;
        this.progressCallback = progressCallback;
    }

    public void update(Runnable onUpdateFinished) {
        VanillaVersion version = new VanillaVersion.VanillaVersionBuilder().withName(ModcraftApplication.MC_VERSION).build();
        UpdaterOptions options = new UpdaterOptions.UpdaterOptionsBuilder().withSilentRead(false).build();

        AbstractForgeVersion forgeVersion = new ModcraftForgeVersionBuilder()
                .withForgeVersion(ModcraftApplication.FORGE_VERSION)
                .withMods("https://download.modcraftmc.fr/mods.json")
                .withFileDeleter(new ModFileDeleter())
                .build();

        FlowUpdater updater = new FlowUpdater.FlowUpdaterBuilder().withVanillaVersion(version).withUpdaterOptions(options).withProgressCallback(new IProgressCallback() {
            @Override
            public void update(DownloadList.DownloadInfo info) {
                System.out.println(info.getDownloadedFiles());
                System.out.println( info.getTotalToDownloadFiles());
                GameUpdater.get().getProgressCallback().onProgressUpdate("Downloading ", info.getDownloadedFiles(), info.getTotalToDownloadFiles());
            }

            @Override
            public void step(Step step) {
                IProgressCallback.super.step(step);
            }

            @Override
            public void onFileDownloaded(Path path) {
                LOGGER.info("File downloaded " + path);
            }
        }).withModLoaderVersion(forgeVersion).build();

        try {
            LOGGER.info("Updating game at " + GameUpdater.get().getUpdateDirectory());
            updater.update(GameUpdater.get().getUpdateDirectory());
        } catch (Exception e) {
            ErrorsHandler.handleError(new Exception("Error while updating the game"));
        }

        LOGGER.info("finished update");
        onUpdateFinished.run();
    }

    public static GameUpdater get() {
        if (INSTANCE == null) {
            ErrorsHandler.handleError(new IllegalStateException("GameUpdater is not initialised"));
        }
        return INSTANCE;
    }

    public Path getUpdateDirectory() {
        return updateDirectory;
    }

    public ProgressCallback getProgressCallback() {
        return progressCallback;
    }
}
