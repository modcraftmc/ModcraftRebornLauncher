package fr.modcraftmc.launcher.controllers;

import fr.modcraftmc.api.ModcraftApiRequestsExecutor;
import fr.modcraftmc.api.models.MaintenanceStatus;
import fr.modcraftmc.launcher.AsyncExecutor;
import fr.modcraftmc.launcher.MFXMLLoader;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.configuration.InstanceProperty;
import fr.modcraftmc.launcher.resources.FilesManager;
import fr.modcraftmc.libs.api.ModcraftServiceUserProfile;
import fr.modcraftmc.libs.errors.ErrorsHandler;
import fr.modcraftmc.libs.launch.LaunchManager;
import fr.modcraftmc.libs.updater.GameUpdater;
import fr.modcraftmc.libs.updater.ProgressCallback;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import net.raphimc.minecraftauth.step.java.StepMCProfile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainControllerV2 extends BaseController implements ProgressCallback {

    @FXML private Pane topContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private HBox hbox;
    @FXML private Pane playBtn;
    @FXML private Label playerName;
    @FXML private Label playerRank;
    @FXML private ImageView playerHead;

    @FXML private Button settingsBtn;
    private Pane settingsPane;

    private StepMCProfile.MCProfile currentProfile;
    private ModcraftServiceUserProfile currentModcraftProfile;

    private boolean settingsStatus;

    public void updateUserInfos(StepMCProfile.MCProfile authInfos) {
        this.currentProfile = authInfos;
        playerName.setText(authInfos.getName());
        try {
            Image image = new Image(new URL("https://minotar.net/avatar/" + authInfos.getName()).openStream(), 64, 64, false, false);
            playerHead.setImage(image);
        } catch (IOException e) {
            ErrorsHandler.handleError(e);
        }

        try {
            currentModcraftProfile = ModcraftServiceUserProfile.getProfile(authInfos.getMcToken().getAccessToken());
            playerRank.setText(currentModcraftProfile.info.role().name().toLowerCase());
        } catch (Exception e) {
            ModcraftApplication.LOGGER.severe("Error while getting modcraft profile");
            ErrorsHandler.handleErrorAndCrashApplication(e);
        }
    }

    @Override
    public void initialize(FXMLLoader loader) {
       super.initialize(loader);

       ModcraftApplication.newsManager.onNewsUpdateCallback(this::buildNewsContainer);

       settingsPane = MFXMLLoader.loadPane("settings.fxml");
       settingsPane.setVisible(false); // invisible by default
       this.topContainer.getChildren().add(settingsPane);

        settingsBtn.setOnMouseClicked((event) -> {
            if (settingsStatus)
                hideSettings();
            else
                showSettings();

            settingsStatus = !settingsStatus;
        });

        // Check for update every then minutes
        AsyncExecutor.runAsyncAtRate(() -> {
            ModcraftApplication.newsManager.fetchNews();
        }, 0, 10, TimeUnit.MINUTES);

        playBtn.setOnMouseClicked(event -> {
            setLauncherState(MainController.State.UPDATING);
            try {
                MaintenanceStatus maintenanceStatus = ModcraftApplication.apiClient.executeRequest(ModcraftApiRequestsExecutor.getMaintenanceStatus());

                if (maintenanceStatus.activated()) {
                    throw new Exception("nous sommes en en maintenance ! \n" + maintenanceStatus.reason());
                }
            } catch (Exception e) {
                ErrorsHandler.handleError(e);
                setLauncherState(MainController.State.IDLE);
                return;
            }

            InstanceProperty instanceProperty = ModcraftApplication.launcherConfig.getInstanceProperty();
            final File instanceDirectory = instanceProperty.isCustomInstance() ? new File(instanceProperty.getCustomInstancePath()) : new File(FilesManager.INSTANCES_PATH, "reborn");
            if (!instanceDirectory.exists()) instanceDirectory.mkdirs();
            GameUpdater gameUpdater = new GameUpdater(instanceDirectory.toPath(), this);

            AsyncExecutor.runAsync(() -> {
                gameUpdater.update(this, () -> {
                    AsyncExecutor.runAsync(() -> {
                        try {
                            Process process = LaunchManager.launch(instanceDirectory, currentProfile);
                            ModcraftApplication.launcherConfig.setLatestGamePid(process.pid());
                            ModcraftApplication.launcherConfig.save();
                            while (process.isAlive()) {}

                            ModcraftApplication.LOGGER.info("Game process shutdown");
                            Platform.runLater(() -> {
                                ModcraftApplication.getWindow().setIconified(false);
                                setLauncherState(MainController.State.IDLE);
                            });
                        } catch (Exception e) {
                            ErrorsHandler.handleError(e);
                        }
                    });
                });
            });
        });
    }

    public void buildNewsContainer(List<Pane> newsList) {

       VBox leftBox = ((VBox) hbox.getChildren().get(0));
       VBox rightBox = ((VBox) hbox.getChildren().get(1));

        leftBox.getChildren().clear();
        rightBox.getChildren().clear();

        for (int i = 0; i < newsList.size(); i++) {
            Pane newsPane = newsList.get(i);
            if (i % 2 == 0) {
                leftBox.getChildren().add(newsPane);
            } else {
                rightBox.getChildren().add(newsPane);
            }
        }
    }

    public void showSettings() {
        scrollPane.setVisible(false); //TODO: animation

        settingsPane.setVisible(true);
    }

    public void hideSettings() {
        scrollPane.setVisible(true);

        settingsPane.setVisible(false);
    }

    public void setLauncherState(MainController.State state){
//        switch (state){
//            case IDLE -> {
//                play.setDisable(false);
//                progressBarOutAnimation();
//                play.getStyleClass().remove("play-button-running");
//                playIndicator.setVisible(false);
//            }
//            case UPDATING -> {
//                play.setDisable(true);
//                progressBarInAnimation();
//                play.getStyleClass().add("play-button-running");
//                playIndicator.setVisible(true);
//            }
//            case PLAYING -> {
//                play.setDisable(true);
//                progressBarOutAnimation();
//                play.getStyleClass().add("play-button-running");
//                playIndicator.setVisible(true);
//            }
//        }
    }

    @Override
    public void onProgressUpdate(String progress, int current, int max) {

    }
}
