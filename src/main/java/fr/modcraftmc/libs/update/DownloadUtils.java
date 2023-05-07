package fr.modcraftmc.libs.update;


import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.modcraftmc.libs.updater.MDFile;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DownloadUtils {

    private static Gson gson = new GsonBuilder().create();
    private static Type listType = new TypeToken<List<MDFile>>() {}.getType();

    public static List<MDFile> getRemoteContent(String url) throws IOException {
        System.out.println(url);
        String json = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
        //System.out.println(json);
        return gson.fromJson(json, listType);
    }

    public static List<String> getIgnoreList(String url) throws IOException {
        return Resources.readLines(new URL(url), Charsets.UTF_8);
    }
}
