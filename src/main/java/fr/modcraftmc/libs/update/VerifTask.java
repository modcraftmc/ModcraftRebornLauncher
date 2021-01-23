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

public class VerifTask extends Task<Void> {

    public final String serverUrl;
    public final File directory;
    public final ProgressBar progressBar;
    private final Label label;
    private int fileAnalyzed = 0;


    public VerifTask(String url, File gameDir, ProgressBar progressBar, Label label) {
        this.serverUrl = url;
        this.directory = gameDir;
        this.progressBar = progressBar;
        this.label = label;


        DownloadTask.remoteContent = DownloadUtils.getRemoteContent(serverUrl + "/content.json", label);

        DownloadTask.ignoreList = DownloadUtils.getIgnoreList(serverUrl + "/ignore.txt");
    }

    public void fileDeteter() {

        Collection<File> localFiles = FileUtils.listFiles(directory, null, true);
        System.out.println(localFiles.size());

        DownloadTask.remoteContent.parallelStream()
                .forEach(mdFile -> {

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
                    fileAnalyzed++;
                    this.updateProgress(fileAnalyzed, DownloadTask.remoteContent.size());
                    Platform.runLater(() -> label.setText(String.format("Analyse (%s/%s)", fileAnalyzed - 1, DownloadTask.remoteContent.size())));

                });


        localFiles.parallelStream().forEach(file -> {


            boolean ignore = false;
            for (String now : DownloadTask.ignoreList) {
                if (file.toString().contains(now.replace("/", "\\"))) {
                    GameUpdater.LOGGER.info("[IGNORE LIST] This file is ignored: " + file.getName());
                    ignore = true;
                }
            }

            if (!ignore) {
                System.out.println("DELETING " + file.getName() + " " + file.delete());
            }
        });


        /*
        Collection<File> localFile = FileUtils.listFiles(directory, null, true);

        System.out.println(localFile.size());

        for (File cursor : localFile) {

            if (cursor.isDirectory()) return;
           GameUpdater.LOGGER.info("current file : " + cursor.getName());
            this.updateProgress(fileAnalyzed, localFile.size());
            Platform.runLater(() -> label.setText(String.format("Analyse (%s/%s)", fileAnalyzed, localFile.size())));

                AtomicBoolean ignore = new AtomicBoolean(false);

            String checkSum = getFileChecksum(digest, cursor);

            DownloadTask.remoteContent
                    .parallelStream()
                    .forEach(mdFile -> {
                            //GameUpdater.LOGGER.info("a"+ checkSum);
                           // GameUpdater.LOGGER.info( "b" + mdFile.getMd5());

                            if (!mdFile.getMd5().equals(checkSum)) {
                                ignore.set(true);
                            }
                    });

            for (String now : DownloadTask.ignoreList) {
                if (cursor.toString().contains(now.replace("/", "\\"))) {
                    GameUpdater.LOGGER.info("[IGNORE LIST] This file is ignored: " + cursor.getName());
                    ignore.set(true);
                }
            }

            if (!ignore.get()) {
                cursor.delete();
            }


            fileAnalyzed++;
        }
         */

    }

    private static String getFileChecksum(MessageDigest digest, File file)
    {
        try {
            //Get file input stream for reading the file content
            FileInputStream fis = new FileInputStream(file);

            //Create byte array to read data in chunks
            byte[] byteArray = new byte[1024];
            int bytesCount = 0;

            //Read file data and update in message digest
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }

            //close the stream; We don't need it now.
            fis.close();

            //Get the hash's bytes
            byte[] bytes = digest.digest();

            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }

            //return complete hash
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void verification() {


        DownloadTask.remoteContent.forEach((file) -> {

            try {

                /*
                String filePath = element.get("path").toString().replaceAll("-FP-","/").replaceAll("/var/www/gameupdater/downloads/", "");

                String name = element.get("filename").toString();

                File cursor = new File(new File(directory, filePath), name);

                if (!cursor.exists()) {
                    DownloadTask.toDownload.add(element);
                }

                 */
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
        fileDeteter();
        verification();
        return null;
    }
}
