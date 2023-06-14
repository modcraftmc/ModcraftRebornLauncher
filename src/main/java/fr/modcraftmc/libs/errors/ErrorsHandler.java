package fr.modcraftmc.libs.errors;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public class ErrorsHandler {

    public static void handleError(Exception exception) {
        exception.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR, exception.getMessage());
        alert.setTitle("ModcraftMC");
        alert.setHeaderText("Une erreur est survenue");
        Platform.runLater(alert::show);
    }
}