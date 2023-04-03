package fr.modcraftmc.libs.updater.phases;

import fr.modcraftmc.libs.updater.UpdateResult;

public interface IUpdaterPhase {

    boolean isUpToDate();

    UpdateResult download();

    String getFriendlyName();
}
