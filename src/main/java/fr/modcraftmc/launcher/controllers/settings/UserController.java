package fr.modcraftmc.launcher.controllers.settings;

import fr.modcraftmc.launcher.MFXMLLoader;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.controllers.BaseController;
import fr.modcraftmc.libs.errors.ErrorsHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.raphimc.minecraftauth.step.java.StepMCProfile;

import java.io.IOException;
import java.net.URL;

public class UserController extends BaseController {

    @FXML
    private Label playerName;
    @FXML
    private Label playerRank;
    @FXML
    private ImageView playerHead;
    @FXML
    private Button logoutBtn;
    @FXML
    private CheckBox keepMicrosoftLogin;
    private StepMCProfile.MCProfile mcProfile;

    @Override
    public void initialize(FXMLLoader loader) {

        this.mcProfile = ModcraftApplication.accountManager.getCurrentMCProfile();

        logoutBtn.setOnMouseClicked((event) -> {
            ModcraftApplication.launcherConfig.setRefreshToken("");
            ModcraftApplication.launcherConfig.setKeeplogin(false);
            ModcraftApplication.launcherConfig.save();
            ModcraftApplication.getWindow().setScene(MFXMLLoader.loadFxml("login.fxml", true));
        });

        keepMicrosoftLogin.setSelected(ModcraftApplication.launcherConfig.isKeeplogin());
        keepMicrosoftLogin.setOnMouseClicked((event) -> {
            ModcraftApplication.launcherConfig.setKeeplogin(keepMicrosoftLogin.isSelected());
            ModcraftApplication.launcherConfig.save();
        });

        playerName.setText(mcProfile.getName());
        try {
            Image image = new Image(new URL("https://minotar.net/avatar/" + mcProfile.getName()).openStream(), 64, 64, false, false);
            playerHead.setImage(image);
        } catch (IOException e) {
            ErrorsHandler.handleError(e);
        }
    }
}
