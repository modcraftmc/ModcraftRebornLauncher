package fr.modcraftmc.launcher.startup;

import fr.modcraftmc.launcher.startup.results.NoopResult;
import fr.modcraftmc.launcher.startup.tasks.ValidateMicrosoftUserTask;
import fr.modcraftmc.launcher.startup.tasks.ValidateModcaftUserTask;
import fr.modcraftmc.libs.errors.ErrorsHandler;
import javafx.scene.control.Label;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

/**
 * an ordered task list system used in the startup phase of the launcher (popup)
 */
public class StartupTasksManager {

    private final List<IStartupTask> tasks = Lists.newArrayList();
    private Label loadingMessage;

    public void init(Label loadingMessage) {
        this.loadingMessage = loadingMessage;

        this.tasks.add(new ValidateMicrosoftUserTask());
        this.tasks.add(new ValidateModcaftUserTask());
    }

    public ITaskResult execute() {
        ITaskResult previousResult = tasks.removeFirst().execute(this, new NoopResult());

        try {
            for (IStartupTask task : tasks) {
                if (previousResult.hasFailed()) {
                    break;
                }
                ITaskResult finalPreviousResult = previousResult;
                previousResult = task.execute(this, finalPreviousResult);
            }
        } catch (Exception e) {
            ErrorsHandler.handleErrorAndCrashApplication(e);
        }
        return previousResult;
    }

    public Label getLoadingMessage() {
        return loadingMessage;
    }
}
