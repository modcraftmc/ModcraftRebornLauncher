package fr.modcraftmc.launcher.startup.results;

import fr.modcraftmc.launcher.startup.ITaskResult;
import net.raphimc.minecraftauth.step.java.StepMCProfile;

public final class ValidateMicrosoftUserTaskResult extends ITaskResult {
    private StepMCProfile.MCProfile mcProfile;

    public ValidateMicrosoftUserTaskResult(StepMCProfile.MCProfile mcProfile) {
        super(false);
        this.mcProfile = mcProfile;
    }

    public ValidateMicrosoftUserTaskResult() {
        super(true);
    }

    public static ValidateMicrosoftUserTaskResult createError() {
        return new ValidateMicrosoftUserTaskResult();
    }

    public StepMCProfile.MCProfile mcProfile() {
        return mcProfile;
    }
}
