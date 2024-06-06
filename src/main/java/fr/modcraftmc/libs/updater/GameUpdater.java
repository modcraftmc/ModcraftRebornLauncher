package fr.modcraftmc.libs.updater;

import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.download.json.Mod;
import fr.flowarg.flowupdater.utils.ModFileDeleter;
import fr.flowarg.flowupdater.utils.UpdaterOptions;
import fr.flowarg.flowupdater.versions.AbstractForgeVersion;
import fr.flowarg.flowupdater.versions.VanillaVersion;
import fr.modcraftmc.api.ModcraftApiRequestsExecutor;
import fr.modcraftmc.api.exception.ParsingException;
import fr.modcraftmc.api.exception.RemoteException;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.controllers.MainControllerV2;
import fr.modcraftmc.launcher.logger.LogManager;
import fr.modcraftmc.launcher.resources.FilesManager;
import fr.modcraftmc.libs.errors.ErrorsHandler;
import fr.modcraftmc.libs.updater.forge.ModcraftForgeVersionBuilder;
import javafx.application.Platform;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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

    public void update(MainControllerV2 controller, Runnable onUpdateFinished) {
        VanillaVersion version = new VanillaVersion.VanillaVersionBuilder().withName(ModcraftApplication.MC_VERSION).build();
        UpdaterOptions options = new UpdaterOptions.UpdaterOptionsBuilder().withSilentRead(false).withJavaPath(FilesManager.JAVA_EXE.getPath()).build();

        List<Mod> mods = new ArrayList<>();
        try {
            ModcraftApplication.apiClient.executeRequest(ModcraftApiRequestsExecutor.getClientModsConfig()).mods().forEach(modInfo -> {
                mods.add(new Mod(modInfo.name(), modInfo.downloadUrl(), modInfo.sha1(), modInfo.size()));
            });
        } catch (ParsingException | IOException | RemoteException e) {
            ErrorsHandler.handleError(e);
            controller.setLauncherState(MainControllerV2.State.IDLE);
            return;
        }

        AbstractForgeVersion forgeVersion = new ModcraftForgeVersionBuilder()
                .withForgeVersion(ModcraftApplication.FORGE_VERSION)
                .withMods(mods)
                .withFileDeleter(new ModFileDeleter())
                .build();

        FlowUpdater updater = new FlowUpdater.FlowUpdaterBuilder()
                .withVanillaVersion(version)
                .withUpdaterOptions(options)
                .withLogger(new fr.modcraftmc.libs.updater.Logger())
                .withProgressCallback(new UpdaterProgessCallback())
                .withModLoaderVersion(forgeVersion)
                .withExternalFiles()
                .build();

        try {
            LOGGER.info("Updating game at " + GameUpdater.get().getUpdateDirectory());
            updater.update(GameUpdater.get().getUpdateDirectory());
        } catch (Exception e) {
            ErrorsHandler.handleError(new Exception("Error while updating the game " + e.getMessage()));
            controller.setLauncherState(MainControllerV2.State.IDLE);
            return;
        }

        LOGGER.info("finished update");
        Platform.runLater(onUpdateFinished);
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
