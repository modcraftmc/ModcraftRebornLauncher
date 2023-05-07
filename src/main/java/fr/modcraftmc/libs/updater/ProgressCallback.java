package fr.modcraftmc.libs.updater;

public interface ProgressCallback {

    void onProgressUpdate(String progress, int current, int max);
}
