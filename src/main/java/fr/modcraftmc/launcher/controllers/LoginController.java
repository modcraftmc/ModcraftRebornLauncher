package fr.modcraftmc.launcher.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.Utils;
import fr.modcraftmc.launcher.components.TranslateTransition;
import fr.modcraftmc.launcher.configuration.InstanceProperty;
import fr.modcraftmc.libs.auth.AccountManager;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginController implements IController {

    private Pattern emailPattern = Pattern.compile("^(.+)@(.+)$");



    //login

    @FXML public Pane logincontainer;

    @FXML public Label serverdesc;
    @FXML public Label accounttype;

    @FXML public JFXTextField emailfield;
    @FXML public JFXPasswordField passwordfield;
    @FXML public JFXCheckBox logincheckbox;
    @FXML public JFXButton loginBtn;

    @FXML public Button closeBtn;


    @Override
    public void initialize() {

        if (ModcraftApplication.launcherConfig.isKeeplogin()) {
            try {
                String accessToken = ModcraftApplication.launcherConfig.getAccesToken();

                if (AccountManager.tryVerify(accessToken)) {
                    processToMainPanel();
                    return;
                }

            } catch (Exception ignored) {}
        }

        serverdesc.setText("Serveur skyblock moddé 1.16.2");
        accounttype.setText("Connectez-vous via votre compte mojang ou modcraftmc.fr");

        emailfield.setPromptText("Email");
        passwordfield.setPromptText("Mot de passe");
        logincheckbox.setText("Se souvenir de moi");
        loginBtn.setText("SE CONNECTER");

            //login action
        passwordfield.setOnKeyReleased((event -> {
            if (event.getCode().name().equalsIgnoreCase("ENTER")) login();
        }));

        loginBtn.setOnMouseClicked((event) -> login());

            //window action
        closeBtn.setOnMouseClicked(event -> {
            ModcraftApplication.getWindow().hide();
            ModcraftApplication.launcherConfig.save();
            System.exit(0);
        });
    }


    public void login() {

        loginBtn.setText("CONNECTION...");
        Matcher matcher = emailPattern.matcher(emailfield.getText());

        if (matcher.matches()) {
            ModcraftApplication.LOGGER.info("Email is valid.");

            if (passwordfield.getText().isEmpty() || passwordfield.getText().length() < 6) {
                loginBtn.setText("SE CONNECTER");
                //throw
            }

            ModcraftApplication.LOGGER.info("Password is valid.");


            try {

                CompletableFuture<Boolean> loginProcess = CompletableFuture.supplyAsync(() -> AccountManager.tryLogin(emailfield.getText(), passwordfield.getText()));

                if (loginProcess.get()) {

                    ModcraftApplication.launcherConfig.setKeeplogin(logincheckbox.isSelected());
                    ModcraftApplication.launcherConfig.save();
                    processToMainPanel();

                }

            }  catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

        } else {
            //throw
            ModcraftApplication.LOGGER.info("Email is invalid.");
            loginBtn.setText("SE CONNECTER");
        }
    }

    public void processToMainPanel() {

        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(1500), logincontainer);

        translateTransition.setOnFinished((event -> {
            ModcraftApplication.getWindow().setScene(Utils.loadFxml("main.fxml"));
        }));

        for (Node child : logincontainer.getChildren()) {
            FadeTransition transition = new FadeTransition(Duration.millis(100), child);
            transition.setFromValue(1);
            transition.setToValue(0);
            transition.play();
        }

        translateTransition.play();
    }
}
