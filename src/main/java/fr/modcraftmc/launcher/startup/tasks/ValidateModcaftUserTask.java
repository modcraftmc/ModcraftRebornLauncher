package fr.modcraftmc.launcher.startup.tasks;


import fr.modcraftmc.launcher.startup.IStartupTask;
import fr.modcraftmc.launcher.startup.results.ValidateMicrosoftUserTaskResult;
import fr.modcraftmc.launcher.startup.results.ValidateModcraftUserTaskResult;

public class ValidateModcaftUserTask implements IStartupTask<ValidateMicrosoftUserTaskResult, ValidateModcraftUserTaskResult> {

    @Override
    public ValidateModcraftUserTaskResult execute(ValidateMicrosoftUserTaskResult previousTaskResult) {
        return null;
    }
}
