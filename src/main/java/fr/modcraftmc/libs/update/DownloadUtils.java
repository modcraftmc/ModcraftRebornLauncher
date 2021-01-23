package fr.modcraftmc.libs.update;


import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.scene.control.Label;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DownloadUtils {

    private static Gson gson = new Gson();
    private static Type listType = new TypeToken<ArrayList<MDFile>>() {}.getType();

    public static List<MDFile> getRemoteContent(String url, Label label) {
        Platform.runLater(() -> label.setText("Récuperation du fichier manifest"));

        try (InputStreamReader streamReader = new InputStreamReader(new URL(url).openStream())) {

            return gson.fromJson(streamReader, listType);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<String> getIgnoreList(String stdurl) {

        try {
            URL url = new URL(stdurl);
            List<String> lines = Resources.readLines(url, Charsets.UTF_8);
            System.out.println(lines);
            return lines;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}
