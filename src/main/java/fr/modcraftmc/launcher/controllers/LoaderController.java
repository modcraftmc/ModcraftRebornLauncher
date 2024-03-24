package fr.modcraftmc.launcher.controllers;

import fr.modcraftmc.launcher.MFXMLLoader;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.SelfUpdater;
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
    public void initialize(FXMLLoader loader) {
        super.initialize(loader);

        SelfUpdater.checkUpdate();

        //TODO: check for update every X minutes and not at startup
//        SelfUpdater.checkUpdate().thenApplyAsync(selfUpdateResult -> {
//            if (selfUpdateResult.hasUpdate()) {
//                Utils.selfCatchSleep(1500);
//                File updater = new File(FilesManager.LAUNCHER_PATH, "updater.jar");
//                try {
//                    Process proc = Runtime.getRuntime().exec(String.format("java -jar %s", updater.getAbsolutePath()));
//                    ModcraftApplication.shutdown(0);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//            return selfUpdateResult;

        CompletableFuture.runAsync(() -> {
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
                    ModcraftApplication.getWindow().centerOnScreen();
                });
            } else {
                    Platform.runLater(() -> {
                        ModcraftApplication.getWindow().hide();
                        ModcraftApplication.getWindow().setWidth(1300);
                        ModcraftApplication.getWindow().setHeight(700);
                        Scene scene = MFXMLLoader.loadFxml("login.fxml", false);
                        ModcraftApplication.getWindow().setScene(scene);
                        ModcraftApplication.getWindow().show();
                        ModcraftApplication.getWindow().centerOnScreen();
                    });
                }
        });
    }
}
