package fr.modcraftmc.launcher.controllers.settings;

import fr.modcraftmc.api.ModcraftApiClient;
import fr.modcraftmc.launcher.AsyncExecutor;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.Utils;
import fr.modcraftmc.launcher.controllers.BaseController;
import fr.modcraftmc.libs.api.ModcraftServiceUserProfile;
import fr.modcraftmc.libs.popup.PopupBuilder;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;

public class DebugController extends BaseController {

    @FXML
    private Label buildType;
    @FXML
    private Label version;
    @FXML
    public Button logBtn;
    @FXML
    public Button devApiBtn;
    @Override
    public void initialize(FXMLLoader loader) {
        buildType.setText("Build Type: " + ModcraftApplication.ENVIRONMENT.getEnv());
        version.setText("Build Time: " + ModcraftApplication.BUILD_TIME);

        logBtn.setOnMouseClicked((event) -> {
            AsyncExecutor.runAsync(() -> {
                Path logdirectory = ModcraftApplication.gameInstanceManager.getActiveInstancePath().resolve("logs");
                if (!logdirectory.toFile().exists()) {}
                    logdirectory.toFile().mkdirs();

                try {
                    Desktop.getDesktop().open(logdirectory.toFile());
                } catch (IOException e) {
                    ModcraftApplication.LOGGER.severe(e.getMessage());
                }
            });
        });

        if (ModcraftApplication.accountManager.getModcraftServiceUserProfile().getServiceUserProfile().info.role().name().equals("default"))
            devApiBtn.setVisible(false);

        devApiBtn.setOnMouseClicked((event) -> {
            ModcraftApplication.forceDevApi = true;
            Alert alert = new PopupBuilder()
                    .setHeader("Attention")
                    .setText("API DEV")
                    .build();
            Utils.ensureFxThread(alert::showAndWait);
            ModcraftApplication.apiClient = new ModcraftApiClient("https://api.dev.modcraftmc.fr/v1");
            try {
                ModcraftServiceUserProfile modcraftUser = ModcraftServiceUserProfile.getProfile(ModcraftApplication.accountManager.getCurrentMCProfile().getMcToken().getAccessToken());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
