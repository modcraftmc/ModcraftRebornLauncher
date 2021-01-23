package fr.modcraftmc.launcher;

import fr.modcraftmc.launcher.controllers.IController;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class Utils {


    private static double xOffset = 0;
    private static double yOffset = 0;
    public static Pane loadFxml(String file) {

        ModcraftApplication.LOGGER.info("Loading fxml file  " + file);

        try {
            FXMLLoader loader = new FXMLLoader(ModcraftApplication.resourcesManager.getResource(file));
            Pane pane = loader.load();

            IController controller = loader.getController();
            controller.initialize();
            pane.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            pane.setOnMouseDragged(event -> {
                ModcraftApplication.getWindow().setX(event.getScreenX() - xOffset);
                ModcraftApplication.getWindow().setY(event.getScreenY() - yOffset);
            });

            return pane;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Pane();
    }
}
