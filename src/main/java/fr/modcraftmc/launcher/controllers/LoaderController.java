package fr.modcraftmc.launcher.controllers;

import fr.modcraftmc.launcher.AsyncExecutor;
import fr.modcraftmc.launcher.MFXMLLoader;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.startup.ITaskResult;
import fr.modcraftmc.launcher.startup.results.ValidateModcraftUserTaskResult;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;

public class LoaderController extends BaseController {

    @FXML
    private Label loadingMessage;

    @Override
    public void initialize(FXMLLoader loader) throws Exception {
        super.initialize(loader);

        ModcraftApplication.startupTasksManager.init(loadingMessage);

        AsyncExecutor.runAsync(() -> {
            ITaskResult result = ModcraftApplication.startupTasksManager.execute();

            // crash should be handled by the task
            if (result.shouldCrash()) {
                return;
            }

            if (result.hasFailed()) {
                Scene scene = MFXMLLoader.loadFxml("login.fxml", false);
                Platform.runLater(() -> ModcraftApplication.switchScene(1300, 700,  scene));
                return;
            }

            Platform.runLater(() -> loadingMessage.setText("Connecté!"));

            // Success
            Scene scene = MFXMLLoader.loadFxml("main_v2.fxml", false);
            ((MainControllerV2) scene.getUserData()).setModcraftUserProfile(((ValidateModcraftUserTaskResult) result));
            Platform.runLater(() -> ModcraftApplication.switchScene(1300, 700,  scene));
        });
    }
}
