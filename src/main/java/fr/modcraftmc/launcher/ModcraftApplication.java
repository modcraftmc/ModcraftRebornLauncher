package fr.modcraftmc.launcher;


import fr.modcraftmc.launcher.configuration.InstanceProperty;
import fr.modcraftmc.launcher.configuration.LauncherConfig;
import fr.modcraftmc.launcher.logger.LogManager;
import fr.modcraftmc.launcher.resources.FilesManager;
import fr.modcraftmc.launcher.resources.ResourcesManager;
import fr.modcraftmc.launcher.startup.StartupTasksManager;
import fr.modcraftmc.libs.news.NewsManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Logger;

public class ModcraftApplication extends Application {

    public static Environment.ENV     ENVIRONMENT = Environment.ENV.DEV;
    public static ResourcesManager    resourcesManager = new ResourcesManager();
    public static FilesManager        filesManager     = new FilesManager();
    public static Logger              LOGGER           = LogManager.createLogger("ModcraftLauncher");
    public static LauncherConfig      launcherConfig;
    public static StartupTasksManager startupTasksManager = new StartupTasksManager();
    public static NewsManager         newsManager = new NewsManager();
    public static fr.modcraftmc.api.ModcraftApiClient apiClient = new fr.modcraftmc.api.ModcraftApiClient("https://api.modcraftmc.fr/v1");

    //Constants
    public static String BUILD_TIME    = "DEV";
    public static String FORGE_VERSION = "43.3.12";
    public static String MC_VERSION    = "1.19.2";
    public static String MCP_VERSION   = "20220805.130853";
    private static Stage window;
    public static ModcraftApplication app;
    public boolean isFirstLaunch;

    @Override
    public void start(Stage stage) {
        app = this;
        System.setProperty("prism.lcdtext", "false"); // anti-aliasing thing

        try {
            Attributes attributes = ModcraftApplication.getManifest().getMainAttributes();
            String buildType = attributes.getValue("Build-Type");
            ENVIRONMENT = Environment.ENV.valueOf(buildType);
            BUILD_TIME = attributes.getValue("Build-Time");
        } catch (Exception e) {
            //huh
        }

        LOGGER.info("ModcraftLauncher started in " + ENVIRONMENT + " environment. (" + BUILD_TIME + ")");
        launcherConfig = LauncherConfig.load(filesManager.getOptionsPath());
        if (launcherConfig.getInstanceProperty() == null) {
            launcherConfig.setInstanceProperty(new InstanceProperty(false, ""));
            isFirstLaunch = true;
        }
        window = stage;
        // need to preload font
        Font font = Font.loadFont(resourcesManager.getResourceAsStream("fonts/LilitaOne-Regular.ttf"), 32);

        stage.setTitle("ModcraftLauncher");
        stage.setWidth(700);
        stage.setHeight(400);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.getIcons().add(new Image(resourcesManager.getResourceAsStream("favicon.png")));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LogManager.getFileHandler().flush();
            LogManager.getFileHandler().close();
            launcherConfig.save();
        }));

        Scene scene = MFXMLLoader.loadFxml("login.fxml", false);
        Scene mainScene = MFXMLLoader.loadFxml("loader.fxml", false);
        stage.setScene(mainScene);
        stage.show();
        stage.centerOnScreen();
    }

    public static void shutdown(int code) {
        LOGGER.info("Houston, we have a shutdown.");
        LogManager.getFileHandler().flush();
        LogManager.getFileHandler().close();
        launcherConfig.save();
        AsyncExecutor.shutdown();
        Platform.exit();
        System.exit(code);
        Runtime.getRuntime().halt(code);
    }

    public static Stage getWindow() {
        return window;
    }

    public static Manifest getManifest() throws IOException {
        return new Manifest((ModcraftApplication.class.getResourceAsStream("/META-INF/MANIFEST.MF")));
    }
}
