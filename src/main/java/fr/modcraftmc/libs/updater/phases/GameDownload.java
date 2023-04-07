package fr.modcraftmc.libs.updater.phases;

import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.utils.UpdaterOptions;
import fr.flowarg.flowupdater.versions.AbstractForgeVersion;
import fr.flowarg.flowupdater.versions.ForgeVersionBuilder;
import fr.flowarg.flowupdater.versions.VanillaVersion;
import fr.modcraftmc.launcher.ModcraftApplication;
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

public class GameDownload{

    private static int octetsDownloaded, fileDownloaded;
    public static List<MDFile> toDownload = new ArrayList<>();
    private static double previousOctets, speed;
    static String speedStr = "calc..";
    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
    private static DecimalFormat df2 = new DecimalFormat("#.##");

    public static boolean isUpToDate() {
    return false;
    }

    public static UpdateResult download() {
        VanillaVersion version = new VanillaVersion.VanillaVersionBuilder().withName(ModcraftApplication.MC_VERSION).build();
        UpdaterOptions options = new UpdaterOptions.UpdaterOptionsBuilder().build();

        AbstractForgeVersion forgeVersion = new ForgeVersionBuilder(ForgeVersionBuilder.ForgeVersionType.NEW)
                .withForgeVersion(ModcraftApplication.FORGE_VERSION)
                .build();

        FlowUpdater updater = new FlowUpdater.FlowUpdaterBuilder().withVanillaVersion(version).withUpdaterOptions(options).withModLoaderVersion(forgeVersion).build();
        try {
            updater.update(GameUpdater.get().getUpdateDirectory());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return UpdateResult.SUCCESS;
    }
    public String getFriendlyName() {
        return "Mise à jour du jeu";
    }
}
