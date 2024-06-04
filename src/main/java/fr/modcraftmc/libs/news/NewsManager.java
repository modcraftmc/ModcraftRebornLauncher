package fr.modcraftmc.libs.news;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.modcraftmc.launcher.MFXMLLoader;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.controllers.NewsContainerController;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import org.apache.commons.compress.utils.Lists;
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

        ModcraftApplication.LOGGER.info("Fetching news asynchronously ");
        try {
            String content = FileUtils.readFileToString(new File(ModcraftApplication.resourcesManager.getResource("news.json").getPath()), "UTF-8");
            List<News> newsList = GSON.fromJson(content, listType);

            ModcraftApplication.LOGGER.info("found " + newsList.size() + " news");

            ModcraftApplication.LOGGER.info("building news containers for " + newsList.size() + " news");
            List<Pane> buildedNewsContainers = Lists.newArrayList();
            for (News news : newsList) {
                Pane newsPane = MFXMLLoader.loadPane("news_container.fxml");
                ((NewsContainerController) newsPane.getUserData()).setup(news);
                buildedNewsContainers.add(newsPane);
            }
            Platform.runLater(() -> newsUpdateCallback.onUpdate(buildedNewsContainers));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void onNewsUpdateCallback(NewsUpdateCallback callback) {
        this.newsUpdateCallback = callback;
    }

    public interface NewsUpdateCallback {
        void onUpdate(List<Pane> newsList);
    }
}


