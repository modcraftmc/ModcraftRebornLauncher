package fr.modcraftmc.launcher;


import fr.modcraftmc.launcher.configuration.InstanceProperty;
import fr.modcraftmc.launcher.configuration.LauncherConfig;
import fr.modcraftmc.launcher.controllers.MainController;
import fr.modcraftmc.launcher.logger.LogManager;
import fr.modcraftmc.launcher.resources.FilesManager;
import fr.modcraftmc.launcher.resources.ResourcesManager;
import fr.modcraftmc.libs.auth.AccountManager;
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
    public static String FORGE_VERSION = "36.1.31";
    public static String MC_VERSION    = "1.16.5";
    public static String MCP_VERSION   = "20210115.111550";

    private static Stage window;

    @Override
    public void start(Stage stage) throws Exception {
        LOGGER.info("ModcraftLauncher started.");
        launcherConfig = LauncherConfig.load(filesManager.getOptionsPath());
        if (launcherConfig.getInstanceProperty() == null) launcherConfig.setInstanceProperty(new InstanceProperty(false, ""));
        Utils.loadFxml("login.fxml", false);
        Utils.loadFxml("main.fxml", false);
        Utils.loadFxml("loginmode.fxml", false);
        window = stage;

        stage.setTitle("ModcraftLauncher");
        stage.setWidth(1100);
        stage.setHeight(600);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.getIcons().add(new Image(resourcesManager.getResourceAsStream("favicon.png")));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            launcherConfig.save();
        }));

        if (AccountManager.tryVerify(launcherConfig.getAccesToken()).get()) {
            Scene mainScene = Utils.loadFxml("main.fxml", false);
            ((MainController) mainScene.getUserData()).updateUserInfos(AccountManager.getAuthInfos());
            stage.setScene(mainScene);
        } else {
            stage.setScene(Utils.loadFxml("login.fxml", false));
        }

        stage.show();
        stage.centerOnScreen();
    }

    public static Stage getWindow() {
        return window;
    }
}
