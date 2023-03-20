package fr.modcraftmc.libs.updater.phases;

import fr.modcraftmc.libs.update.DownloadUtils;
import fr.modcraftmc.libs.updater.GameUpdater;
import fr.modcraftmc.libs.updater.UpdateResult;

import java.io.IOException;

public class FetchData implements IUpdaterPhase {

    @Override
    public boolean isUpToDate() {
        // Always false, always fetch latest data from update server
        return false;
    }

    @Override
    public UpdateResult download() {
        try {
            GlobalPhaseData.manifest = DownloadUtils.getRemoteContent(GameUpdater.get().getUpdateServer() + GameUpdater.MANIFEST_ENDPOINT);
            GlobalPhaseData.ignoreList = DownloadUtils.getIgnoreList(GameUpdater.get().getUpdateServer() + GameUpdater.IGNORELIST_ENDPOINT);
        } catch (IOException e) {
            return UpdateResult.faillure();
        }

        return UpdateResult.success();
    }

    @Override
    public String getFriendlyName() {
        return "Connexion au serveur de mise à jour";
    }
}
