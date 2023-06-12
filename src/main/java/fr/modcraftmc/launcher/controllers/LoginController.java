package fr.modcraftmc.launcher.controllers;

import animatefx.animation.FadeIn;
import animatefx.animation.FadeOut;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.Utils;
import fr.modcraftmc.launcher.components.FadeOutWithDuration;
import fr.modcraftmc.libs.auth.AccountManager;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.concurrent.CompletableFuture;

public class LoginController implements IController {

    @FXML private Pane logoContainer;
    @FXML private Pane loadingContainer;
    @FXML private Pane loginFormContainer;
    @FXML private Pane authFormContainer;

    @FXML private Button loginButton;

    @FXML private Button close;
    @FXML private Button minimize;

    @FXML private Button urlButton;
    @FXML private Button codeButton;
    @FXML private Label urlLabel;
    @FXML private Label codeLabel;
    @FXML private Label loadingMessage;
    @FXML private CheckBox keepLoginCheckbox;



    private Pane pane;
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void initialize(FXMLLoader loader) {

        pane = loader.getRoot();
        pane.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        pane.setOnMouseDragged(event -> {
            ModcraftApplication.getWindow().setX(event.getScreenX() - xOffset);
            ModcraftApplication.getWindow().setY(event.getScreenY() - yOffset);
        });


        TranslateTransition logoUp = new TranslateTransition(Duration.seconds(2), logoContainer);
        logoUp.setByY(-200f);

        FadeOut fadeOut = new FadeOut(loadingContainer);
        FadeIn fadeIn = new FadeIn(loginFormContainer);


        logoUp.setByY(-200f);
        logoUp.setDuration(Duration.millis(1000));
        fadeOut.playOnFinished(fadeIn);

        TranslateTransition loginFormContainerLeft = new TranslateTransition(Duration.millis(300), loginFormContainer);
        loginFormContainerLeft.setByX(-200f);
        FadeOutWithDuration loginFormContainerLeftFadeOut = new FadeOutWithDuration(loginFormContainer);

        TranslateTransition authFormContainerLeft = new TranslateTransition(Duration.millis(300), authFormContainer);
        authFormContainerLeft.setByX(-200f);
        FadeIn authFormContainerFadeOut = new FadeIn(authFormContainer);
        FadeIn loadingContainerFadeIn = new FadeIn(loadingContainer);


        loginButton.setOnMouseClicked((event -> {

            AccountManager.authenticate((MsaDeviceCode) -> {
                Platform.runLater(() -> {
                    String authUrl = MsaDeviceCode.verificationUri();
                    String authCode = MsaDeviceCode.userCode();

                    urlLabel.setText("Url: " + authUrl);
                    codeLabel.setText("Code: " + authCode);

                    urlButton.setOnMouseClicked(unused -> {
                        Utils.openBrowser(authUrl);
                    });

                    codeButton.setOnMouseClicked(unused -> {
                        Utils.copyToClipboard(authCode);
                    });

                    loginFormContainerLeft.play();
                    loginFormContainerLeftFadeOut.play();

                    authFormContainerLeft.play();
                    authFormContainerFadeOut.play();

                    loadingMessage.setText("En attente de connexion");
                    loadingContainerFadeIn.play();
                });
            }).thenAcceptAsync(authResult -> {
                if (authResult.isLoggedIn()) {
                    loadingMessage.setText("Connecté!");
                    pleaseWait().thenAcceptAsync((unused) -> {
                        Scene scene = Utils.loadFxml("main.fxml", false);
                        ((MainController) scene.getUserData()).updateUserInfos(authResult.getMcProfile());
                        ModcraftApplication.getWindow().setScene(scene);
                    }, Platform::runLater);
                }
            }, Platform::runLater);
        }));

        keepLoginCheckbox.setOnAction(event -> {
            ModcraftApplication.launcherConfig.setKeeplogin(keepLoginCheckbox.isSelected());
        });

        AccountManager.validate(loadingMessage).thenAcceptAsync((authResult -> {
            if (!authResult.isLoggedIn()) {
                Platform.runLater(() -> {
                    fadeOut.play();
                    logoUp.play();
                });
            } else {
                loadingMessage.setText("Connecté!");
                pleaseWait().thenAcceptAsync((unused) -> {
                    Scene scene = Utils.loadFxml("main.fxml", false);
                    ((MainController) scene.getUserData()).updateUserInfos(authResult.getMcProfile());
                    ModcraftApplication.getWindow().setScene(scene);
                }, Platform::runLater);

            }

        }), Platform::runLater);

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

    private CompletableFuture<Void> pleaseWait() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }
}
