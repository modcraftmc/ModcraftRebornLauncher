package fr.modcraftmc.launcher;

import fr.modcraftmc.launcher.controllers.IController;
import fr.modcraftmc.libs.errors.ErrorsHandler;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MFXMLLoader {
    private static final Map<String, Scene> loadedScenes = new HashMap<>();

    public static Scene loadFxml(String file, boolean forceReload) {
        if (loadedScenes.containsKey(file) && !forceReload) {
            return loadedScenes.get(file);
        }
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(ModcraftApplication.resourcesManager.getResource(file));
            AnchorPane pane = loader.load();
            IController controller = loader.getController();
            controller.initialize(loader);
            Scene scene = new Scene(pane);
            scene.setUserData(controller);
            loadedScenes.put(file, scene);
            return scene;
        } catch (Exception e) {
            ErrorsHandler.handleError(e);
        }
        throw new IllegalStateException("Error when loading a fxml scene");
    }

    public static Pane loadPane(String fxml) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(ModcraftApplication.resourcesManager.getResource(fxml));
            AnchorPane pane = loader.load();
            IController controller = loader.getController();
            pane.setUserData(controller);
            controller.initialize(loader);
            return pane;
        } catch (IOException e) {
            ErrorsHandler.handleError(e);
        }
        throw new IllegalStateException("Error when loading a fxml pane");
    }
}
