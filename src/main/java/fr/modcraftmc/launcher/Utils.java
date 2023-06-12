package fr.modcraftmc.launcher;

import fr.modcraftmc.launcher.controllers.IController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

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

    public static boolean checkAccount() {
        AtomicBoolean returnValue = new AtomicBoolean(false);
        if (ModcraftApplication.launcherConfig.isKeeplogin()) {
            //AccountManager.tryVerify(ModcraftApplication.launcherConfig.getRefreshToken()).thenAccept(returnValue::set);
        }

        return returnValue.get();
    }

    public static void copyToClipboard(String s) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(s);
        clipboard.setContent(content);
    }

    public static void copyToClipboard(URL url) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putUrl(url.toString());
        clipboard.setContent(content);
    }

    public static void openBrowser(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
