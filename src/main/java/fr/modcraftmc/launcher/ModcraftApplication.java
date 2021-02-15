package fr.modcraftmc.launcher;


import fr.modcraftmc.launcher.configuration.InstanceProperty;
import fr.modcraftmc.launcher.configuration.LauncherConfig;
import fr.modcraftmc.launcher.logger.LogManager;
import fr.modcraftmc.launcher.resources.FilesManager;
import fr.modcraftmc.launcher.resources.ResourcesManager;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.logging.Logger;

public class ModcraftApplication extends Application {

    public static Logger LOGGER = LogManager.createLogger("Launcher");

    public static ResourcesManager resourcesManager = new ResourcesManager();
    public static FilesManager     filesManager     = new FilesManager();
    public static LauncherConfig   launcherConfig;

    //Constants
    public static String FORGE_VERSION = "36.0.24";
    public static String MC_VERSION    = "1.16.5";
    public static String MCP_VERSION   = "20210115.111550";

    private static Stage window;
    public static Scene mainScene;
    public static Scene settingsScene;


    @Override
    public void start(Stage stage) throws Exception {
        LOGGER.info("ModcraftLauncher started.");
        launcherConfig = LauncherConfig.load(filesManager.getOptionsPath());
        if (launcherConfig.getInstanceProperty() == null) launcherConfig.setInstanceProperty(new InstanceProperty(false, ""));
        settingsScene = new Scene(Utils.loadFxml("settings.fxml"));
        window = stage;

        stage.setTitle("ModcraftLauncher");
        stage.setWidth(1100);
        stage.setHeight(600);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.getIcons().add(new Image(resourcesManager.getResourceAsStream("favicon.png")));

        stage.setScene(new Scene(Utils.loadFxml("login.fxml")));

        stage.show();
        stage.centerOnScreen();


    }

    public static Stage getWindow() {
        return window;
    }
}
