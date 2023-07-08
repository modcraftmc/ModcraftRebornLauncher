package fr.modcraftmc.launcher.controllers;

import fr.modcraftmc.launcher.AsyncExecutor;
import fr.modcraftmc.launcher.MaintenanceManager;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.Utils;
import fr.modcraftmc.launcher.configuration.InstanceProperty;
import fr.modcraftmc.launcher.resources.FilesManager;
import fr.modcraftmc.libs.errors.ErrorsHandler;
import fr.modcraftmc.libs.launch.LaunchManager;
import fr.modcraftmc.libs.serverpinger.MinecraftPing;
import fr.modcraftmc.libs.serverpinger.MinecraftPingReply;
import fr.modcraftmc.libs.updater.GameUpdater;
import fr.modcraftmc.libs.updater.ProgressCallback;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXSlider;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;
import net.raphimc.mcauth.step.java.StepMCProfile;

import java.io.File;
import java.io.IOException;
import java.net.URL;


public class MainController extends BaseController implements ProgressCallback {

    public enum State {
        IDLE,
        UPDATING,
        PLAYING
    }
    //Drag
    private double xOffset = 0;
    private double yOffset = 0;

    //Window
    @FXML public Button close;
    @FXML public Button minimize;

    @FXML public Button play;
    @FXML public ProgressIndicator playIndicator;

    //Account
    @FXML public Pane accountContainer;
    @FXML public Button logout;
    @FXML public Label playerName;
    @FXML public Label playerRank;
    @FXML public ImageView playerHead;

    //Server state
    @FXML public Label serverState;
    @FXML public Label playersCount;

    @FXML public ProgressBar progressBar;
    @FXML public Label progressText;

    //News
    @FXML public Pane news;

    //Settings
    @FXML public Button settings;
    private boolean showSettings = false;
    @FXML public Pane settingsPane;
    @FXML public Pane pathBox;

    @FXML public MFXSlider ramSlider;
    @FXML public Label ramText;

    @FXML public MFXCheckbox keepConnected;
    @FXML public MFXCheckbox keepOpen;
    @FXML public MFXCheckbox customPath;
    @FXML public MFXTextField customPathValue;
    @FXML public MFXButton browseCustomPath;

    //Blocker
    @FXML public Pane blocker;
    @FXML public Label blockerText;

    private Process launchProcess;
    private boolean isUpdateLaunched = false;
    private AnchorPane pane;

    private StepMCProfile.MCProfile currentProfile;

