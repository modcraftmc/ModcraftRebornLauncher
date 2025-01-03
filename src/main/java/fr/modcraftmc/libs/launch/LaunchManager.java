package fr.modcraftmc.libs.launch;

import fr.flowarg.openlauncherlib.NoFramework;
import fr.modcraftmc.launcher.Environment;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.resources.FilesManager;
import fr.modcraftmc.libs.auth.AccountManager;
import fr.theshark34.openlauncherlib.JavaUtil;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;
import net.raphimc.minecraftauth.step.java.StepMCProfile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

public class LaunchManager {

    public static Process launch(File dir) throws Exception {
        StepMCProfile.MCProfile currentProfile = ModcraftApplication.accountManager.getCurrentMCProfile();
        ModcraftApplication.LOGGER.info(currentProfile.getName());
        if (currentProfile.isExpired()) {
            AccountManager.validate(null);
            currentProfile = ModcraftApplication.accountManager.getCurrentMCProfile();
        }

        String name = currentProfile.getName();
        String accesToken = currentProfile.getMcToken().getAccessToken();
        String uuid = currentProfile.getId().toString();
        String xuid = currentProfile.getMcToken().getXblXsts().getToken();
        String clientId = currentProfile.getMcToken().getXblXsts().getUserHash();

        AuthInfos authInfos = new AuthInfos(name, accesToken, uuid, xuid, clientId);
        final NoFramework noFramework = new NoFramework(dir.toPath(), authInfos, GameFolder.FLOW_UPDATER_1_19_SUP);
        if (System.getProperty("os.name").contains("windows")) {
            JavaUtil.setJavaCommand(new File(FilesManager.JAVA_PATH, "\\bin\\java.exe").getPath());
        } else {
            JavaUtil.setJavaCommand(new File(FilesManager.JAVA_PATH, "/bin/java").getPath());
        }
        ModcraftApplication.LOGGER.info("launching");
        noFramework.getAdditionalVmArgs().add((String.format("-Xmx%sG", ModcraftApplication.launcherConfig.getRam())));
        noFramework.getAdditionalArgs().add("-Djava.net.preferIPv4Stack=true -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true");
        return noFramework.launch(ModcraftApplication.MC_VERSION, ModcraftApplication.FORGE_VERSION, NoFramework.ModLoader.FORGE);
    }
}
