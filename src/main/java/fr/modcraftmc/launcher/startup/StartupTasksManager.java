package fr.modcraftmc.launcher.startup;

import fr.modcraftmc.launcher.AsyncExecutor;
import fr.modcraftmc.launcher.startup.results.NoopResult;
import fr.modcraftmc.launcher.startup.tasks.ValidateMicrosoftUserTask;
import fr.modcraftmc.launcher.startup.tasks.ValidateModcaftUserTask;
import fr.modcraftmc.libs.errors.ErrorsHandler;
import javafx.scene.control.Label;

import java.util.Arrays;
import java.util.List;

/**
 * an ordered task list system used in the startup phase of the launcher (popup)
 */
public class StartupTasksManager {

    private Label loadingMessage;
    private List<IStartupTask> tasks = Arrays.asList(new ValidateMicrosoftUserTask(), new ValidateModcaftUserTask());

    public void init(Label loadingMessage) {
        this.loadingMessage = loadingMessage;

    }

    public void execute() {
        ITaskResult previousResult = tasks.remove(0).execute(new NoopResult());

        try {
            for (IStartupTask task : tasks) {
                ITaskResult finalPreviousResult = previousResult;
                previousResult = (ITaskResult) AsyncExecutor.submitAsync(() -> task.execute(finalPreviousResult)).get();
            }
        } catch (Exception e) {
            ErrorsHandler.handleErrorAndCrashApplication(e);
        }
    }
}
