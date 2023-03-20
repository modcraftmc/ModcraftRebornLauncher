package fr.modcraftmc.libs.updater.phases;

import fr.modcraftmc.libs.updater.UpdateResult;

public class ModcraftAutoDeploy implements IUpdaterPhase {

    @Override
    public boolean isUpToDate() {
        return false;
    }

    @Override
    public UpdateResult download() {
        return UpdateResult.success();
    }

    @Override
    public String getFriendlyName() {
        return "Mise à jour de Modcraft";
    }
}
