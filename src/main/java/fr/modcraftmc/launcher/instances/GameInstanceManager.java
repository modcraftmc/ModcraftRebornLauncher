package fr.modcraftmc.launcher.instances;

import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.resources.FilesManager;

import java.nio.file.Path;

//TODO: rework this when our api support multi instances.
public class GameInstanceManager {

    private static Path PROD;
    private static Path DEV;


    public void refreshInstances() {
        boolean useCustomInstance = ModcraftApplication.launcherConfig.getInstanceProperty().customInstance();
        Path modcraftRoot = useCustomInstance ? Path.of(ModcraftApplication.launcherConfig.getInstanceProperty().customInstancePath()) : FilesManager.INSTANCES_PATH;

        Path instancesPath = modcraftRoot.resolve("instances");

        PROD = instancesPath.resolve("reborn");
        DEV = instancesPath.resolve("dev");

        ModcraftApplication.LOGGER.info("Reborn instance path: " + PROD.toAbsolutePath());
        ModcraftApplication.LOGGER.info("Dev instance path: " + DEV.toAbsolutePath());
    }

    public Path getActiveInstancePath() {
        if (ModcraftApplication.forceDevApi) {
            return DEV;
        }
        return PROD;
    }
}
