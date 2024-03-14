package fr.modcraftmc.launcher;

import com.sun.javafx.application.HostServicesDelegate;
import fr.modcraftmc.libs.errors.ErrorsHandler;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.util.concurrent.CompletableFuture;

public class Utils {

    public static void copyToClipboard(String s) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(s);
        clipboard.setContent(content);
    }

    public static void openBrowser(String url) {
        HostServicesDelegate hostServices = HostServicesDelegate.getInstance(ModcraftApplication.app);
        hostServices.showDocument(url);
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

    public static void selfCatchSleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            ErrorsHandler.handleError(e);
        }
    }
}
