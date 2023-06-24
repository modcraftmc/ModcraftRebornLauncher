package fr.modcraftmc.libs.launch;

import fr.flowarg.openlauncherlib.NoFramework;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.resources.FilesManager;
import fr.modcraftmc.libs.errors.ErrorsHandler;
import fr.theshark34.openlauncherlib.JavaUtil;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;
import net.raphimc.mcauth.step.java.StepMCProfile;

import java.io.File;

public class LaunchManager {

    public static Process launch(File dir, StepMCProfile.MCProfile currentProfile) {

        String name = currentProfile.name();
        String accesToken = currentProfile.prevResult().prevResult().access_token();
        String uuid = currentProfile.id().toString();
        String xuid = currentProfile.prevResult().prevResult().prevResult().token();
        String clientId = currentProfile.prevResult().prevResult().prevResult().userHash();

        try {
            AuthInfos authInfos = new AuthInfos(name, accesToken, uuid, xuid, clientId);
            //AuthInfos authInfos = new AuthInfos(AccountManager.getAuthInfos().get().name(), AccountManager.getAuthInfos().get().accessToken() ,AccountManager.getAuthInfos().get().uuid(), AccountManager.getAuthInfos().get().xuid(), AccountManager.getAuthInfos().get().clientId());
            final NoFramework noFramework = new NoFramework(dir.toPath(), authInfos, GameFolder.FLOW_UPDATER_1_19_SUP);
            if (System.getProperty("os.name").contains("windows")) {
                JavaUtil.setJavaCommand(new File(FilesManager.JAVA_PATH, "\\bin\\java.exe").getPath());
            } else {
                JavaUtil.setJavaCommand(new File(FilesManager.JAVA_PATH, "/bin/java").getPath());
            }
            ModcraftApplication.LOGGER.info("launching");
            noFramework.getAdditionalVmArgs().add((String.format("-Xmx%sG", ModcraftApplication.launcherConfig.getRam())));
            return noFramework.launch(ModcraftApplication.MC_VERSION, ModcraftApplication.FORGE_VERSION, NoFramework.ModLoader.FORGE);
        } catch (Exception e) {
            ErrorsHandler.handleError(e);
        }
        return null;
    }
}
