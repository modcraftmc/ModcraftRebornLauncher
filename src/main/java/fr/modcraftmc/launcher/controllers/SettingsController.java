package fr.modcraftmc.launcher.controllers;

import fr.modcraftmc.launcher.MFXMLLoader;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class SettingsController extends BaseController {

    @FXML
    private Pane settingsContainer;
    private Pane currentPane;

    @FXML Pane userBtn;
    @FXML Pane gameBtn;
    @FXML Pane debugBtn;

    @FXML private Rectangle activeTab;
    private int BTN_HEIGHT = 50;
    private int BTN_SPACING = 15;

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

        gameBtn.setOnMouseClicked((event) -> {
            switchSettingPage(gamePane, 1);
        });

        Pane debugPane = MFXMLLoader.loadPane("settings/debug.fxml");
        settingsContainer.getChildren().add(debugPane);
        debugPane.setVisible(false);

        debugBtn.setOnMouseClicked((event) -> {
            switchSettingPage(debugPane, 4);
        });
    }

    private void switchSettingPage(Pane newPage, int index) {
        currentPane.setVisible(false);
        currentPane.setDisable(true);

        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(100), activeTab);
        translateTransition.setToY((index * BTN_HEIGHT) + (index * BTN_SPACING));
        translateTransition.play();

        currentPane = newPage;
        newPage.setVisible(true);
        newPage.setDisable(false);
    }
}
