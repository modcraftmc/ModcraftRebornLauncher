package fr.modcraftmc.libs.news;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.modcraftmc.launcher.MFXMLLoader;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.Utils;
import fr.modcraftmc.launcher.controllers.NewsContainerController;
import javafx.scene.layout.Pane;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.io.IOUtils;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class NewsManager {

    private final Gson GSON = new GsonBuilder().create();
    private final Type listType = new TypeToken<List<News>>() {}.getType();
    private NewsUpdateCallback newsUpdateCallback;

    public void fetchNews() {

        ModcraftApplication.LOGGER.info("Fetching news asynchronously ");
        try {
            //TODO: define api endpoints in modcraft api client ?
            // like ModcraftApiClient.instance().getNews()
            URL newsUrl = new URI(ModcraftApplication.ENVIRONMENT.getApiUrl() + "/news").toURL();
            String content = IOUtils.toString(newsUrl, StandardCharsets.UTF_8);
            List<News> newsList = GSON.fromJson(content, listType);

            ModcraftApplication.LOGGER.info("found " + newsList.size() + " news");

            ModcraftApplication.LOGGER.info("building news containers for " + newsList.size() + " news");
            List<Pane> buildedNewsContainers = Lists.newArrayList();
            for (News news : newsList) {
                Pane newsPane = MFXMLLoader.loadPane("news_container.fxml");
                ((NewsContainerController) newsPane.getUserData()).setup(news);
                buildedNewsContainers.add(newsPane);
            }
            Utils.ensureFxThread(() -> newsUpdateCallback.onUpdate(buildedNewsContainers));
        } catch (Exception e) {
            ModcraftApplication.LOGGER.warning(e.getMessage());
        }
    }

    public void onNewsUpdateCallback(NewsUpdateCallback callback) {
        this.newsUpdateCallback = callback;
    }

    public interface NewsUpdateCallback {
        void onUpdate(List<Pane> newsList);
    }
}


