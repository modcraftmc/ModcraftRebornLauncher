package fr.modcraftmc.libs.updater.phases;

import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.download.DownloadList;
import fr.flowarg.flowupdater.download.IProgressCallback;
import fr.flowarg.flowupdater.utils.ModFileDeleter;
import fr.flowarg.flowupdater.utils.UpdaterOptions;
import fr.flowarg.flowupdater.versions.AbstractForgeVersion;
import fr.flowarg.flowupdater.versions.ForgeVersionBuilder;
import fr.flowarg.flowupdater.versions.VanillaVersion;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.libs.updater.GameUpdater;
import fr.modcraftmc.libs.updater.UpdateResult;

public class GameDownload{

    public static boolean isUpToDate() {
    return false;
    }

    public static UpdateResult download() {
        VanillaVersion version = new VanillaVersion.VanillaVersionBuilder().withName(ModcraftApplication.MC_VERSION).build();
        UpdaterOptions options = new UpdaterOptions.UpdaterOptionsBuilder().withSilentRead(false).build();

        AbstractForgeVersion forgeVersion = new ForgeVersionBuilder(ForgeVersionBuilder.ForgeVersionType.NEW)
                .withForgeVersion(ModcraftApplication.FORGE_VERSION)
                .withMods("https://modcraftmc.fr/mods.json")
                .withFileDeleter(new ModFileDeleter())
                .build();

        FlowUpdater updater = new FlowUpdater.FlowUpdaterBuilder().withVanillaVersion(version).withUpdaterOptions(options).withProgressCallback(new IProgressCallback() {
            @Override
            public void update(DownloadList.DownloadInfo info) {
                System.out.println(info.getDownloadedFiles());
                System.out.println( info.getTotalToDownloadFiles());
                GameUpdater.get().getProgressCallback().onProgressUpdate("Downloading ", info.getDownloadedFiles(), info.getTotalToDownloadFiles());
            }

        }).withModLoaderVersion(forgeVersion).build();
        try {
            System.out.println(GameUpdater.get().getUpdateDirectory());
            updater.update(GameUpdater.get().getUpdateDirectory());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return UpdateResult.SUCCESS;
    }
}
