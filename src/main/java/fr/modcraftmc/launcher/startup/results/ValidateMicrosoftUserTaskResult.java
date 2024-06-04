package fr.modcraftmc.launcher.startup.results;

import fr.modcraftmc.launcher.startup.ITaskResult;
import net.raphimc.minecraftauth.step.java.StepMCProfile;

public record ValidateMicrosoftUserTaskResult(boolean isLoggedIn, StepMCProfile.MCProfile mcProfile) implements ITaskResult {
}
