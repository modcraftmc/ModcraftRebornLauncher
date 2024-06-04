package fr.modcraftmc.launcher.startup.tasks;

import fr.modcraftmc.launcher.startup.IStartupTask;
import fr.modcraftmc.launcher.startup.results.NoopResult;
import fr.modcraftmc.launcher.startup.results.ValidateMicrosoftUserTaskResult;

public class ValidateMicrosoftUserTask implements IStartupTask<NoopResult, ValidateMicrosoftUserTaskResult> {

    @Override
    public ValidateMicrosoftUserTaskResult execute(NoopResult unused) {
        return new ValidateMicrosoftUserTaskResult(false, null);
    }
}
