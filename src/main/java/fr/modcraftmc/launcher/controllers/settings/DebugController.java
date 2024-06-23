package fr.modcraftmc.launcher.controllers.settings;

import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.controllers.BaseController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class DebugController extends BaseController {

    @FXML
    private Label buildType;
    @FXML
    private Label version;
    @FXML
    public Button logBtn;

    @Override
    public void initialize(FXMLLoader loader) {
        buildType.setText("Build Type: " + ModcraftApplication.ENVIRONMENT.name());
        version.setText("Build Time: " + ModcraftApplication.BUILD_TIME);

        logBtn.setOnMouseClicked((event) -> {
            File instance = ModcraftApplication.filesManager.getInstancesPath();
            if (ModcraftApplication.launcherConfig.getInstanceProperty().customInstance())
                instance = new File(ModcraftApplication.launcherConfig.getInstanceProperty().customInstancePath());

            File logdirectory = new File(instance, "logs");
            if (!logdirectory.exists())
                logdirectory.mkdirs();

            try {
                Desktop.getDesktop().open(logdirectory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
