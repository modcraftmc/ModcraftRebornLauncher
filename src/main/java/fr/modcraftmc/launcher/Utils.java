package fr.modcraftmc.launcher;

import com.sun.javafx.geom.Vec2d;
import fr.modcraftmc.launcher.controllers.IController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Utils {

    private static Map<String, Scene> loadedScenes = new HashMap<>();
    public static Scene loadFxml(String file, boolean forceReload) {

        if (loadedScenes.containsKey(file)&& !forceReload) {
            return loadedScenes.get(file);
        }

        try {
            FXMLLoader loader = new FXMLLoader(ModcraftApplication.resourcesManager.getResource(file));
            AnchorPane pane = loader.load();

            IController controller = loader.getController();
            controller.initialize(loader);
            Scene scene = new Scene(pane);
            scene.setUserData(controller);
            loadedScenes.put(file, scene);
            return scene;

        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new RuntimeException();
    }

    public static void copyToClipboard(String s) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(s);
        clipboard.setContent(content);
    }

    public static InputStream catchForbidden(@NotNull URL url) throws IOException
    {
        final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.addRequestProperty("User-Agent", "Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.124 Safari/537.36");
        connection.setInstanceFollowRedirects(true);
        return connection.getInputStream();
    }

    public static void openBrowser(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static CompletableFuture<Void> pleaseWait(int millis) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    public static double magnitude(Vec2d vector) {
        return Math.sqrt(vector.x * vector.x + vector.y * vector.y);
    }

    public static double dot(Vec2d vector, Vec2d axis) {
        return vector.x * axis.x + vector.y * axis.y;
    }

    public static double project(Vec2d vector, Vec2d axis) {
        double axisMagnitude = magnitude(axis);
        if (axisMagnitude == 0) {
            return 0;
        }
        return dot(vector, axis) / axisMagnitude;
    }

    public static Vec2d normalize(Vec2d vector) {
        double vectorMagnitude = magnitude(vector);
        if (vectorMagnitude == 0) {
            return new Vec2d(0, 0);
        }
        return new Vec2d(vector.x / vectorMagnitude, vector.y / vectorMagnitude);
    }

    public static Vec2d projectVector(Vec2d vector, Vec2d axis) {
        Vec2d normalizedAxis = normalize(axis);
        double projection = project(vector, axis);
        return new Vec2d(projection * normalizedAxis.x, projection * normalizedAxis.y);
    }
}
