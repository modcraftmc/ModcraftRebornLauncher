package fr.modcraftmc.libs.update;

import fr.modcraftmc.launcher.logger.LogManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.File;
import java.util.logging.Logger;

public class GameUpdater {

    private final String url;
    private final File gameDir;
    private final ProgressBar progressBar;
    private final Label label;

    private Task<Void> task;
    public static Thread update;

    public static Logger LOGGER = LogManager.createLogger("Updater");

    public static Thread.UncaughtExceptionHandler exceptionHandler = (t, e) -> {
        //TODO: CRASH REPORTTER
        e.printStackTrace();
        //t.interrupt();
    };

    public GameUpdater(String url, File gameDir, ProgressBar bar, Label label){
        this.url = url;
        this.gameDir = gameDir;
        this.progressBar = bar;
        this.label = label;
    }

    public Thread start(){

            update.start();
            return update;

    }

    public Task getUpdater() {
        task = new DownloadTask(url, gameDir, progressBar, label);
        Task verif = new VerifTask(url, gameDir, progressBar, label);

        verif.setOnSucceeded((e) -> new Thread(task).start());
        System.out.println(gameDir.toPath());
        gameDir.mkdirs();

        if (progressBar != null) {
            Platform.runLater(() -> {
                progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
                progressBar.progressProperty().unbind();
                progressBar.progressProperty().bind(verif.progressProperty());
            });
        }

        update = new Thread(verif);
        update.setDaemon(true);
        update.setUncaughtExceptionHandler(exceptionHandler);
        return task;
    }
}
