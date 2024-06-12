package fr.modcraftmc.launcher.controllers.settings;

import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.controllers.BaseController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;

public class DebugController extends BaseController {

    @FXML
    private Label buildType;
    @FXML
    private Label version;

    @Override
    public void initialize(FXMLLoader loader) {
        buildType.setText("Build Type: " + ModcraftApplication.ENVIRONMENT.name());
        version.setText("Build Time: " + ModcraftApplication.BUILD_TIME);
    }
}
