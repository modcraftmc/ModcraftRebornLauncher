package fr.modcraftmc.launcher.startup.tasks;


import fr.modcraftmc.launcher.Utils;
import fr.modcraftmc.launcher.startup.RetryableTask;
import fr.modcraftmc.launcher.startup.StartupTasksManager;
import fr.modcraftmc.launcher.startup.results.ValidateMicrosoftUserTaskResult;
import fr.modcraftmc.launcher.startup.results.ValidateModcraftUserTaskResult;
import fr.modcraftmc.libs.api.ModcraftServiceUserProfile;
import javafx.scene.paint.Color;
import net.raphimc.minecraftauth.step.java.StepMCProfile;

public class ValidateModcaftUserTask extends RetryableTask<ValidateMicrosoftUserTaskResult, ValidateModcraftUserTaskResult> {

    @Override
    public ValidateModcraftUserTaskResult execute(StartupTasksManager tasksManager, ValidateMicrosoftUserTaskResult previousResult) {
        String loadMessageText = "Vérification du compte Modcraft... " + this.getFormatedTryCount();
        Utils.ensureFxThread(() -> tasksManager.getLoadingMessage().setText(loadMessageText));

        return execute(previousResult.mcProfile());

    }

    // temp workaround; i didn't thought we still need this logic when login-in
    public static ValidateModcraftUserTaskResult execute(StepMCProfile.MCProfile mcProfile) {
        try {
            ModcraftServiceUserProfile modcraftUser = ModcraftServiceUserProfile.getProfile(mcProfile.getMcToken().getAccessToken());

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
