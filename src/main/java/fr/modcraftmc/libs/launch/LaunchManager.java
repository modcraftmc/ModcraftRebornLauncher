package fr.modcraftmc.libs.launch;

import fr.flowarg.openlauncherlib.NewForgeVersionDiscriminator;
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
            NewForgeVersionDiscriminator forgeVersionDiscriminator = new NewForgeVersionDiscriminator(ModcraftApplication.FORGE_VERSION, ModcraftApplication.MC_VERSION, "net.minecraftforge", ModcraftApplication.MCP_VERSION);
            GameVersion VERSION = new GameVersion(ModcraftApplication.MC_VERSION, GameType.V1_13_HIGER_FORGE.setNewForgeVersionDiscriminator(forgeVersionDiscriminator));
            GameInfos infos = new GameInfos("modcraftmc", dir, VERSION, new GameTweak[] {});

            ExternalLaunchProfile profile = MinecraftLauncher.createExternalProfile(infos, GameFolder.FLOW_UPDATER, AccountManager.getAuthInfos());

            if (System.getProperty("os.name").contains("windows")) {
                JavaUtil.setJavaCommand(new File(FilesManager.JAVA_PATH, "\\bin\\java").getPath());
            } else {
                JavaUtil.setJavaCommand(new File(FilesManager.JAVA_PATH, "/bin/java").getPath());
            }
            profile.getVmArgs().add(String.format("-Xmx%sG", ModcraftApplication.launcherConfig.getRam()));
            ExternalLauncher launcher = new ExternalLauncher(profile);

            Process process = launcher.launch();

            return process;
        } catch (LaunchException e) {
            e.printStackTrace();
        }
        return null;
    }
}
