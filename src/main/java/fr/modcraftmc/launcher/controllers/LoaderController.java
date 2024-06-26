package fr.modcraftmc.launcher.controllers;

import fr.modcraftmc.launcher.MFXMLLoader;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.Utils;
import fr.modcraftmc.libs.auth.AccountManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;

import java.util.concurrent.CompletableFuture;

public class LoaderController extends BaseController {

    @FXML
    private Label loadingMessage;

    @Override
    public void initialize(FXMLLoader loader) throws Exception {
        super.initialize(loader);

        ModcraftApplication.startupTasksManager.init(loadingMessage);
        CompletableFuture.runAsync(() -> ModcraftApplication.startupTasksManager.execute());

        CompletableFuture.runAsync(() -> {
            AccountManager.AuthResult authResult = AccountManager.validate(loadingMessage);
            if (authResult.isLoggedIn()) {
                Platform.runLater(() -> loadingMessage.setText("Connecté!"));
                Utils.selfCatchSleep(1500);

                ModcraftApplication.accountManager.setCurrentMCProfile(authResult.getMcProfile());
                Scene scene = MFXMLLoader.loadFxml("main_v2.fxml", false);
                Platform.runLater(() -> ModcraftApplication.switchScene(1300, 700,  scene));
            } else {
                Scene scene = MFXMLLoader.loadFxml("login.fxml", false);
                Platform.runLater(() -> ModcraftApplication.switchScene(1300, 700, scene));
            }
        });
    }
}
