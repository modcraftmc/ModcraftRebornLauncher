package fr.modcraftmc.launcher.controllers;

import fr.modcraftmc.libs.news.News;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;

public class NewsContainerController extends BaseController {

    @FXML private Label title;
    @FXML private Label content;
    @FXML private Label publishDate;

    @Override
    public void initialize(FXMLLoader loader) {
        //super.initialize(loader);
    }

    public void setup(News news) {
        this.title.setText(news.title());
        this.content.setText(news.content());
        this.publishDate.setText(news.publish_date());
    }
}
