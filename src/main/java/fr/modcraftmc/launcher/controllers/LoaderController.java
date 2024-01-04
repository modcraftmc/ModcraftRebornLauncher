package fr.modcraftmc.launcher.controllers;

import fr.modcraftmc.launcher.*;
import fr.modcraftmc.launcher.resources.FilesManager;
import fr.modcraftmc.libs.auth.AccountManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;

import java.io.File;
import java.io.IOException;

public class LoaderController extends BaseController {

    @FXML
    private Label loadingMessage;

    @Override
    public void initialize(FXMLLoader loader) {
        super.initialize(loader);


        SelfUpdater.checkUpdate().thenApplyAsync(selfUpdateResult -> {
            if (selfUpdateResult.hasUpdate()) {
                Utils.selfCatchSleep(1500);
                File updater = new File(FilesManager.LAUNCHER_PATH, "updater.jar");
                try {
                    Process proc = Runtime.getRuntime().exec(String.format("java -jar %s", updater.getAbsolutePath()));
                    ModcraftApplication.shutdown(0);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return selfUpdateResult;
        }, AsyncExecutor::runAsync).thenRunAsync(() -> {
            AccountManager.AuthResult authResult = AccountManager.validate(loadingMessage);
            if (authResult.isLoggedIn())  {
                Platform.runLater(() -> loadingMessage.setText("Connecté!"));
                Utils.selfCatchSleep(1500);
                Platform.runLater(() -> {
                    ModcraftApplication.getWindow().hide();
                    ModcraftApplication.getWindow().setWidth(1300);
                    ModcraftApplication.getWindow().setHeight(700);
                    Scene scene = MFXMLLoader.loadFxml("main.fxml", false);
                    ((MainController) scene.getUserData()).updateUserInfos(authResult.getMcProfile());
                    ModcraftApplication.getWindow().setScene(scene);
                    ModcraftApplication.getWindow().show();
                });
            } else {
                    Platform.runLater(() -> {
                        Scene scene = MFXMLLoader.loadFxml("login.fxml", false);
                        ModcraftApplication.getWindow().setScene(scene);
                    });
                }
        });
    }
}
