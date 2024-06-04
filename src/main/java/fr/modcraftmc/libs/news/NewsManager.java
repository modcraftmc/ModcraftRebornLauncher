package fr.modcraftmc.libs.news;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.modcraftmc.launcher.ModcraftApplication;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class NewsManager {

    private final Gson GSON = new GsonBuilder().create();
    private Type listType = new TypeToken<List<News>>() {}.getType();
    private NewsUpdateCallback newsUpdateCallback;

    public void fetchNews() {

        ModcraftApplication.LOGGER.info("fetchNews");
        try {
            String content = FileUtils.readFileToString(new File(ModcraftApplication.resourcesManager.getResource("news.json").getPath()), "UTF-8");
            List<News> news = GSON.fromJson(content, listType);

            ModcraftApplication.LOGGER.info("found " + news.size() + " news");
            newsUpdateCallback.onUpdate(news);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onNewsUpdateCallback(NewsUpdateCallback callback) {
        this.newsUpdateCallback = callback;
    }

    public interface NewsUpdateCallback {
        void onUpdate(List<News> newsList);
    }
}


