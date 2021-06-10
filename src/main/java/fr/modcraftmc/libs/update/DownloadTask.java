package fr.modcraftmc.libs.update;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DownloadTask extends Task<Void> {

    public final String serverUrl;
    public final File directory;
    public final ProgressBar progressBar;
    private final Label label;

    public static List<MDFile> remoteContent, toDownload = new ArrayList<>();
    public static List<String> ignoreList;

    private int  octetsDownloaded, fileDownloaded;
    private static DecimalFormat df2 = new DecimalFormat("#.##");

    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    public DownloadTask(String serverUrl, File directory, ProgressBar progressBar, Label label) {
        this.serverUrl = serverUrl;
        this.directory = directory;
        this.progressBar = progressBar;
        this.label = label;
    }

    double previousOctets;
    double speed;
    String speedStr = "calc..";
    @Override
    protected Void call() throws Exception {

        Platform.runLater(() -> {
            progressBar.progressProperty().unbind();
            progressBar.progressProperty().bind(this.progressProperty());
        });

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            previousOctets = octetsDownloaded;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            double speedBytes = octetsDownloaded - previousOctets;

            speed = (speedBytes) / 1000 / 1000;
            speedStr = df2.format(speed);

        }, 0, 1, TimeUnit.SECONDS);

        System.out.println(toDownload.size());

        toDownload.parallelStream().forEach(file -> {
            String path = file.getPath();
            String name = file.getName();
            File cursor = new File(new File(directory, path), name);

            System.out.println(cursor.getAbsolutePath());
            if (cursor.getParentFile().exists()) {
                if (!cursor.exists()) {
                    download(cursor, file);
                }
            } else {
                cursor.getParentFile().mkdirs();
                download(cursor, file);
            }
        });

        return null;
    }

    public void download(File cursor, MDFile obj) {

            String filePath = obj.getPath();
            String name = obj.getName();

            try {
                URL fileUrl;
                fileUrl = new URL(this.serverUrl + "/downloads/" + filePath + name);

                BufferedInputStream bis = new BufferedInputStream(fileUrl.openStream());
                FileOutputStream fos = new FileOutputStream(cursor);
                final byte[] data = new byte[4096];
                int count;

                while ((count = bis.read(data, 0, 32)) != -1) {
                    octetsDownloaded += count;
                    fos.write(data, 0, count);
                }

                bis.close();
                fos.flush();
                fos.close();
                fileDownloaded++;
                Platform.runLater(() -> label.setText(String.format("Téléchargement (%s/%s) (%s mo/s)", fileDownloaded, toDownload.size(), speedStr)));
                this.updateProgress(fileDownloaded, toDownload.size());

            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    protected void succeeded() {
        Platform.runLater(() -> label.setText("Téléchargement terminé"));
        super.succeeded();
    }
}
