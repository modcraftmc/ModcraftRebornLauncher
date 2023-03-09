package fr.modcraftmc.libs.updater.phases;

import fr.modcraftmc.libs.update.DownloadUtils;
import fr.modcraftmc.libs.updater.GameUpdater;

import java.io.IOException;

public class FetchData implements IUpdaterPhase {

    @Override
    public boolean isUpToDate() {
        // Always false, always fetch latest data from update server
        return false;
    }

    @Override
    public boolean download() {
        try {
            GlobalPhaseData.manifest = DownloadUtils.getRemoteContent(GameUpdater.get().getUpdateServer() + GameUpdater.MANIFEST_ENDPOINT);
            GlobalPhaseData.ignoreList = DownloadUtils.getIgnoreList(GameUpdater.get().getUpdateServer() + GameUpdater.IGNORELIST_ENDPOINT);
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    @Override
    public String getFriendlyName() {
        return "Connexion au serveur de mise à jour";
    }
}
