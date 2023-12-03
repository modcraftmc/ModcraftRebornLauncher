package fr.modcraftmc.libs.errors;

import fr.modcraftmc.launcher.ModcraftApplication;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class ErrorsHandler {

    public static void handleErrorAndCrashApplication(Exception exception) {
        ModcraftApplication.LOGGER.severe("Error thrown ! : " + exception.getMessage());
        exception.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR, exception.getMessage());
        alert.setTitle("ModcraftMC");
        alert.setHeaderText("Une erreur est survenue");
        Platform.runLater(() -> {
            alert.showAndWait();
            Platform.exit();
        });
    }

    public static void handleErrorWithCustomHeaderAndCrashApplication(String header, Exception exception) {
        ModcraftApplication.LOGGER.severe("Error thrown ! : " + exception.getMessage());
        exception.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR, exception.getMessage());
        alert.setTitle("ModcraftMC");
        alert.setHeaderText(header);
        Platform.runLater(() -> {
            alert.showAndWait();
            Platform.exit();
        });
    }

    public static void handleError(Exception exception) {
        ModcraftApplication.LOGGER.severe("Error thrown ! : " + exception.getMessage());
        Alert alert = new Alert(Alert.AlertType.ERROR, exception.getMessage());
        alert.setTitle("ModcraftMC");
        alert.setHeaderText("Une erreur est survenue");
        Platform.runLater(alert::show);
    }

    public static void handleErrorWithCustomHeader(String header, Exception exception) {
        ModcraftApplication.LOGGER.severe("Error thrown ! : " + exception.getMessage());
        Alert alert = new Alert(Alert.AlertType.ERROR, exception.getMessage());
        alert.setTitle("ModcraftMC");
        alert.setHeaderText(header);
        Platform.runLater(alert::show);
    }
}
