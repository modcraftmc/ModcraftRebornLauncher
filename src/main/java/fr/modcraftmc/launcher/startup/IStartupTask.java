package fr.modcraftmc.launcher.startup;

/**
 * Startup task are used in the popup windows.
 *
 * @param <P> previous task type {@link ITaskResult}.
 * @param <R> result task type {@link ITaskResult}.
 */
public interface IStartupTask<P extends ITaskResult, R extends ITaskResult> {

    R execute(P previousTaskResult);
}
