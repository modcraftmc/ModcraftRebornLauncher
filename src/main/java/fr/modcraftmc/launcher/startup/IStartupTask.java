package fr.modcraftmc.launcher.startup;

/**
 * Startup task are used in the popup windows.
 *
 * @param <Previous>> previous task type {@link ITaskResult}.
 * @param <Result>> result task type {@link ITaskResult}.
 */
public interface IStartupTask<Previous extends ITaskResult, Result extends ITaskResult> {

    Result execute(StartupTasksManager tasksManager, Previous previousTaskResult);

    /**
     * @return the name of the task
     */
    String getName();
}
