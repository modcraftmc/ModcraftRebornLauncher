package fr.modcraftmc.libs.updater;

import fr.flowarg.flowupdater.download.DownloadList;
import fr.flowarg.flowupdater.download.IProgressCallback;
import fr.flowarg.flowupdater.download.Step;
import fr.modcraftmc.launcher.logger.LogManager;

import java.nio.file.Path;
import java.util.logging.Logger;

public class UpdaterProgessCallback implements IProgressCallback {

    public static Logger LOGGER = LogManager.createLogger("Updater");

    String updateText = "";
    @Override
    public void update(DownloadList.DownloadInfo info) {
        GameUpdater.get().getProgressCallback().onProgressUpdate(updateText, info.getDownloadedFiles(), info.getTotalToDownloadFiles());
    }

    @Override
    public void step(Step step) {
        LOGGER.info(step.name());
        switch (step) {
            case DL_LIBS -> updateText = "Téléchargement des libraries";
            case DL_ASSETS -> updateText = "Téléchargement des assets";
            case MODS -> updateText = "Téléchargement des mods";
            case MOD_LOADER -> updateText = "Installation de Forge";
        }
        GameUpdater.get().getProgressCallback().onProgressUpdate(updateText, 0, 0);
    }

    @Override
    public void onFileDownloaded(Path path) {
        //LOGGER.info("File downloaded " + path);
    }
}
