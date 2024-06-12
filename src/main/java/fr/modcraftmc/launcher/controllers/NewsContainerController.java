package fr.modcraftmc.launcher.controllers;

import fr.modcraftmc.launcher.Utils;
import fr.modcraftmc.libs.news.News;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class NewsContainerController extends BaseController {

    @FXML
    private Pane container;
    @FXML
    private Label title;
    @FXML
    private Label content;
    @FXML
    private Label publishDate;

    @Override
    public void initialize(FXMLLoader loader) {
        container.getChildren().forEach((a) -> {
            a.setOnMouseClicked(mouseEvent -> {
                Utils.openBrowser("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
            });
        });
    }

    public void setup(News news) {
        this.title.setText(news.title());
        this.content.setText(news.content());
        this.publishDate.setText(news.publish_date());
    }
}