    public void updateUserInfos(StepMCProfile.MCProfile authInfos) {
        this.currentProfile = authInfos;
        playerName.setText(authInfos.name());
        try {
            Image image = new Image(new URL("https://minotar.net/avatar/" + authInfos.name()).openStream(), 64, 64, false, false);
            playerHead.setImage(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(FXMLLoader loader) {
        super.initialize(loader);

        setLauncherState(State.IDLE);

        AsyncExecutor.runAsyncAtRate(() -> {
            try {
                MinecraftPingReply minecraftPing = new MinecraftPing().getPing("servers.modcraftmc.fr");
                ModcraftApplication.LOGGER.info("Updating server status");

                Platform.runLater(() -> {
                    playersCount.setText(String.format("%s/%s", minecraftPing.getPlayers().getOnline(), minecraftPing.getPlayers().getMax()));
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 10);


        play.setOnMouseClicked(event -> {
            MaintenanceManager.MaintenanceStatus maintenanceStatus = MaintenanceManager.getMaintenanceStatusSync();
            if (maintenanceStatus.isActivated()) {
                ErrorsHandler.handleErrorWithCustomHeader("nous sommes en en maintenance !", new Exception(maintenanceStatus.reason()));
               return;
            }

            setLauncherState(State.UPDATING);
            InstanceProperty instanceProperty = ModcraftApplication.launcherConfig.getInstanceProperty();
            final File instanceDirectory = instanceProperty.isCustomInstance() ? new File(instanceProperty.getCustomInstancePath()) : new File(FilesManager.INSTANCES_PATH, "reborn");
            if (!instanceDirectory.exists()) instanceDirectory.mkdirs();
            GameUpdater gameUpdater = new GameUpdater(instanceDirectory.toPath(), this);

            AsyncExecutor.runAsync(() -> {
                gameUpdater.update(() -> {
                    Process process = LaunchManager.launch(instanceDirectory, currentProfile);

                    AsyncExecutor.runAsync(() -> {
                        while (process.isAlive()) {}

                        ModcraftApplication.LOGGER.info("Game process shutdown");
                        Platform.runLater(() -> setLauncherState(State.IDLE));

                    });
                });
            });
        });

        //#region settings
        settingsPane.setMouseTransparent(true); //Disable click on settings pane
        ramSlider.setMin(4);
        ramSlider.setMax(16);
        ramSlider.setValue(ModcraftApplication.launcherConfig.getRam());
        ramText.setText(String.format("%s Gb", (int) ramSlider.getValue()));
        keepConnected.setSelected(ModcraftApplication.launcherConfig.isKeeplogin());
        keepOpen.setSelected(ModcraftApplication.launcherConfig.isKeepOpen());
        customPath.setSelected(ModcraftApplication.launcherConfig.getInstanceProperty().isCustomInstance());
        pathBox.setDisable(!customPath.isSelected());
        customPathValue.setText(ModcraftApplication.launcherConfig.getInstanceProperty().getCustomInstancePath());


        ramSlider.setOnMouseDragged(event -> {
            ramText.setText(String.format("%s Gb", (int) ramSlider.getValue()));
            ModcraftApplication.launcherConfig.setRam((int) ramSlider.getValue());
        });

        customPath.setOnMouseClicked(event -> {
            if (!customPath.isSelected()) {
                customPathValue.setText("");
                ModcraftApplication.launcherConfig.setInstanceProperty(new InstanceProperty(false, ""));
            }
            pathBox.setDisable(!customPath.isSelected());
        });

        keepConnected.setOnMouseClicked(event -> ModcraftApplication.launcherConfig.setKeeplogin(keepConnected.isSelected()));

        keepOpen.setOnMouseClicked(event -> ModcraftApplication.launcherConfig.setKeepOpen(keepOpen.isSelected()));

        browseCustomPath.setOnMouseClicked(event -> {
            DirectoryChooser fileChooser = new DirectoryChooser();
            fileChooser.setTitle("Select directory");
            fileChooser.setInitialDirectory(FilesManager.INSTANCES_PATH);
            File path = fileChooser.showDialog(ModcraftApplication.getWindow());
            if (path != null) {
                customPathValue.setText(path.getAbsolutePath());
                ModcraftApplication.launcherConfig.setInstanceProperty(new InstanceProperty(true, customPathValue.getText()));
            }
        });

        settings.setOnMouseClicked(event -> {
            showSettings = !showSettings;
            if(showSettings) {
                settingsInAnimation();
            } else {
                settingsOutAnimation();
            }
        });
        //#endregion

        //#region account
        logout.setOnMouseClicked(event -> {
            ModcraftApplication.launcherConfig.setKeeplogin(false);
            ModcraftApplication.getWindow().setScene(Utils.loadFxml("auth.fxml", true));
        });

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
    public void onProgressUpdate(String progress, int current, int max) {

        Platform.runLater(() -> {
            progressBar.setProgress((double) current / max);
            progressText.setText(progress + " " + current + "/" + max);
        });
    }

    public void setLauncherState(State state){
        switch (state){
            case IDLE -> {
                play.setDisable(false);
                progressBarOutAnimation();
                play.getStyleClass().remove("play-button-running");
                playIndicator.setVisible(false);
            }
            case UPDATING -> {
                play.setDisable(true);
                progressBarInAnimation();
                play.getStyleClass().add("play-button-running");
                playIndicator.setVisible(true);
            }
            case PLAYING -> {
                play.setDisable(true);
                progressBarOutAnimation();
                play.getStyleClass().add("play-button-running");
                playIndicator.setVisible(true);
            }
        }
    }

    public void settingsInAnimation(){
        ParallelTransition transition = new ParallelTransition();

        Node[] fadeOutNodes = new Node[]{news};
        Node[] fadeInNodes = new Node[]{settingsPane};
        for (Node node : fadeOutNodes) {
            node.setMouseTransparent(true);
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(150), node);
            fadeTransition.setFromValue(1);
            fadeTransition.setToValue(0);
            transition.getChildren().add(fadeTransition);
        }
        for (Node node : fadeInNodes) {
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(150), node);
            fadeTransition.setDelay(Duration.millis(150));
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);
            transition.getChildren().add(fadeTransition);
        }

        transition.setOnFinished(event -> {
            for (Node node : fadeInNodes) {
                node.setMouseTransparent(false);
            }
        });
        transition.play();
    }

    public void settingsOutAnimation(){
        ParallelTransition transition = new ParallelTransition();

        Node[] fadeOutNodes = new Node[]{settingsPane};
        Node[] fadeInNodes = new Node[]{news};
        for (Node node : fadeOutNodes) {
            node.setMouseTransparent(true);
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(150), node);
            fadeTransition.setFromValue(1);
            fadeTransition.setToValue(0);
            transition.getChildren().add(fadeTransition);
        }
        for (Node node : fadeInNodes) {
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(150), node);
            fadeTransition.setDelay(Duration.millis(150));
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);
            transition.getChildren().add(fadeTransition);
        }

        transition.setOnFinished(event -> {
            for (Node node : fadeInNodes) {
                node.setMouseTransparent(false);
            }
        });
        transition.play();
    }

    public void progressBarInAnimation(){
        ParallelTransition transition = new ParallelTransition();
        progressBar.setVisible(true);
        progressText.setVisible(true);

        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(500), progressBar);
        TranslateTransition translateTransition2 = new TranslateTransition(Duration.millis(500), progressText);
        translateTransition.setFromY(50);
        translateTransition2.setFromY(50);
        translateTransition.setToY(0);
        translateTransition2.setToY(0);

        transition.getChildren().addAll(translateTransition, translateTransition2);
        transition.play();
    }

    public void progressBarOutAnimation(){
        ParallelTransition transition = new ParallelTransition();

        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(500), progressBar);
        TranslateTransition translateTransition2 = new TranslateTransition(Duration.millis(500), progressText);
        translateTransition.setFromY(0);
        translateTransition2.setFromY(0);
        translateTransition.setToY(50);
        translateTransition2.setToY(50);

        transition.getChildren().addAll(translateTransition, translateTransition2);
        transition.setOnFinished(event -> {
            progressBar.setVisible(false);
            progressText.setVisible(false);
        });
        transition.play();
    }
}
