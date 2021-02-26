package fr.modcraftmc.launcher;

import fr.modcraftmc.launcher.controllers.IController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Utils {


    private static double xOffset = 0;
    private static double yOffset = 0;

    private static Map<String, Scene> loadedScenes = new HashMap<>();
    public static Scene loadFxml(String file, boolean forceReload) {

        if (loadedScenes.containsKey(file)&& !forceReload) {
            return loadedScenes.get(file);
        }

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
            Scene scene = new Scene(pane);
            scene.setUserData(controller);
            loadedScenes.put(file, scene);
            return scene;

        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new RuntimeException();
    }
}
