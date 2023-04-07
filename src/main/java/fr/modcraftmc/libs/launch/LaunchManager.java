package fr.modcraftmc.libs.launch;

import fr.flowarg.openlauncherlib.NewForgeVersionDiscriminator;
import fr.flowarg.openlauncherlib.NoFramework;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.resources.FilesManager;
import fr.modcraftmc.libs.auth.AccountManager;
import fr.theshark34.openlauncherlib.JavaUtil;
import fr.theshark34.openlauncherlib.LaunchException;
import fr.theshark34.openlauncherlib.external.ExternalLaunchProfile;
import fr.theshark34.openlauncherlib.external.ExternalLauncher;
import fr.theshark34.openlauncherlib.minecraft.*;
import javafx.application.Platform;

import java.io.File;

public class LaunchManager {

    public static Process launch(File dir) {

        try {
            AuthInfos authInfos = new AuthInfos(AccountManager.getAuthInfos().get().name(), AccountManager.getAuthInfos().get().accessToken() ,AccountManager.getAuthInfos().get().uuid(), AccountManager.getAuthInfos().get().xuid(), AccountManager.getAuthInfos().get().clientId());
            final NoFramework noFramework = new NoFramework(dir.toPath(), authInfos, GameFolder.FLOW_UPDATER_1_19_SUP);
            if (System.getProperty("os.name").contains("windows")) {
                JavaUtil.setJavaCommand(new File(FilesManager.JAVA_PATH, "\\bin\\java.exe").getPath());
            } else {
                JavaUtil.setJavaCommand(new File(FilesManager.JAVA_PATH, "/bin/java").getPath());
            }
            noFramework.getAdditionalVmArgs().add((String.format("-Xmx%sG", ModcraftApplication.launcherConfig.getRam())));
            Process process = noFramework.launch(ModcraftApplication.MC_VERSION, ModcraftApplication.FORGE_VERSION, NoFramework.ModLoader.FORGE);
            return process;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
