package fr.modcraftmc.launcher.controllers;

import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.Utils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.net.URL;
import java.util.function.Supplier;

public class AuthenticationPopupController implements IController {
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML public Label url;
    @FXML public Button cancel;
    @FXML public Button copy;
    @FXML public Button open;

    private Supplier<URL> urlSupplier;
    private Supplier<Stage> popupSupplier;
    private Supplier<Runnable> authCanceler;

    public static AuthenticationPopupController show(URL url, Runnable authCanceler) {
        Scene scene = Utils.loadFxml("authenticationPopup.fxml", false);
        AuthenticationPopupController controller = (AuthenticationPopupController) scene.getUserData();
        controller.show(url, authCanceler, scene);
        return controller;
    }

    public void show(URL url, Runnable authCanceler, Scene scene) {
        this.urlSupplier = () -> url;
        this.authCanceler = () -> authCanceler;
        Stage stage = new Stage();
        this.popupSupplier = () -> stage;
        this.url.setText(url.toString());
        stage.initOwner(ModcraftApplication.getWindow());
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("Authentication Confirmation");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void initialize(FXMLLoader loader) {
        AnchorPane pane = loader.getRoot();
        pane.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        pane.setOnMouseDragged(event -> {
            popupSupplier.get().setX(event.getScreenX() - xOffset);
            popupSupplier.get().setY(event.getScreenY() - yOffset);
        });
        cancel.setOnAction(event -> {
            authCanceler.get().run();
            popupSupplier.get().close();
        });
        copy.setOnAction(event -> {
            Utils.copyToClipboard(urlSupplier.get());
        });
        open.setOnAction(event -> {
            Utils.openBrowser(urlSupplier.get());
        });
    }

    public void close() {
        popupSupplier.get().close();
    }
}
