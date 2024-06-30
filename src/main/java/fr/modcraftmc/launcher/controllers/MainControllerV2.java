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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import net.raphimc.minecraftauth.step.java.StepMCProfile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class MainControllerV2 extends BaseController implements ProgressCallback {

    @FXML
    private Pane topContainer;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private HBox hbox;
    @FXML
    private Pane playBtn;
    @FXML
    private Label playerName;
    @FXML
    private Label playerRank;
    @FXML
    private ImageView playerHead;

    @FXML
    private Label loadingText;
    @FXML
    private MFXProgressSpinner loadingIndicator;

    @FXML
    private MFXProgressBar progressBar;
    @FXML
    private Label progressLabel;

    @FXML
    private Pane discordBtn;
    @FXML
    private Button settingsBtn;
    private Pane settingsPane;

    @FXML
    public Label serverStatus;
    @FXML
    public Label playersCount;
    @FXML
    public Circle serverColor;

    private ModcraftServiceUserProfile currentModcraftProfile;

    private boolean updatePopupAlreadyShowed;
    private boolean settingsStatus;

    private StepMCProfile.MCProfile mcProfile;

    private int newsNumber = 0;

    @Override
    public void initialize(FXMLLoader loader) throws Exception {
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
            parsePlayerRank(currentModcraftProfile.info.role().name().toLowerCase(), playerRank);
        } catch (Exception e) {
            Exception apiError = new Exception("Impossible de récuperer votre profile depuis notre API. Si le problème persiste, contactez-nous sur discord.");
            throw apiError;
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
                MinecraftPingReply minecraftPing = new MinecraftPing().getPing("prodv4.modcraftmc.fr");
                ModcraftApplication.LOGGER.info(String.format("Updating server status (%s/%s)", minecraftPing.getPlayers().getOnline(), minecraftPing.getPlayers().getMax()));

                Platform.runLater(() -> {
                    if (minecraftPing.getDescription().getText().contains("maintenance")) {
                        serverColor.setFill(Color.valueOf("#FE8E01"));
                        serverStatus.setText("Serveur en maintenance");
                    } else {
                        serverColor.setFill(Color.valueOf("#10CB00"));
                        serverStatus.setText("Serveur en ligne");
                    }
                    playersCount.setText(String.format("%s/%s joueurs", minecraftPing.getPlayers().getOnline(), minecraftPing.getPlayers().getMax()));
                });
            } catch (IOException e) {
                serverColor.setFill(Color.valueOf("#FE0101"));
                serverStatus.setText("Serveur hors ligne");
                playersCount.setText(String.format("0/100 joueurs"));
                e.printStackTrace();
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
                Platform.runLater(() -> setLauncherState(State.PLAYING));
                process.get().onExit().join();

                ModcraftApplication.LOGGER.info("Game process shutdown");
                Platform.runLater(() -> {
                    ModcraftApplication.getWindow().setIconified(false);
                    setLauncherState(State.IDLE);
                });
            }
        });

        // Check for update every then minutes
        AsyncExecutor.runAsyncAtRate(() -> {
            ModcraftApplication.newsManager.fetchNews();
        }, 0, 10, TimeUnit.MINUTES);

        playBtn.setOnMouseClicked(event -> {
            setLauncherState(State.UPDATING);
            try {
                MaintenanceStatus maintenanceStatus = ModcraftApplication.apiClient.executeRequest(ModcraftApiRequestsExecutor.getMaintenanceStatus());

                if (maintenanceStatus.activated()) {
                    throw new Exception("nous sommes en en maintenance ! \n" + maintenanceStatus.reason());
                }
            } catch (Exception e) {
                Exception apiError = new Exception("Impossible de contacter notre API. Si le problème persiste, contactez-nous sur discord.");
                ErrorsHandler.handleError(e);
                setLauncherState(State.IDLE);
                return;
            }

            InstanceProperty instanceProperty = ModcraftApplication.launcherConfig.getInstanceProperty();
            final File instanceDirectory = instanceProperty.customInstance() ? new File(instanceProperty.customInstancePath()) : new File(FilesManager.INSTANCES_PATH, "reborn");
            if (!instanceDirectory.exists()) instanceDirectory.mkdirs();
            GameUpdater gameUpdater = new GameUpdater(instanceDirectory.toPath(), this);

            AsyncExecutor.runAsync(() -> {
                gameUpdater.update(this, () -> {
                    try {
                        Process process = LaunchManager.launch(instanceDirectory);
                        ModcraftApplication.launcherConfig.setLatestGamePid(process.pid());
                        ModcraftApplication.launcherConfig.save();

                        if (!ModcraftApplication.launcherConfig.isKeepOpen())
                            Platform.runLater(() -> ModcraftApplication.getWindow().setIconified(true));

                        Platform.runLater(() -> setLauncherState(State.PLAYING));
                        process.waitFor();

                        ModcraftApplication.LOGGER.info("Game process shutdown");
                        Platform.runLater(() -> {
                            ModcraftApplication.getWindow().setIconified(false);
                            setLauncherState(State.IDLE);
                        });
                    } catch (Exception e) {
                        ErrorsHandler.handleError(e);
                    }
                });
            });

            // 5% chance to play a sound
            Utils.playSound("doot_doot.mp3", 5);
        });

        if (ModcraftApplication.app.isFirstLaunch) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Cette version du launcher est actuellement en alpha");
            alert.setContentText("En cas de problème, merci de contacter le support sur Discord. (discord.modcraftmc.fr)");

            alert.showAndWait();
        }
    }

    private void parsePlayerRank(String rank, Label playerRank) {
        String finalText = "Joueur";
        Color finalColor = Color.rgb(255, 255, 255);
        if (rank.equalsIgnoreCase("administrateur")) {
            finalText = "Administrateur";
            finalColor = Color.rgb(255, 0, 0);
        } else if (!rank.equals("default")) {
            finalText = rank;
        }
        playerRank.setTextFill(finalColor);
        playerRank.setText(finalText);
    }

    public void buildNewsContainer(List<Pane> newsList) {
        this.newsNumber = newsList.size();
        if (newsList.isEmpty()) {
            loadingText.setText("Aucune news");
            loadingText.setVisible(true);
            loadingIndicator.setVisible(false);
            scrollPane.setVisible(false);
            return;
        }
        scrollPane.setVisible(this.newsNumber > 2);
        loadingIndicator.setVisible(false);
        loadingText.setVisible(false);

        VBox leftBox = ((VBox) hbox.getChildren().get(0));
        VBox rightBox = ((VBox) hbox.getChildren().get(1));

        leftBox.getChildren().clear();
        rightBox.getChildren().clear();

        for (int i = 0; i < this.newsNumber; i++) {
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
        settingsBtn.setText("Retour");
        settingsPane.setVisible(true);
        loadingText.setVisible(false);
    }

    public void hideSettings() {
        settingsBtn.setText("Paramètres");
        settingsPane.setVisible(false);
        loadingText.setVisible(this.newsNumber == 0);
        scrollPane.setVisible(this.newsNumber > 2);
    }

    public void setLauncherState(State state) {
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
                progressLabel.setText("Jeu lancé");
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
