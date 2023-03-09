package fr.modcraftmc.libs.updater.phases;

public class GameDownload implements IUpdaterPhase {

    @Override
    public boolean isUpToDate() {
        return false;
    }

    @Override
    public boolean download() {
        return false;
    }

    @Override
    public String getFriendlyName() {
        return "Mise à jour du jeu";
    }
}
