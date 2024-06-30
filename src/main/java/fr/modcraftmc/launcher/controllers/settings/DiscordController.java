package fr.modcraftmc.launcher.controllers.settings;

import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.controllers.BaseController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;

public class DiscordController extends BaseController {

    @FXML
    private CheckBox discordActivityEnabled;


    @Override
    public void initialize(FXMLLoader loader) {
        this.discordActivityEnabled.setSelected(ModcraftApplication.launcherConfig.isDiscordActivityEnabled());
        this.discordActivityEnabled.setOnMouseClicked(event -> {
            ModcraftApplication.launcherConfig.setDiscordActivityEnabled(discordActivityEnabled.isSelected());
            discordActivityEnabled.setText("Activer l'activitée de jeu Discord (appliqué au redémarrage du launcher)");
        });
    }
}
