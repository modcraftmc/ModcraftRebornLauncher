package fr.modcraftmc.launcher;


import fr.modcraftmc.launcher.configuration.InstanceProperty;
import fr.modcraftmc.launcher.configuration.LauncherConfig;
import fr.modcraftmc.launcher.logger.LogManager;
import fr.modcraftmc.launcher.resources.FilesManager;
import fr.modcraftmc.launcher.resources.ResourcesManager;
import fr.modcraftmc.launcher.startup.StartupTasksManager;
import fr.modcraftmc.libs.auth.AccountManager;
import fr.modcraftmc.libs.discord.DiscordManager;
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

    public static Environment ENVIRONMENT = new Environment(Environment.ENV.DEV, Utils.getOS());
    public static ResourcesManager resourcesManager = new ResourcesManager();
    public static FilesManager filesManager = new FilesManager();
    public static Logger LOGGER;
    public static LauncherConfig launcherConfig;
    public static StartupTasksManager startupTasksManager = new StartupTasksManager();
    public static NewsManager newsManager = new NewsManager();
    public static AccountManager accountManager = new AccountManager();
    public static fr.modcraftmc.api.ModcraftApiClient apiClient = new fr.modcraftmc.api.ModcraftApiClient("https://api.modcraftmc.fr/v1");
    public static DiscordManager discordManager = new DiscordManager();

    //Constants
    public static String BUILD_TIME = "DEV";
    public static String FORGE_VERSION = "43.4.8";
    public static String MC_VERSION = "1.19.2";
    public static String MCP_VERSION = "20220805.130853";
    public static ModcraftApplication app;
    private static Stage window;
    public boolean isFirstLaunch;

    public static void shutdown(int code) {
        LOGGER.info("Houston, we have a shutdown.");
        discordManager.stop();
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

    @Override
    public void start(Stage stage) {
        app = this;
        System.setProperty("prism.lcdtext", "false"); // anti-aliasing thing

        try {
            Attributes attributes = ModcraftApplication.getManifest().getMainAttributes();
            String buildType = attributes.getValue("Build-Type");
            ENVIRONMENT = new Environment(Environment.ENV.valueOf(buildType), Utils.getOS());
            BUILD_TIME = attributes.getValue("Build-Time");
        } catch (Exception e) {
            //huh
        }
        filesManager.init();
        LogManager.init();

        LOGGER = LogManager.createLogger("ModcraftLauncher");
        LOGGER.info("ModcraftLauncher started in " + ENVIRONMENT + ". (" + BUILD_TIME + ")" + "(" + FilesManager.DEFAULT_PATH + ")");
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

        discordManager.setOnLoaded(() -> {
           discordManager.setState("sur le launcher");
        });
        AsyncExecutor.runAsync(() -> discordManager.start());

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

    // if width or height is set to -1, use the last value
    public static void switchScene(int width, int height, Scene scene) {
        // only hide if the window side has changed
        boolean shouldHideAndCenter = (width != -1 & height != -1);
        if (shouldHideAndCenter)
            ModcraftApplication.getWindow().hide();
        ModcraftApplication.getWindow().setWidth(width == -1 ? ModcraftApplication.getWindow().getWidth() : width);
        ModcraftApplication.getWindow().setHeight(height == -1 ? ModcraftApplication.getWindow().getHeight() : height);
        ModcraftApplication.getWindow().setScene(scene);
        ModcraftApplication.getWindow().show();
        if (shouldHideAndCenter)
            ModcraftApplication.getWindow().centerOnScreen();
    }
}
