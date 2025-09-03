package fr.modcraftmc.libs.updater;

public interface ProgressCallback {

    void onProgressUpdate(UpdaterProgessCallback.UpdateMessages state, String progress, int current, int max);
}
