package fr.modcraftmc.launcher.startup.tasks;


import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.startup.IStartupTask;
import fr.modcraftmc.launcher.startup.StartupTasksManager;
import fr.modcraftmc.launcher.startup.results.ValidateMicrosoftUserTaskResult;
import fr.modcraftmc.launcher.startup.results.ValidateModcraftUserTaskResult;
import fr.modcraftmc.libs.api.ModcraftServiceUserProfile;
import fr.modcraftmc.libs.errors.ErrorsHandler;
import javafx.application.Platform;
import javafx.scene.paint.Color;

public class ValidateModcaftUserTask implements IStartupTask<ValidateMicrosoftUserTaskResult, ValidateModcraftUserTaskResult> {

    @Override
    public ValidateModcraftUserTaskResult execute(StartupTasksManager tasksManager, ValidateMicrosoftUserTaskResult previousResult) {
        ModcraftApplication.LOGGER.info("Validating Microsoft user profile");
        Platform.runLater(() -> tasksManager.getLoadingMessage().setText("Vérification du compte Modcraft..."));
        try {
            ModcraftServiceUserProfile modcraftUser = ModcraftServiceUserProfile.getProfile(previousResult.mcProfile().getMcToken().getAccessToken());

            String rank = modcraftUser.info.role().name();
            String finalText = "Joueur";
            Color finalColor = Color.rgb(255, 255, 255);
            if (rank.equalsIgnoreCase("administrateur")) {
                finalText = "Administrateur";
                finalColor = Color.rgb(255, 0, 0);
            } else if (!rank.equals("default")) {
                finalText = rank;
            }

            return new ValidateModcraftUserTaskResult(modcraftUser, new ValidateModcraftUserTaskResult.PlayerRankInfos(finalText, finalColor));

        } catch (Exception e) {
            ErrorsHandler.handleErrorWithCustomHeaderAndCrashApplication("Imposible de contacter notre API.", e);
            return ValidateModcraftUserTaskResult.createError();
        }
    }
}
