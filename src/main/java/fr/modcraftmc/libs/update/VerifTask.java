package fr.modcraftmc.libs.update;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class VerifTask extends Task<Void> {

    public final String serverUrl;
    public final File directory;
    public final ProgressBar progressBar;
    private final Label label;
    private AtomicInteger fileAnalyzed = new AtomicInteger();


    public VerifTask(String url, File gameDir, ProgressBar progressBar, Label label) {
        this.serverUrl = url;
        this.directory = gameDir;
        this.progressBar = progressBar;
        this.label = label;

        DownloadTask.remoteContent = DownloadUtils.getRemoteContent(serverUrl + "/content.json", label);
        DownloadTask.ignoreList = DownloadUtils.getIgnoreList(serverUrl + "/ignore.txt");
    }

    public void checkLocalFiles() {
        Collection<File> localFiles = new ConcurrentLinkedQueue<>(FileUtils.listFiles(directory, null, true));

        CountDownLatch latch = new CountDownLatch(DownloadTask.remoteContent.size());
        ExecutorService taskExecutor = Executors.newFixedThreadPool(20);

        for (MDFile mdFile : DownloadTask.remoteContent) {
            taskExecutor.submit(() -> {
                File lFile = new File(new File(directory, mdFile.getPath()), mdFile.getName());

                try {
                    if (lFile.exists()) {
                        String md5 = getFileChecksum(MessageDigest.getInstance("MD5"), lFile);
                        if (!md5.equalsIgnoreCase(mdFile.getMd5())) {
                            lFile.delete();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                localFiles.remove(lFile);
                int current = fileAnalyzed.incrementAndGet();
                this.updateProgress(current, DownloadTask.remoteContent.size());
                Platform.runLater(() -> label.setText(String.format("Analyse des fichiers (%s/%s)", current, DownloadTask.remoteContent.size())));
                latch.countDown();
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        localFiles.parallelStream().forEach(file -> {

            boolean ignore = false;
            for (String now : DownloadTask.ignoreList) {
                if (file.toString().contains(now.replace("/", "\\"))) {
                    GameUpdater.LOGGER.info("[IGNORE LIST] This file is ignored: " + file.getName());
                    ignore = true;
                }
            }

            if (!ignore)
                file.delete();
        });
    }

    private static String getFileChecksum(MessageDigest digest, File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            byte[] byteArray = new byte[1024];
            int bytesCount = 0;

            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }

            fis.close();

            byte[] bytes = digest.digest();

            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void checkForUpdate() {
        DownloadTask.remoteContent.forEach((file) -> {
            try {
                String path = file.getPath();
                String name = file.getName();

                File cursor = new File(new File(directory, path), name);
                if (!cursor.exists()) {
                    DownloadTask.toDownload.add(file);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        this.updateProgress(100, 100);
    }

    @Override
    protected Void call() throws Exception {
        checkLocalFiles();
        checkForUpdate();
        return null;
    }
}
