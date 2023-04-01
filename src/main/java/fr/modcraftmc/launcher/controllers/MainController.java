package fr.modcraftmc.launcher.controllers;

import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.Utils;
import fr.modcraftmc.launcher.configuration.InstanceProperty;
import fr.modcraftmc.launcher.resources.FilesManager;
import fr.modcraftmc.libs.serverpinger.MinecraftPing;
import fr.modcraftmc.libs.serverpinger.MinecraftPingOptions;
import fr.modcraftmc.libs.serverpinger.MinecraftPingReply;
import fr.modcraftmc.libs.updater.GameUpdater;
import fr.modcraftmc.libs.updater.ProgressCallback;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import net.hycrafthd.minecraft_authenticator.login.User;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;


public class MainController implements IController, ProgressCallback {

    //Window
    @FXML public Button close;
    @FXML public Button minimize;

    @FXML public Button play;

    //Account
    @FXML public Label playerName;
    @FXML public ImageView playerHead;
//    @FXML public Button logout;

    //Server state
    @FXML public Label serverState;
    @FXML public Label playersCount;

//    //Progress bar
//    @FXML public Label progessLabel;
//    @FXML public ProgressBar progress;

    //News
    @FXML public Pane news;

    //Settings
    @FXML public Button settings;
    private boolean showSettings = false;

//    @FXML public MFXSlider ramSlider;
//    @FXML public Label ramText;
//
//    @FXML public MFXCheckbox customPathCheckbox;
//    @FXML public TextField customPathValue;
//    @FXML public Button findBtn;
//    @FXML public MFXCheckbox keepOpen;

    private Process launchProcess;
    private boolean isUpdateLaunched = false;

    public void updateUserInfos(User authInfos) {
        playerName.setText(authInfos.name());
        try {

            Image image = new Image(new URL("https://minotar.net/avatar/" + authInfos.name()).openStream(), 64, 64, false, false);
            playerHead.setImage(image);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize() {
        playersCount.setText("Na/Na");

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    MinecraftPingReply data = new MinecraftPing().getPing(new MinecraftPingOptions().setHostname("v4.modcraftmc.fr").setPort(25565));
                    Platform.runLater(() -> playersCount.setText(String.format("%s/%s joueurs", data.getPlayers().getOnline(), data.getPlayers().getMax())));
                } catch (IOException e) {
                }
            }
        }, 0, 60000);

        play.setOnMouseClicked(event -> {

            InstanceProperty instanceProperty = ModcraftApplication.launcherConfig.getInstanceProperty();
            File instanceDirectory = instanceProperty.isCustomInstance() ? new File(instanceProperty.getCustomInstancePath()) : new File(FilesManager.INSTANCES_PATH, "v4-staff");
            if (!instanceDirectory.exists()) instanceDirectory.mkdirs();
            GameUpdater gameUpdater = new GameUpdater("", instanceDirectory.toPath(), this);

//            if (!isUpdateLaunched) {
//                isUpdateLaunched = true;
//                Platform.runLater(() -> {
//                    label.setVisible(true);
//                    play.setText("Arrêter");
//                    progress.setVisible(true);
//                });
//
//                InstanceProperty instanceProperty = ModcraftApplication.launcherConfig.getInstanceProperty();
//                File path = instanceProperty.isCustomInstance() ? new File(instanceProperty.getCustomInstancePath()) : new File(FilesManager.INSTANCES_PATH, "v4-staff");
//                if (!path.exists()) path.mkdirs();
//
//                GameUpdater gameUpdater = new GameUpdater("https://files.modcraftmc.fr", path, progress, label);
//
//                Task<Void> updateTask = gameUpdater.getUpdater();
//
//                updateTask.setOnSucceeded(onSuccess -> {
//                    launchProcess = LaunchManager.launch(path);
//                    Platform.runLater(() -> {
//                        progress.progressProperty().unbind();
//                        progress.setVisible(false);
//                        label.setVisible(false);
//                    });
//                    if (!ModcraftApplication.launcherConfig.isKeepOpen()) System.exit(0);
//                });
//
//                gameUpdater.start();
//
//            } else {
//                isUpdateLaunched = false;
//
//                if (launchProcess != null) {
//                    launchProcess.destroy();
//                    launchProcess = null;
//                    Platform.runLater(() -> {
//                        label.setText("");
//                        play.setText("JOUER");
//                        progress.setVisible(false);
//                    });
//                }
//            }
        });

        //#region settings
//        ramSlider.setMin(4);
//        ramSlider.setMax(16);
//        ramSlider.setValue(ModcraftApplication.launcherConfig.getRam());
//        ramText.setText(String.format("Ram: %sGb", ((int)Math.floor(ramSlider.getValue()))));
//        keepOpen.setSelected(ModcraftApplication.launcherConfig.isKeepOpen());
//        customPathCheckbox.setSelected(ModcraftApplication.launcherConfig.getInstanceProperty().isCustomInstance());
//        customPathValue.setText(ModcraftApplication.launcherConfig.getInstanceProperty().getCustomInstancePath());
//
//
//        ramSlider.setOnMouseDragged(event -> {
//            ramText.setText("Ram: " + ((int)Math.floor(ramSlider.getValue()+0.5)));
//            ModcraftApplication.launcherConfig.setRam(((int)Math.floor(ramSlider.getValue()+0.5)));
//        });
//
//        customPathCheckbox.setOnMouseClicked(event -> {
//            if (!customPathCheckbox.isSelected()) {
//                customPathValue.setText("");
//                ModcraftApplication.launcherConfig.setInstanceProperty(new InstanceProperty(false, ""));
//            }
//
//        });
//
//        keepOpen.setOnMouseClicked(event -> ModcraftApplication.launcherConfig.setKeepOpen(keepOpen.isSelected()));
//
//        findBtn.setOnMouseClicked(event -> {
//            DirectoryChooser fileChooser = new DirectoryChooser();
//            fileChooser.setTitle("Select directory");
//            fileChooser.setInitialDirectory(FilesManager.INSTANCES_PATH);
//            File path = fileChooser.showDialog(ModcraftApplication.getWindow());
//            if (path != null) {
//                customPathValue.setText(path.getAbsolutePath());
//                ModcraftApplication.launcherConfig.setInstanceProperty(new InstanceProperty(true, customPathValue.getText()));
//            }
//        });
//
//        settings.setOnMouseClicked(event -> {
//            leftpane.setVisible(showSettings = !showSettings);
//            news.setVisible(!showSettings);
//            settings.setText(showSettings ? "RETOUR" : "PARAMÈTRES");
//        });
        //#endregion

        //#region account
//        logout.setOnMouseClicked(event -> {
//            ModcraftApplication.launcherConfig.setKeeplogin(false);
//            ModcraftApplication.getWindow().setScene(Utils.loadFxml("login.fxml", true));
//        });
        //#endregion

        //#region window action
        close.setOnMouseClicked(event -> {
            ModcraftApplication.getWindow().hide();
            System.exit(0);
        });

        minimize.setOnMouseClicked(event ->  {
            ModcraftApplication.getWindow().setIconified(true);
            ModcraftApplication.launcherConfig.save();
        });
        //#endregion
    }

    @Override
    public void onProgressUpdate(String progress) {
//        progessLabel.setText(progress);
    }
}
