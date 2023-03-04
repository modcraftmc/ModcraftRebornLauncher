package fr.modcraftmc.launcher.controllers;

//import com.jfoenix.controls.JFXButton;
import fr.modcraftmc.libs.auth.AccountManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ChoseLoginController implements IController {

    @FXML public Label accounttype;
    @FXML public Button microsoftBtn;
    @FXML public Button mojangBtn;
    @FXML public Button modcraftBtn;

    private AccountManager.LoginType loginType;

    @Override
    public void initialize() {

        accounttype.setText("Choisisez votre mode de connexion.");

        microsoftBtn.setOnMouseClicked((event) -> {
            loginType = AccountManager.LoginType.MICROSOFT;
        });

        mojangBtn.setOnMouseClicked((event) -> {
            loginType = AccountManager.LoginType.MOJANG;
        });

        modcraftBtn.setOnMouseClicked((event) -> {
            loginType = AccountManager.LoginType.MICROSOFT;
        });

    }
}
