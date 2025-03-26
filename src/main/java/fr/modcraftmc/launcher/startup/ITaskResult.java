package fr.modcraftmc.launcher.startup;

public abstract class ITaskResult {

    private boolean hasFailed;
    private boolean shouldCrash;

    public ITaskResult(boolean hasFailed) {
        this.hasFailed = hasFailed;
    }

    public ITaskResult(boolean hasFailed, boolean shouldCrash) {
        this.hasFailed = hasFailed;
        this.shouldCrash = shouldCrash;
    }

    public boolean hasFailed() {
        return hasFailed;
    }

    public boolean shouldCrash() {
        return shouldCrash;
    }
}
