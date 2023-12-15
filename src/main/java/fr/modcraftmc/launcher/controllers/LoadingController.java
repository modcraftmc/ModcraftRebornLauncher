package fr.modcraftmc.launcher.controllers;

import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.Utils;
import fr.modcraftmc.libs.auth.AccountManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;

public class LoadingController extends BaseController {

    @FXML
    private Label loadingMessage;

    @Override
    public void initialize(FXMLLoader loader) {
        super.initialize(loader);

        AccountManager.validate(loadingMessage).thenAcceptAsync((authResult -> {
            if (authResult.isLoggedIn())  {
                loadingMessage.setText("Connecté!");
                Utils.pleaseWait(500).thenAcceptAsync((unused) -> {
                    Scene scene = Utils.loadFxml("main.fxml", false);
                    ((MainController) scene.getUserData()).updateUserInfos(authResult.getMcProfile());
                    ModcraftApplication.getWindow().setScene(scene);
                }, Platform::runLater);
            } else {
                Scene scene = Utils.loadFxml("login.fxml", false);
                ModcraftApplication.getWindow().setScene(scene);
            }
        }), Platform::runLater);
    }
}
