package fr.modcraftmc.launcher.controllers;

import fr.modcraftmc.launcher.MFXMLLoader;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class SettingsController extends BaseController {

    private final int BTN_HEIGHT = 50;
    private final int BTN_SPACING = 15;
    @FXML
    Pane userBtn;
    @FXML
    Pane gameBtn;
    @FXML
    Pane modsBtn;
    @FXML
    Pane discordBtn;
    @FXML
    Pane debugBtn;
    @FXML
    private Pane settingsContainer;
    private Pane currentPane;
    @FXML
    private Rectangle activeTab;

    @Override
    public void initialize(FXMLLoader loader) {
        Pane userPane = MFXMLLoader.loadPane("settings/user.fxml");
        settingsContainer.getChildren().add(userPane);
        userPane.setVisible(true);
        currentPane = userPane;

        userBtn.setOnMouseClicked((event) -> {
            switchSettingPage(userPane, 0);
        });

        Pane gamePane = MFXMLLoader.loadPane("settings/game.fxml");
        settingsContainer.getChildren().add(gamePane);
        gamePane.setVisible(false);
        gamePane.setDisable(true);

        gameBtn.setOnMouseClicked((event) -> {
            switchSettingPage(gamePane, 1);
        });

        Pane modsPane = MFXMLLoader.loadPane("settings/not_implemented.fxml");
        settingsContainer.getChildren().add(modsPane);
        modsPane.setVisible(false);
        modsPane.setDisable(true);

        modsBtn.setOnMouseClicked((event) -> {
            switchSettingPage(modsPane, 2);
        });

        Pane discordPane = MFXMLLoader.loadPane("settings/not_implemented.fxml");
        settingsContainer.getChildren().add(discordPane);
        discordPane.setVisible(false);
        discordPane.setDisable(true);

        discordBtn.setOnMouseClicked((event) -> {
            switchSettingPage(discordPane, 3);
        });

        Pane debugPane = MFXMLLoader.loadPane("settings/debug.fxml");
        settingsContainer.getChildren().add(debugPane);
        debugPane.setVisible(false);
        debugPane.setDisable(true);

        debugBtn.setOnMouseClicked((event) -> {
            switchSettingPage(debugPane, 4);
        });
    }

    private void switchSettingPage(Pane newPage, int index) {
        currentPane.setVisible(false);
        currentPane.setDisable(true);

        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(100), activeTab);
        translateTransition.setToY(index * (BTN_SPACING + BTN_HEIGHT));
        translateTransition.play();

        currentPane = newPage;
        newPage.setVisible(true);
        newPage.setDisable(false);
    }
}
