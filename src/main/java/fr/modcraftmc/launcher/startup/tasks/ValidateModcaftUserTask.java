package fr.modcraftmc.launcher.startup.tasks;


import fr.modcraftmc.launcher.startup.RetryableTask;
import fr.modcraftmc.launcher.startup.StartupTasksManager;
import fr.modcraftmc.launcher.startup.results.ValidateMicrosoftUserTaskResult;
import fr.modcraftmc.launcher.startup.results.ValidateModcraftUserTaskResult;
import fr.modcraftmc.libs.api.ModcraftServiceUserProfile;
import javafx.application.Platform;
import javafx.scene.paint.Color;

public class ValidateModcaftUserTask extends RetryableTask<ValidateMicrosoftUserTaskResult, ValidateModcraftUserTaskResult> {

    @Override
    public ValidateModcraftUserTaskResult execute(StartupTasksManager tasksManager, ValidateMicrosoftUserTaskResult previousResult) {
        String loadMessageText = "Vérification du compte Modcraft... " + this.getFormatedTryCount();
        Platform.runLater(() -> tasksManager.getLoadingMessage().setText(loadMessageText));

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
            return ValidateModcraftUserTaskResult.createError(new Exception("Impossible de contacter notre API." + e));
        }
    }

    @Override
    public String getName() {
        return "Modcraft account validation";
    }

    @Override
    public int maxTryCount() {
        return 3;
    }
}
