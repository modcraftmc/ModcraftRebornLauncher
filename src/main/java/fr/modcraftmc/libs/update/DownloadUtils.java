package fr.modcraftmc.libs.update;


import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fr.modcraftmc.libs.updater.MDFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DownloadUtils {

    private static Gson gson = new Gson();
    private static Type listType = new TypeToken<ArrayList<MDFileOld>>() {}.getType();

    public static List<MDFile> getRemoteContent(String url) throws IOException {
        return gson.fromJson(new InputStreamReader(new URL(url).openStream()), listType);
    }

    public static List<String> getIgnoreList(String url) throws IOException {
        return Resources.readLines(new URL(url), Charsets.UTF_8);
    }
}
