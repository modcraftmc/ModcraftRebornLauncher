package fr.modcraftmc.libs.updater;

import fr.flowarg.flowupdater.download.DownloadList;
import fr.flowarg.flowupdater.download.IProgressCallback;
import fr.flowarg.flowupdater.download.Step;

import java.nio.file.Path;

public class UpdaterProgessCallback implements IProgressCallback {

    private UpdateMessages currentMessage = UpdateMessages.LIBS;
    boolean isDownloading;

    @Override
    public void update(DownloadList.DownloadInfo info) {
        GameUpdater.get().getProgressCallback().onProgressUpdate(currentMessage.getString(isDownloading), info.getDownloadedFiles(), info.getTotalToDownloadFiles());
    }

    @Override
    public void step(Step step) {
        switch (step) {
            case DL_LIBS -> {
                isDownloading = false;
                currentMessage = UpdateMessages.LIBS;
            }
            case DL_ASSETS -> {
                currentMessage = UpdateMessages.ASSETS;
            }
            case MODS -> {
                currentMessage = UpdateMessages.MODS;
            }
            case MOD_LOADER -> {
                currentMessage = UpdateMessages.FORGE;
                GameUpdater.get().getProgressCallback().onProgressUpdate(currentMessage.dlText, -1, -1);
                return;
            }
        }

        GameUpdater.get().getProgressCallback().onProgressUpdate(currentMessage.getString(isDownloading), 0, 0);
    }

    @Override
    public void onFileDownloaded(Path path) {
        isDownloading = true;
    }

    public enum UpdateMessages {
        LIBS("Vérification des libraries", "Téléchargement des libraries"),
        ASSETS("Vérification des assets", "Téléchargement des assets"),
        MODS("Vérification des mods", "Téléchargement des mods"),
        FORGE("Installation de Forge", "Installation de Forge");

        private final String verifText;
        private final String dlText;
        UpdateMessages(String verifText, String dlText) {
            this.verifText = verifText;
            this.dlText = dlText;
        }

        public String getString(boolean isDownloading) {
            return isDownloading ? this.dlText : this.verifText;
        }
    }
}
