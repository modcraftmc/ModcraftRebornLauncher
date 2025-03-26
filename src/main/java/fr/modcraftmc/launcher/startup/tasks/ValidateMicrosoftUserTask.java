package fr.modcraftmc.launcher.startup.tasks;

import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.Utils;
import fr.modcraftmc.launcher.startup.IStartupTask;
import fr.modcraftmc.launcher.startup.StartupTasksManager;
import fr.modcraftmc.launcher.startup.results.NoopResult;
import fr.modcraftmc.launcher.startup.results.ValidateMicrosoftUserTaskResult;
import fr.modcraftmc.libs.auth.AccountManager;
import javafx.application.Platform;

public class ValidateMicrosoftUserTask implements IStartupTask<NoopResult, ValidateMicrosoftUserTaskResult> {

    @Override
    public ValidateMicrosoftUserTaskResult execute(StartupTasksManager tasksManager, NoopResult unused) {
        ModcraftApplication.LOGGER.info("Validating Microsoft user profile");
        Platform.runLater(() -> tasksManager.getLoadingMessage().setText("Vérification du compte Microsoft..."));
        AccountManager.AuthResult authResult = AccountManager.validate();
        if (authResult.isLoggedIn()) {
            Utils.selfCatchSleep(1500);

            ModcraftApplication.accountManager.setCurrentMCProfile(authResult.getMcProfile());
            return new ValidateMicrosoftUserTaskResult(authResult.getMcProfile());
        }

        // not logged in
        return ValidateMicrosoftUserTaskResult.createError();
    }
}
