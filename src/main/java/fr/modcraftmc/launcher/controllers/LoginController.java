package fr.modcraftmc.launcher.controllers;

import animatefx.animation.FadeIn;
import animatefx.animation.FadeOut;
import fr.modcraftmc.launcher.MFXMLLoader;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.Utils;
import fr.modcraftmc.libs.auth.AccountManager;
import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class LoginController extends BaseController {

    @FXML public Pane welcomeContainer;
    @FXML public Pane authContainer;
    @FXML public Pane loadingContainer;

    @FXML public Pane microsoftButton;
    @FXML public Pane codeButton;
    @FXML public Pane copyAndOpenButton;
    @FXML public Label authCode;
    @FXML private Label loadingMessage;
    @FXML private MFXProgressSpinner loadingIndicator;
    @FXML private Label microsoftText;


    @Override
    public void initialize(FXMLLoader loader) {
        super.initialize(loader);

        TranslateTransition loginFormContainerLeft = new TranslateTransition(Duration.millis(300), welcomeContainer);
        loginFormContainerLeft.setByX(-300f);
        FadeOut loginFormContainerLeftFadeOut = new FadeOut(welcomeContainer);

        TranslateTransition authFormContainerLeft = new TranslateTransition(Duration.millis(300), authContainer);
        authFormContainerLeft.setByX(-300f);
        FadeIn authFormContainerFadeOut = new FadeIn(authContainer);
        FadeIn loadingContainerFadeIn = new FadeIn(loadingContainer);

        loadingIndicator.setVisible(false);

        microsoftButton.setOnMouseClicked(event -> {
            microsoftButton.setDisable(true); //TODO: replace the text with a loading animation

            loadingIndicator.setVisible(true);
            microsoftText.setVisible(false);

            AccountManager.authenticate(msaDeviceCode -> {
                Platform.runLater(() -> {
                    loginFormContainerLeft.play();
                    loginFormContainerLeftFadeOut.play();
                    authFormContainerLeft.play();
                    authFormContainerFadeOut.play();

                    authCode.setText(msaDeviceCode.getUserCode());
                    loadingContainerFadeIn.play();
                    loadingMessage.setText("En attente de connexion");

                    copyAndOpenButton.setOnMouseClicked(unused -> {
                        Utils.copyToClipboard(msaDeviceCode.getUserCode());
                        Utils.openBrowser(msaDeviceCode.getDirectVerificationUri());
                    });

                    codeButton.setOnMouseClicked(unused -> {
                        Utils.copyToClipboard(msaDeviceCode.getUserCode());
                    });
                });
            }).thenAcceptAsync(authResult -> {
                if (authResult.isLoggedIn()) {
                    loadingMessage.setText("Connecté!");

                    

                    Utils.pleaseWait(2000).thenAcceptAsync((unused) -> {
                        microsoftButton.setDisable(false);
                        ModcraftApplication.accountManager.setCurrentMCProfile(authResult.getMcProfile());
                        Scene scene = MFXMLLoader.loadFxml("main_v2.fxml", true);
                        ModcraftApplication.getWindow().setScene(scene);
                    }, Platform::runLater);
                }
            }, Platform::runLater);
        });
    }
}
