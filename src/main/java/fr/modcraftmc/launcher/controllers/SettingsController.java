package fr.modcraftmc.launcher.controllers;

import fr.modcraftmc.launcher.MFXMLLoader;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

public class SettingsController extends BaseController {

    @FXML
    private Pane settingsContainer;
    private Pane currentPane;

    @FXML Pane userBtn;
    @FXML Pane gameBtn;
    @FXML Pane debugBtn;

    @Override
    public void initialize(FXMLLoader loader) {
        Pane userPane = MFXMLLoader.loadPane("settings/user.fxml");
        settingsContainer.getChildren().add(userPane);
        userPane.setVisible(true);
        currentPane = userPane;

        userBtn.setOnMouseClicked((event) -> {
            switchSettingPage(userPane);
        });

        Pane gamePane = MFXMLLoader.loadPane("settings/game.fxml");
        settingsContainer.getChildren().add(gamePane);
        gamePane.setVisible(false);

        gameBtn.setOnMouseClicked((event) -> {
            switchSettingPage(gamePane);
        });

        Pane debugPane = MFXMLLoader.loadPane("settings/debug.fxml");
        settingsContainer.getChildren().add(debugPane);
        debugPane.setVisible(false);

        debugBtn.setOnMouseClicked((event) -> {
            switchSettingPage(debugPane);
        });
    }

    private void switchSettingPage(Pane newPage) {
        currentPane.setVisible(false);
        currentPane.setDisable(true);

        currentPane = newPage;
        newPage.setVisible(true);
        newPage.setDisable(false);
    }
}
