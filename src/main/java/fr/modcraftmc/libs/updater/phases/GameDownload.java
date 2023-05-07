package fr.modcraftmc.libs.updater.phases;

import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.download.DownloadList;
import fr.flowarg.flowupdater.download.IProgressCallback;
import fr.flowarg.flowupdater.utils.UpdaterOptions;
import fr.flowarg.flowupdater.versions.AbstractForgeVersion;
import fr.flowarg.flowupdater.versions.ForgeVersionBuilder;
import fr.flowarg.flowupdater.versions.VanillaVersion;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.libs.updater.GameUpdater;
import fr.modcraftmc.libs.updater.ModcraftForgeVersionBuilder;
import fr.modcraftmc.libs.updater.UpdateResult;

import java.net.URL;

public class GameDownload{

    public static boolean isUpToDate() {
    return false;
    }

    public static UpdateResult download() {
        VanillaVersion version = new VanillaVersion.VanillaVersionBuilder().withName(ModcraftApplication.MC_VERSION).build();
        UpdaterOptions options = new UpdaterOptions.UpdaterOptionsBuilder().withSilentRead(false).build();

        AbstractForgeVersion forgeVersion = new ModcraftForgeVersionBuilder(ModcraftForgeVersionBuilder.ForgeVersionType.MODCRAFT)
                .withForgeVersion(ModcraftApplication.FORGE_VERSION)
                .withMods("https://modcraftmc.fr/mods.json")
                .build();

        FlowUpdater updater = new FlowUpdater.FlowUpdaterBuilder().withVanillaVersion(version).withUpdaterOptions(options).withProgressCallback(new IProgressCallback() {
            @Override
            public void update(DownloadList.DownloadInfo info) {
                System.out.println(info.getDownloadedFiles());
                System.out.println( info.getTotalToDownloadFiles());
                GameUpdater.get().getProgressCallback().onProgressUpdate("", info.getDownloadedFiles(), info.getTotalToDownloadFiles());
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
    public String getFriendlyName() {
        return "Mise à jour du jeu";
    }
}
