package fr.modcraftmc.launcher.startup;

//note: task result system need to be improved
public abstract class ITaskResult {

    private final boolean hasFailed;
    private final boolean shouldCrash;
    private final Exception taskException;

    public ITaskResult(boolean hasFailed, boolean shouldCrash, Exception taskException) {
        this.hasFailed = hasFailed;
        this.shouldCrash = shouldCrash;
        this.taskException = taskException;
    }

    public ITaskResult(boolean hasFailed) {
        this(hasFailed, false, null);
    }

    public boolean hasFailed() {
        return hasFailed;
    }

    public boolean shouldCrash() {
        return shouldCrash;
    }

    public Exception getTaskException() {
        return taskException;
    }
}
