package fr.modcraftmc.launcher.startup;

import fr.modcraftmc.launcher.logger.LogManager;
import fr.modcraftmc.launcher.startup.results.NoopResult;
import fr.modcraftmc.launcher.startup.tasks.ValidateMicrosoftUserTask;
import fr.modcraftmc.launcher.startup.tasks.ValidateModcaftUserTask;
import fr.modcraftmc.libs.errors.ErrorsHandler;
import javafx.scene.control.Label;
import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.logging.Logger;

/**
 * an ordered task list system used in the startup phase of the launcher (popup)
 */
public class StartupTasksManager {

    private final Logger LOGGER = LogManager.createLogger("StartupTasksManager");
    private final List<IStartupTask<?, ?>> tasks = Lists.newArrayList();
    private Label loadingMessage;

    public void init(Label loadingMessage) {
        this.loadingMessage = loadingMessage;

        this.tasks.add(new ValidateMicrosoftUserTask());
        this.tasks.add(new ValidateModcaftUserTask());
    }

    public ITaskResult execute() {
        ITaskResult previousResult = new NoopResult();
        ITaskResult lastSuccessfulResult = previousResult;

        try {
            for (IStartupTask<?, ?> task : tasks) {
                boolean taskSuccess = false;

                while (!taskSuccess) {
                    if (task instanceof RetryableTask<?,?> retryableTask) {
                        if (retryableTask.getTryCount() >= retryableTask.maxTryCount())
                            throw previousResult.getTaskException();

                        retryableTask.newTry();
                    }

                    previousResult = this.execute(task, previousResult.hasFailed() ? lastSuccessfulResult : previousResult);

                    if (previousResult.hasFailed()) {
                        LOGGER.info("task " + task.getClass().getSimpleName() + " failed");
                        continue;
                    }

                    lastSuccessfulResult = previousResult;
                    taskSuccess = true;
                }

            }
        } catch (Exception e) {
            if (previousResult.shouldCrash()) {
                ErrorsHandler.handleErrorAndCrashApplication(e);
            }

        }
        return previousResult;
    }

    public ITaskResult execute(IStartupTask task, ITaskResult previousResult) {
        LOGGER.info("running task " + task.getName());
        return task.execute(this, previousResult);
    }

    public Label getLoadingMessage() {
        return loadingMessage;
    }
}
