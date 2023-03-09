package fr.modcraftmc.libs.updater.phases;

public interface IUpdaterPhase {

    boolean isUpToDate();

    boolean download();

    String getFriendlyName();
}
