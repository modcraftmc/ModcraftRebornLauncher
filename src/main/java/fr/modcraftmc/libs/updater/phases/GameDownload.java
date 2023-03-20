package fr.modcraftmc.libs.updater.phases;

import fr.modcraftmc.libs.update.DownloadTask;
import fr.modcraftmc.libs.update.GameUpdaterOld;
import fr.modcraftmc.libs.update.MDFileOld;
import fr.modcraftmc.libs.updater.GameUpdater;
import fr.modcraftmc.libs.updater.MDFile;
import fr.modcraftmc.libs.updater.UpdateResult;
import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class GameDownload implements IUpdaterPhase {

    private int  octetsDownloaded, fileDownloaded;
    public static List<MDFile> toDownload = new ArrayList<>();
    double previousOctets;
    double speed;
    String speedStr = "calc..";
    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
    private static DecimalFormat df2 = new DecimalFormat("#.##");

    @Override
    public boolean isUpToDate() {
        File directory = GameUpdater.get().getUpdateDirectory().toFile();
        Collection<File> localFiles = new ConcurrentLinkedQueue<>(FileUtils.listFiles(directory, null, true));
        AtomicInteger fileAnalyzed = new AtomicInteger();

        CountDownLatch latch = new CountDownLatch(DownloadTask.remoteContent.size());
        ExecutorService taskExecutor = Executors.newFixedThreadPool(20);

        for (MDFile mdFile : GlobalPhaseData.manifest) {
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
                GameUpdater.get().getProgressCallback().onProgressUpdate(String.format("Analyse des fichiers (%s/%s)", current, GlobalPhaseData.manifest.size()));
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
            for (String now : GlobalPhaseData.ignoreList) {
                if (file.toString().contains(now.replace("/", "\\"))) {
                    GameUpdaterOld.LOGGER.info("[IGNORE LIST] This file is ignored: " + file.getName());
                    ignore = true;
                }
            }

            if (!ignore)
                file.delete();
        });


        AtomicBoolean needUpdate = new AtomicBoolean(false);
        GlobalPhaseData.manifest.forEach((file) -> {
            try {
                String path = file.getPath();
                String name = file.getName();

                File cursor = new File(new File(directory, path), name);
                if (!cursor.exists()) {
                    needUpdate.set(true);
                    toDownload.add(file);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return needUpdate.get();
    }

    @Override
    public UpdateResult download() {
        File directory = GameUpdater.get().getUpdateDirectory().toFile();
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

        CountDownLatch latch = new CountDownLatch(toDownload.size());
        ExecutorService taskExecutor = Executors.newFixedThreadPool(20);

        for (MDFile file : toDownload) {
            taskExecutor.submit(() -> {
                String path = file.getPath();
                String name = file.getName();
                File cursor = new File(new File(directory, path), name);

                System.out.println(cursor.getAbsolutePath());
                if (cursor.getParentFile().exists()) {
                    if (!cursor.exists()) {
                        downloadFile(cursor, file);
                    }
                } else {
                    cursor.getParentFile().mkdirs();
                    downloadFile(cursor, file);
                }
                latch.countDown();
            });

        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            return UpdateResult.faillure();
        }
        taskExecutor.shutdown();
        System.out.println("All download finished !");
        return UpdateResult.success();
    }

    public void downloadFile(File cursor, MDFile obj) {

        String filePath = obj.getPath();
        String name = obj.getName();

        try {
            URL fileUrl;
            fileUrl = new URL(GameUpdater.get().getUpdateServer()+ "/downloads/" + filePath + name);

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
            GameUpdater.get().getProgressCallback().onProgressUpdate(String.format("Téléchargement (%s/%s) (%s mo/s)", fileDownloaded, toDownload.size(), speedStr));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getFriendlyName() {
        return "Mise à jour du jeu";
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
}
