package fr.modcraftmc.launcher.controllers;

import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.components.SizeTransition;
import fr.modcraftmc.launcher.configuration.InstanceProperty;
import fr.modcraftmc.launcher.resources.FilesManager;
import fr.modcraftmc.libs.auth.AccountManager;
import fr.modcraftmc.libs.launch.LaunchManager;
import fr.modcraftmc.libs.serverpinger.MinecraftPing;
import fr.modcraftmc.libs.serverpinger.MinecraftPingOptions;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;
import net.hycrafthd.minecraft_authenticator.login.User;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;


public class MainController implements IController, ProgressCallback {

    //Drag
    private double xOffset = 0;
    private double yOffset = 0;

    //Window
    @FXML public Button close;
    @FXML public Button minimize;

    @FXML public Button play;

    //Account
    private boolean isLogged = false;
    @FXML public Pane accountContainer;
    @FXML public Button login;
    @FXML public Button logout;
    @FXML public Label playerName;
    @FXML public Label playerRank;
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
    public void initialize(FXMLLoader loader) {
        AnchorPane pane = loader.getRoot();
        pane.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        pane.setOnMouseDragged(event -> {
            ModcraftApplication.getWindow().setX(event.getScreenX() - xOffset);
            ModcraftApplication.getWindow().setY(event.getScreenY() - yOffset);
        });

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

//            LaunchManager.launch(new File(FilesManager.INSTANCES_PATH, "v4-staff"));
//            if (true) {
//                return;
//            }

            InstanceProperty instanceProperty = ModcraftApplication.launcherConfig.getInstanceProperty();
            File instanceDirectory = instanceProperty.isCustomInstance() ? new File(instanceProperty.getCustomInstancePath()) : new File(FilesManager.INSTANCES_PATH, "v4-staff");
            if (!instanceDirectory.exists()) instanceDirectory.mkdirs();
            GameUpdater gameUpdater = new GameUpdater("", instanceDirectory.toPath(), this);

            gameUpdater.update().thenRun(() -> {
                LaunchManager.launch(instanceDirectory);
            });

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
//            ModcraftApplication.getWindow().setScene(Utils.loadFxml("login.fxml", true));
            setLogged(false);
        });

        login.setOnMouseClicked(event -> {
            triggerAuthentification();
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
    public void onProgressUpdate(String progress) {
//        progessLabel.setText(progress);
    }

    public void triggerAuthentification() {
        block("Authentication...");
        AtomicReference<AuthenticationPopupController> popup = new AtomicReference<>();
        CompletableFuture<Boolean> futureBoolean = AccountManager.tryLogin((url, canceler) -> Platform.runLater(() -> popup.set(AuthenticationPopupController.show(url, () -> canceler.run()))));
        futureBoolean.orTimeout(5, TimeUnit.MINUTES).thenAccept(success -> {
            if (success) {
                Platform.runLater(() -> {
                    popup.get().close();
                    updateUserInfos(AccountManager.getAuthInfos().get());
                });
                setLogged(true);
            } else {
                ModcraftApplication.LOGGER.warning("Authentication failed");
            }
            unblock();
        }).exceptionally(throwable -> {
            ModcraftApplication.LOGGER.warning("Authentication timeout");
            throwable.printStackTrace();
            unblock();
            return null;
        });
    }

    public void setLogged(boolean isLogged) {
        this.isLogged = isLogged;
        if (isLogged) {
            loginAnimation();
        } else {
            logoutAnimation();
        }
    }

    public void logoutAnimation() {
        ParallelTransition transition = new ParallelTransition();

        Node[] fadeOutNodes = new Node[]{logout, playerName, playerHead, playerRank};
        Node[] fadeInNodes = new Node[]{login};
        for (Node node : fadeOutNodes) {
            node.setMouseTransparent(true);
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), node);
            fadeTransition.setFromValue(1);
            fadeTransition.setToValue(0);
            transition.getChildren().add(fadeTransition);
        }
        for (Node node : fadeInNodes) {
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), node);
            fadeTransition.setDelay(Duration.millis(900));
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);
            transition.getChildren().add(fadeTransition);
        }

        SizeTransition scaleTransition = new SizeTransition(Duration.millis(500), accountContainer);
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(500), accountContainer);
        scaleTransition.setDelay(Duration.millis(400));
        scaleTransition.setFromValues(accountContainer.getPrefWidth(), accountContainer.getPrefHeight());
        scaleTransition.setToValues(accountContainer.getPrefWidth(), accountContainer.getPrefHeight() * 0.5);
        translateTransition.setDelay(Duration.millis(400));
        translateTransition.setFromY(0);
        translateTransition.setToY(100);

        transition.getChildren().addAll(scaleTransition, translateTransition);
        transition.setOnFinished(event -> {
            for (Node node : fadeInNodes) {
                node.setMouseTransparent(false);
            }
        });
        transition.play();
    }

    public void loginAnimation() {
        ParallelTransition transition = new ParallelTransition();

        Node[] fadeOutNodes = new Node[]{login};
        Node[] fadeInNodes = new Node[]{logout, playerName, playerHead, playerRank};
        for (Node node : fadeOutNodes) {
            node.setMouseTransparent(true);
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), node);
            fadeTransition.setFromValue(1);
            fadeTransition.setToValue(0);
            transition.getChildren().add(fadeTransition);
        }
        for (Node node : fadeInNodes) {
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), node);
            fadeTransition.setDelay(Duration.millis(900));
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);
            transition.getChildren().add(fadeTransition);
        }

        SizeTransition scaleTransition = new SizeTransition(Duration.millis(500), accountContainer);
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(500), accountContainer);
        scaleTransition.setDelay(Duration.millis(400));
        scaleTransition.setFromValues(accountContainer.getPrefWidth(), accountContainer.getPrefHeight());
        scaleTransition.setToValues(accountContainer.getPrefWidth(), accountContainer.getPrefHeight() * 2);
        translateTransition.setDelay(Duration.millis(400));
        translateTransition.setFromY(100);
        translateTransition.setToY(0);

        transition.getChildren().addAll(scaleTransition, translateTransition);
        transition.setOnFinished(event -> {
            for (Node node : fadeInNodes) {
                node.setMouseTransparent(false);
            }
        });
        transition.play();
    }

    public void settingsInAnimation(){
        ParallelTransition transition = new ParallelTransition();

        Node[] fadeOutNodes = new Node[]{news};
        Node[] fadeInNodes = new Node[]{settingsPane};
        for (Node node : fadeOutNodes) {
            node.setMouseTransparent(true);
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), node);
            fadeTransition.setFromValue(1);
            fadeTransition.setToValue(0);
            transition.getChildren().add(fadeTransition);
        }
        for (Node node : fadeInNodes) {
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), node);
            fadeTransition.setDelay(Duration.millis(500));
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
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), node);
            fadeTransition.setFromValue(1);
            fadeTransition.setToValue(0);
            transition.getChildren().add(fadeTransition);
        }
        for (Node node : fadeInNodes) {
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), node);
            fadeTransition.setDelay(Duration.millis(500));
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

    public void block(String message){
        blocker.setMouseTransparent(false);
        blocker.setVisible(true);
        blockerText.setText(message);
        ModcraftApplication.LOGGER.info("Blocking UI with message : " + message);
    }

    public void unblock(){
        blocker.setMouseTransparent(true);
        blocker.setVisible(false);
    }
}
