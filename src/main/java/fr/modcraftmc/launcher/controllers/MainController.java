package fr.modcraftmc.launcher.controllers;

import com.jfoenix.controls.JFXButton;
import fr.flowarg.flowlogger.ILogger;
import fr.flowarg.flowupdater.download.IProgressCallback;
import fr.flowarg.flowupdater.download.Step;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.Utils;
import fr.modcraftmc.launcher.configuration.InstanceProperty;
import fr.modcraftmc.launcher.resources.FilesManager;
import fr.modcraftmc.libs.auth.AccountManager;
import fr.modcraftmc.libs.launch.LaunchManager;
import fr.modcraftmc.libs.serverpinger.MinecraftPing;
import fr.modcraftmc.libs.serverpinger.MinecraftPingOptions;
import fr.modcraftmc.libs.serverpinger.MinecraftPingReply;
import fr.modcraftmc.libs.update.GameUpdater;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;


public class MainController implements IController {

    @FXML public Label playername;
    @FXML public ImageView playerhead;
    @FXML public JFXButton logout;

    @FXML public JFXButton settings;
    @FXML public JFXButton play;
    @FXML public Button closeBtn;
    @FXML public Rectangle hideBtn;

    @FXML public Label serverstate;
    @FXML public Label playerslabel;

    @FXML public Label label;
    @FXML public ProgressBar progress;

    public Process launchProcess;
    public boolean isLaunched = false;
    @Override
    public void initialize() {

        AuthInfos authInfos = AccountManager.getAuthInfos();
        playername.setText(authInfos.getUsername());
        try {

            Image image = new Image(new URL("https://minotar.net/avatar/" + authInfos.getUsername()).openStream(), 64, 64, false, false);
            playerhead.setImage(image);

        } catch (IOException e) {
            e.printStackTrace();
        }


        logout.setText("SE DÉCONNECTER");
        settings.setText("PARAMÈTRES");
        play.setText("JOUER");
        serverstate.setText("Ouvert");
        label.setText("");
        progress.setVisible(false);

        playerslabel.setText("Na/Na");

        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                try {
                    MinecraftPingReply data = new MinecraftPing().getPing(new MinecraftPingOptions().setHostname("vps.modcraftmc.fr").setPort(25565));

                    Platform.runLater(() -> playerslabel.setText(String.format("%s/%s joueurs", data.getPlayers().getOnline(), data.getPlayers().getMax())));
                } catch (IOException e) {
                }

            }
        }, 0, 30000);


        play.setOnMouseClicked((event -> new Thread(() -> {
            progress.setVisible(true);

            if (!isLaunched) {
                progress.setVisible(true);

                InstanceProperty instanceProperty = ModcraftApplication.launcherConfig.getInstanceProperty();

                File path = instanceProperty.isCustomInstance() ? new File(instanceProperty.getCustomInstancePath()) : new File(FilesManager.INSTANCES_PATH, "v3files");
                path.mkdirs();

                GameUpdater gameUpdater = new GameUpdater("http://update.modcraftmc.fr:100", path, progress, label);
                Task task = gameUpdater.getUpdater();

                task.setOnSucceeded(e -> {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }

                    launchProcess = LaunchManager.launch(path);
                    if (!ModcraftApplication.launcherConfig.isKeepOpen()) System.exit(0);

                });

                try {
                    gameUpdater.start().join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Platform.runLater(() -> {
                    label.setText("en cours");
                    play.setText("Arrêter");
                });

            } else {

                if (launchProcess != null) launchProcess.destroy();

                Platform.runLater(() -> {
                    play.setText("JOUER");
                    progress.setVisible(false);
                    label.setText("");
                });
            }

            /*
            if (launchProcess == null || !launchProcess.isAlive()) {
                progress.setVisible(true);
                InstanceProperty instanceProperty = ModcraftApplication.launcherConfig.getInstanceProperty();

                File path = instanceProperty.isCustomInstance() ? new File(instanceProperty.getCustomInstancePath()) : new File(FilesManager.INSTANCES_PATH, "v3files");
                path.mkdirs();
                GameUpdater gameUpdater = new GameUpdater("http://update.modcraftmc.fr:100", path, progress, label);
                Task task = gameUpdater.getUpdater();
                task.setOnSucceeded((e) -> {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                    launchProcess = LaunchManager.launch(path);

                    if (! ModcraftApplication.launcherConfig.isKeepOpen()) System.exit(0);
                });
                try {
                    gameUpdater.start().join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    label.setText("en cours");
                    play.setText("Arrêter");
                });
            } else {
                launchProcess.destroy();
                Platform.runLater(() -> {
                    play.setText("JOUER");
                    progress.setVisible(false);
                    label.setText("");
                });
            }

             */
        }).start()));


        logout.setOnMouseClicked(event -> {
            ModcraftApplication.launcherConfig.setKeeplogin(false);
            ModcraftApplication.getWindow().setScene(new Scene(Utils.loadFxml("login.fxml")));
        });
        SettingsController.instance.setup(authInfos);

        settings.setOnMouseClicked(event -> {
            ModcraftApplication.getWindow().setScene(ModcraftApplication.settingsScene);
        });

        //window action
        closeBtn.setOnMouseClicked(event -> {
            ModcraftApplication.getWindow().hide();
            ModcraftApplication.launcherConfig.save();
            System.exit(0);
        });

        hideBtn.setOnMouseClicked(event ->  {
            ModcraftApplication.getWindow().setIconified(true);
            ModcraftApplication.launcherConfig.save();
        });

    }
}
