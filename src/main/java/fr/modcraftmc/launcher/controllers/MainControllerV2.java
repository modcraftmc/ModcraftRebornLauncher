package fr.modcraftmc.launcher.controllers;

import fr.modcraftmc.api.ModcraftApiRequestsExecutor;
import fr.modcraftmc.api.models.MaintenanceStatus;
import fr.modcraftmc.launcher.*;
import fr.modcraftmc.launcher.configuration.InstanceProperty;
import fr.modcraftmc.launcher.resources.FilesManager;
import fr.modcraftmc.libs.api.ModcraftServiceUserProfile;
import fr.modcraftmc.libs.errors.ErrorsHandler;
import fr.modcraftmc.libs.launch.LaunchManager;
import fr.modcraftmc.libs.popup.PopupBuilder;
import fr.modcraftmc.libs.serverpinger.MinecraftPing;
import fr.modcraftmc.libs.serverpinger.MinecraftPingReply;
import fr.modcraftmc.libs.updater.GameUpdater;
import fr.modcraftmc.libs.updater.ProgressCallback;
import io.github.palexdev.materialfx.controls.MFXProgressBar;
import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
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
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class MainControllerV2 extends BaseController implements ProgressCallback {

    @FXML private Pane topContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private HBox hbox;
    @FXML private Pane playBtn;
    @FXML private Label playerName;
    @FXML private Label playerRank;
    @FXML private ImageView playerHead;

    @FXML private Label loadingText;
    @FXML private MFXProgressSpinner loadingIndicator;

    @FXML private MFXProgressBar progressBar;
    @FXML private Label progressLabel;

    @FXML private Pane discordBtn;
    @FXML private Button settingsBtn;
    private Pane settingsPane;

    private ModcraftServiceUserProfile currentModcraftProfile;

    private boolean updatePopupAlreadyShowed;
    private boolean settingsStatus;

    private StepMCProfile.MCProfile mcProfile;

    @Override
    public void initialize(FXMLLoader loader) {
       super.initialize(loader);

       ModcraftApplication.newsManager.onNewsUpdateCallback(this::buildNewsContainer);

       this.mcProfile = ModcraftApplication.accountManager.getCurrentMCProfile();

       ModcraftApplication.LOGGER.warning("account name " + mcProfile.getName());

        playerName.setText(mcProfile.getName());
        try {
            Image image = new Image(new URL("https://minotar.net/avatar/" + mcProfile.getName()).openStream(), 64, 64, false, false);
            playerHead.setImage(image);
        } catch (IOException e) {
            ErrorsHandler.handleError(e);
        }

        try {
            currentModcraftProfile = ModcraftServiceUserProfile.getProfile(mcProfile.getMcToken().getAccessToken());
            playerRank.setText(currentModcraftProfile.info.role().name().toLowerCase());
        } catch (Exception e) {
            ModcraftApplication.LOGGER.severe("Error while getting modcraft profile");
            ErrorsHandler.handleErrorAndCrashApplication(e);
        }

       progressBar.setVisible(false);
       progressLabel.setVisible(false);

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

        discordBtn.setOnMouseClicked((event) -> {
            Utils.openBrowser("https://discord.modcraftmc.fr");
        });

        AsyncExecutor.runAsyncAtRate(() -> {
            try {
                MinecraftPingReply minecraftPing = new MinecraftPing().getPing("servers.modcraftmc.fr");
                ModcraftApplication.LOGGER.info(String.format("Updating server status (%s/%s)",  minecraftPing.getPlayers().getOnline(), minecraftPing.getPlayers().getMax()));

                Platform.runLater(() -> {
                    //playersCount.setText(String.format("%s/%s", minecraftPing.getPlayers().getOnline(), minecraftPing.getPlayers().getMax()));
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, 10);

        // Check for update every then minutes
        AsyncExecutor.runAsyncAtRate(() -> {
            if (updatePopupAlreadyShowed) return;
            SelfUpdater.checkUpdate().thenAcceptAsync(selfUpdateResult -> {
                if (selfUpdateResult.hasUpdate()) {
                    Alert alert = new PopupBuilder().setHeader("Une mise à jour est disponible").setText("Le launcher va redémarrer.").build();
                    alert.show();
                    updatePopupAlreadyShowed = true;
                    alert.setOnCloseRequest(dialogEvent -> {
                        SelfUpdater.doUpdate(selfUpdateResult.bootstrapPath());
                    });
                }
            }, Platform::runLater);
        }, 10, 10, TimeUnit.MINUTES);

        AsyncExecutor.runAsync(() -> {
            Optional<ProcessHandle> process = ProcessHandle.of(ModcraftApplication.launcherConfig.latestGamePid());

            if (process.isPresent() && process.get().isAlive()) {
                Platform.runLater(() -> setLauncherState(MainControllerV2.State.PLAYING));
                while (process.get().isAlive()) {}

                ModcraftApplication.LOGGER.info("Game process shutdown");
                Platform.runLater(() -> {
                    ModcraftApplication.getWindow().setIconified(false);
                    setLauncherState(MainControllerV2.State.IDLE);
                });
            }
        });

        // Check for update every then minutes
        AsyncExecutor.runAsyncAtRate(() -> {
            ModcraftApplication.newsManager.fetchNews();
        }, 0, 10, TimeUnit.MINUTES);

        playBtn.setOnMouseClicked(event -> {
            setLauncherState(MainControllerV2.State.UPDATING);
            try {
                MaintenanceStatus maintenanceStatus = ModcraftApplication.apiClient.executeRequest(ModcraftApiRequestsExecutor.getMaintenanceStatus());

                if (maintenanceStatus.activated()) {
                    throw new Exception("nous sommes en en maintenance ! \n" + maintenanceStatus.reason());
                }
            } catch (Exception e) {
                ErrorsHandler.handleError(e);
                setLauncherState(MainControllerV2.State.IDLE);
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
                            Process process = LaunchManager.launch(instanceDirectory);
                            ModcraftApplication.launcherConfig.setLatestGamePid(process.pid());
                            ModcraftApplication.launcherConfig.save();
                            Platform.runLater(() -> setLauncherState(State.PLAYING));
                            while (process.isAlive()) {}

                            ModcraftApplication.LOGGER.info("Game process shutdown");
                            Platform.runLater(() -> {
                                ModcraftApplication.getWindow().setIconified(false);
                                setLauncherState(MainControllerV2.State.IDLE);
                            });
                        } catch (Exception e) {
                            ErrorsHandler.handleError(e);
                        }
                    });
                });
            });
        });

        if (ModcraftApplication.app.isFirstLaunch) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Cette version du launcher est actuellement en alpha");
            alert.setContentText("En cas de problème, merci de contacter le support sur Discord. (discord.modcraftmc.fr)");

            alert.showAndWait();
        }
    }

    public void buildNewsContainer(List<Pane> newsList) {
        loadingIndicator.setVisible(false);
        loadingText.setVisible(false);

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

    public void setLauncherState(MainControllerV2.State state) {
        switch (state) {
            case IDLE -> {
                progressBar.setVisible(false);
                progressLabel.setVisible(false);
                playBtn.setVisible(true);
//                play.setDisable(false);
//                progressBarOutAnimation();
//                play.getStyleClass().remove("play-button-running");
//                playIndicator.setVisible(false);
            }
            case UPDATING -> {
                progressBar.setVisible(true);
                progressLabel.setVisible(true);
                playBtn.setVisible(false);
            }
//                play.setDisable(true);
//                progressBarInAnimation();
//                play.getStyleClass().add("play-button-running");
//                playIndicator.setVisible(true);
            case PLAYING -> {
                progressBar.setVisible(true);
                progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                progressLabel.setVisible(true);
                progressLabel.setText("Lancement du jeu");
                playBtn.setVisible(false);
//                play.setDisable(true);
//                progressBarOutAnimation();
//                play.getStyleClass().add("play-button-running");
//                playIndicator.setVisible(true);
            }
        }
    }



    @Override
    public void onProgressUpdate(String progress, int current, int max) {
        Platform.runLater(() -> {
            if (max > 0) {
                progressBar.setProgress((double) current / max);
                progressLabel.setText(progress + " " + current + "/" + max);
            } else if (current == -1) {
                progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
                progressLabel.setText(progress);
            } else {
                progressLabel.setText(progress);
            }
        });
    }

    public enum State {
        IDLE,
        UPDATING,
        PLAYING
    }
}
