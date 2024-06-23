package fr.modcraftmc.launcher.controllers;

import fr.modcraftmc.launcher.Utils;
import fr.modcraftmc.libs.news.News;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class NewsContainerController extends BaseController {

    @FXML
    private Pane container;
    @FXML
    private Label title;
    @FXML
    private Label content;
    @FXML
    private Label publishDate;

    private String urlAccess;

    @Override
    public void initialize(FXMLLoader loader) {
        this.container.setOnMouseClicked(event -> {
            Utils.openBrowser(urlAccess);
        });
    }

    public void setup(News news) {
        this.title.setText(news.title());
        this.content.setText(news.description());
        try {
            Instant instant = Instant.parse(news.datePublished());
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy 'à' HH'h'mm");
            this.publishDate.setText(dateTimeFormat.format(Date.from(instant)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.urlAccess = news.urlAccess();
    }
}
