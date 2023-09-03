package fr.modcraftmc.libs.errors;

import fr.modcraftmc.launcher.ModcraftApplication;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class ErrorsHandler {

    public static void handleError(Exception exception) {
        ModcraftApplication.LOGGER.severe("Error thrown ! : " + exception.getMessage());
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, exception.getMessage());
            alert.setTitle("ModcraftMC");
            alert.setHeaderText("Une erreur est survenue");
            alert.showAndWait();
            Platform.exit();
        });
    }

    public static void handleErrorWithCustomHeader(String header, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR, exception.getMessage());
        alert.setTitle("ModcraftMC");
        alert.setHeaderText(header);
        Platform.runLater(alert::show);
    }
}
