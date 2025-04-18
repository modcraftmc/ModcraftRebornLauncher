package fr.modcraftmc.launcher.controllers;

import fr.modcraftmc.launcher.*;
import fr.modcraftmc.launcher.startup.tasks.ValidateModcaftUserTask;
import fr.modcraftmc.libs.auth.AccountManager;
import io.github.palexdev.materialfx.controls.MFXProgressSpinner;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class LoginController extends BaseController {

    @FXML
    public Pane welcomeContainer;
    @FXML
    public Pane loadingContainer;
    @FXML
    public Pane microsoftButton;
    @FXML
    private Label loadingMessage;
    @FXML
    private MFXProgressSpinner loadingIndicator;
    @FXML
    private Label microsoftText;


    @Override
    public void initialize(FXMLLoader loader) throws Exception {
        super.initialize(loader);
        loadingIndicator.setVisible(false);
        loadingMessage.setText("");

        microsoftButton.setOnMouseClicked(event -> {
            microsoftButton.setDisable(true);

            loadingIndicator.setVisible(true);
            microsoftText.setVisible(false);
            loadingMessage.setText("En attente de la connexion...");

            AccountManager.authenticate().thenAcceptAsync(authResult -> {
                if (authResult.isLoggedIn()) {
                    loadingMessage.setText("Connecté!");

                    Utils.pleaseWait(2000).thenAcceptAsync((unused) -> {
                        microsoftButton.setDisable(false);
                        ModcraftApplication.accountManager.setCurrentMCProfile(authResult.getMcProfile());
                        ModcraftApplication.launcherConfig.setKeeplogin(true);
                        ModcraftApplication.LOGGER.info(authResult.getMcProfile().getName());

                        ModcraftApplication.accountManager.setModcraftServiceUserProfile(ValidateModcaftUserTask.execute(authResult.getMcProfile()));

                        Scene scene = MFXMLLoader.loadFxml(Constants.MAIN_FXML, true);
                        ModcraftApplication.switchScene(-1, -1, scene);
                    }, AsyncExecutor::runAsync);
                    return;
                }

                microsoftButton.setDisable(false);
                loadingIndicator.setVisible(false);
                microsoftText.setVisible(true);
                loadingMessage.setText("");

            }, Utils::ensureFxThread);
        });
    }
}
