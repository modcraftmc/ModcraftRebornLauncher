package fr.modcraftmc.libs.updater;

import fr.flowarg.flowupdater.download.DownloadList;
import fr.flowarg.flowupdater.download.IProgressCallback;
import fr.flowarg.flowupdater.download.Step;

import java.nio.file.Path;

public class UpdaterProgessCallback implements IProgressCallback {
    String updateText = "Recherche de mise à jour";
    @Override
    public void update(DownloadList.DownloadInfo info) {
        GameUpdater.get().getProgressCallback().onProgressUpdate(updateText, info.getDownloadedFiles(), info.getTotalToDownloadFiles());
    }

    @Override
    public void step(Step step) {
        switch (step) {
            case DL_LIBS -> updateText = "Téléchargement des libraries";
            case DL_ASSETS -> updateText = "Téléchargement des assets";
            case MODS -> updateText = "Téléchargement des mods";
            case MOD_LOADER -> {
                updateText = "Installation de Forge";
                GameUpdater.get().getProgressCallback().onProgressUpdate(updateText, -1, -1);
                return;
            }
        }
        GameUpdater.get().getProgressCallback().onProgressUpdate(updateText, 0, 0);
    }

    @Override
    public void onFileDownloaded(Path path) {
    }
}
