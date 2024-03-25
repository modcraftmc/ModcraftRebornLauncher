package fr.modcraftmc.libs.errors;

import fr.modcraftmc.launcher.ModcraftApplication;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class ErrorsHandler {

    //TODO: use popup builder
    public static void handleErrorAndCrashApplication(Exception exception) {
        ModcraftApplication.LOGGER.severe("Error thrown ! : " + exception.getMessage());
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, exception.getMessage());
            alert.setTitle("ModcraftMC");
            alert.setHeaderText("Une erreur est survenue");
            alert.showAndWait();
            ModcraftApplication.shutdown(1);
        });
    }

    public static void handleErrorWithCustomHeaderAndCrashApplication(String header, Exception exception) {
        ModcraftApplication.LOGGER.severe("Error thrown ! : " + exception.getMessage());
        exception.printStackTrace();
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, exception.getMessage());
            alert.setTitle("ModcraftMC");
            alert.setHeaderText(header);
            alert.showAndWait();
            ModcraftApplication.shutdown(1);
        });
    }

    public static void handleError(Exception exception) {
        ModcraftApplication.LOGGER.severe("Error thrown ! : " + exception.getMessage());
        exception.printStackTrace();
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, exception.getMessage());
            alert.setTitle("ModcraftMC");
            alert.setHeaderText("Une erreur est survenue");
            alert.show();
        });
    }

    public static void handleErrorWithCustomHeader(String header, Exception exception) {
        ModcraftApplication.LOGGER.severe("Error thrown ! : " + exception.getMessage());
        exception.printStackTrace();
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, exception.getMessage());
            alert.setTitle("ModcraftMC");
            alert.setHeaderText(header);
            alert.show();
        });
    }
}
