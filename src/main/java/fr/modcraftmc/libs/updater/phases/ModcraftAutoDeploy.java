package fr.modcraftmc.libs.updater.phases;

import fr.modcraftmc.libs.updater.UpdateResult;

public class ModcraftAutoDeploy {

    public static boolean isUpToDate() {
        return false;
    }

    public static UpdateResult download() {
        return UpdateResult.SUCCESS;
    }

    public String getFriendlyName() {
        return "Mise à jour de Modcraft";
    }
}
