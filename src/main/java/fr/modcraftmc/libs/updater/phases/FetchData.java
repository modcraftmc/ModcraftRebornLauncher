package fr.modcraftmc.libs.updater.phases;

import fr.modcraftmc.libs.update.DownloadUtils;
import fr.modcraftmc.libs.updater.GameUpdater;
import fr.modcraftmc.libs.updater.UpdateResult;

import java.io.IOException;

public class FetchData {

    public static UpdateResult run() {
        try {
            GlobalPhaseData.manifest = DownloadUtils.getRemoteContent(GameUpdater.get().getUpdateServer() + GameUpdater.MANIFEST_ENDPOINT);
            GlobalPhaseData.ignoreList = DownloadUtils.getIgnoreList(GameUpdater.get().getUpdateServer() + GameUpdater.IGNORELIST_ENDPOINT);
            GlobalPhaseData.autoDeployList = DownloadUtils.getIgnoreList(GameUpdater.get().getUpdateServer() + GameUpdater.IGNORELIST_ENDPOINT);
        } catch (IOException e) {
            return UpdateResult.FAILURE;
        }

        return UpdateResult.SUCCESS;
    }
}
