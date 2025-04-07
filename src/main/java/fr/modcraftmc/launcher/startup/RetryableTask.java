package fr.modcraftmc.launcher.startup;

public abstract class RetryableTask<Previous extends ITaskResult, Result extends ITaskResult> implements IStartupTask<Previous, Result> {

    private int tryCount = 0;

    public abstract int maxTryCount();

    public int getTryCount() {
        return tryCount;
    }

    public void newTry() {
        tryCount++;
    }

    public String getFormatedTryCount() {
        if (tryCount == 1)
            return ""; // do not display trycount is task haven't failed yet
        return String.format("(%s / %s)", this.getTryCount(), this.maxTryCount());
    }
}
