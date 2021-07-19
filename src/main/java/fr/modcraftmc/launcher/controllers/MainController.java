package fr.modcraftmc.launcher.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXSlider;
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
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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

    @FXML public Pane leftpane;

    @FXML public Pane news;
    @FXML public JFXButton news1Btn;
    @FXML public JFXButton news2Btn;

    private boolean showSettings = false;

    @FXML public JFXSlider ramSlider;
    @FXML public Label ramText;

    @FXML public JFXCheckBox customPathCheckbox;
    @FXML public TextField customPathValue;
    @FXML public JFXButton fidnBtn;
    @FXML public JFXCheckBox keepOpen;

    private Process launchProcess;
    private boolean isLaunched = false;

    public void updateUserInfos(AuthInfos authInfos) {
        playername.setText(authInfos.getUsername());
        try {

            Image image = new Image(new URL("https://minotar.net/avatar/" + authInfos.getUsername()).openStream(), 64, 64, false, false);
            playerhead.setImage(image);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize() {
        logout.setText("SE DÉCONNECTER");
        settings.setText("PARAMÈTRES");
        play.setText("JOUER");
        serverstate.setText("Ouvert");
        label.setText("");
        progress.setVisible(false);
        playerslabel.setText("Na/Na");

        //TODO: handle this with a news manager
        news1Btn.setOnMouseReleased(event -> {
            new Thread(() -> {
                try {
                    Desktop.getDesktop().browse(new URI("https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }).start();
        });

        news2Btn.setOnMouseReleased(event -> {
            new Thread(() -> {
                try {
                    Desktop.getDesktop().browse(new URI("https://www.youtube.com/watch?v=dQw4w9WgXcQ"));
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }
            }).start();
        });

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    MinecraftPingReply data = new MinecraftPing().getPing(new MinecraftPingOptions().setHostname("v4.modcraftmc.fr").setPort(25565));
                    Platform.runLater(() -> playerslabel.setText(String.format("%s/%s joueurs", data.getPlayers().getOnline(), data.getPlayers().getMax())));
                } catch (IOException e) {
                }
            }
        }, 0, 60000);

        play.setOnMouseClicked((event -> new Thread(() -> {
            progress.setVisible(true);

            if (!isLaunched) {
                InstanceProperty instanceProperty = ModcraftApplication.launcherConfig.getInstanceProperty();

                File path = instanceProperty.isCustomInstance() ? new File(instanceProperty.getCustomInstancePath()) : new File(FilesManager.INSTANCES_PATH, "v4-survival");
                path.mkdirs();

                GameUpdater gameUpdater = new GameUpdater("https://files.modcraftmc.fr", path, progress, label);
                Task<Void> task = gameUpdater.getUpdater();

                task.setOnSucceeded(e -> {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }

                    //launchProcess = LaunchManager.launch(path);
                    isLaunched = true;
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
                isLaunched = false;

                Platform.runLater(() -> {
                    play.setText("JOUER");
                    progress.setVisible(false);
                    label.setText("");
                });
            }
        }).start()));

        ramSlider.setMin(4);
        ramSlider.setMax(16);
        ramSlider.setValue(ModcraftApplication.launcherConfig.getRam());
        ramText.setText(String.format("Ram: %sGb", ((int)Math.floor(ramSlider.getValue()))));

        customPathCheckbox.setSelected(ModcraftApplication.launcherConfig.getInstanceProperty().isCustomInstance());
        customPathValue.setText(ModcraftApplication.launcherConfig.getInstanceProperty().getCustomInstancePath());


        ramSlider.setOnMouseDragged(event -> {
            ramText.setText("Ram: " + ((int)Math.floor(ramSlider.getValue()+0.5)));
            ModcraftApplication.launcherConfig.setRam(((int)Math.floor(ramSlider.getValue()+0.5)));
        });

        customPathCheckbox.setOnMouseClicked(event -> {
            if (!customPathCheckbox.isSelected()) {
                customPathValue.setText("");
                ModcraftApplication.launcherConfig.setInstanceProperty(new InstanceProperty(false, ""));
            }

        });

        keepOpen.setOnMouseClicked(event -> ModcraftApplication.launcherConfig.setKeepOpen(keepOpen.isSelected()));

        fidnBtn.setOnMouseClicked(event -> {
            DirectoryChooser fileChooser = new DirectoryChooser();
            fileChooser.setTitle("Select directory");
            fileChooser.setInitialDirectory(FilesManager.INSTANCES_PATH);
            File path = fileChooser.showDialog(ModcraftApplication.getWindow());
            if (path != null) {
                customPathValue.setText(path.getAbsolutePath());
                ModcraftApplication.launcherConfig.setInstanceProperty(new InstanceProperty(true, customPathValue.getText()));
            }
        });


        logout.setOnMouseClicked(event -> {
            ModcraftApplication.launcherConfig.setKeeplogin(false);
            ModcraftApplication.getWindow().setScene(Utils.loadFxml("login.fxml", true));
        });

        settings.setOnMouseClicked(event -> {
            leftpane.setVisible(showSettings = !showSettings);
            news.setVisible(!showSettings);
            settings.setText(showSettings ? "RETOUR" : "PARAMÈTRES");
        });

        //window action
        closeBtn.setOnMouseClicked(event -> {
            ModcraftApplication.getWindow().hide();
            System.exit(0);
        });

        hideBtn.setOnMouseClicked(event ->  {
            ModcraftApplication.getWindow().setIconified(true);
            ModcraftApplication.launcherConfig.save();
        });
    }
}
