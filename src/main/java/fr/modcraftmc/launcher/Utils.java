package fr.modcraftmc.launcher;

import com.sun.javafx.application.HostServicesDelegate;
import fr.modcraftmc.libs.errors.ErrorsHandler;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
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

    public static String getFileChecksum(MessageDigest digest, File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);

        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }

        fis.close();

        byte[] bytes = digest.digest();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }
}
