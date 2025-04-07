package fr.modcraftmc.launcher.startup.results;

import fr.modcraftmc.launcher.startup.ITaskResult;
import fr.modcraftmc.libs.api.ModcraftServiceUserProfile;
import javafx.scene.paint.Color;

public class ValidateModcraftUserTaskResult extends ITaskResult {

    private ModcraftServiceUserProfile serviceUserProfile;

    private PlayerRankInfos playerRankInfos;


    public ValidateModcraftUserTaskResult(ModcraftServiceUserProfile serviceUserProfile, PlayerRankInfos playerRankInfos) {
        super(false);
        this.serviceUserProfile = serviceUserProfile;
        this.playerRankInfos = playerRankInfos;
    }

    public ValidateModcraftUserTaskResult(Exception e) {
        super(true, true, e);
    }

    // Task are responsible for displaying error messages
    public static ValidateModcraftUserTaskResult createError(Exception e) {
        return new ValidateModcraftUserTaskResult(e);
    }

    public ModcraftServiceUserProfile getServiceUserProfile() {
        return serviceUserProfile;
    }

    public PlayerRankInfos getPlayerRankInfos() {
        return playerRankInfos;
    }

    public record PlayerRankInfos(String name, Color color) {
    }
}
