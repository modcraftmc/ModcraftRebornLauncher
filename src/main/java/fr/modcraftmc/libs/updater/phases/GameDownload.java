package fr.modcraftmc.libs.updater.phases;

import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.utils.UpdaterOptions;
import fr.flowarg.flowupdater.versions.AbstractForgeVersion;
import fr.flowarg.flowupdater.versions.VanillaVersion;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.libs.updater.GameUpdater;
import fr.modcraftmc.libs.updater.ModcraftForgeVersionBuilder;
import fr.modcraftmc.libs.updater.UpdateResult;

public class GameDownload{

    public static boolean isUpToDate() {
    return false;
    }

    public static UpdateResult download() {
        VanillaVersion version = new VanillaVersion.VanillaVersionBuilder().withName(ModcraftApplication.MC_VERSION).build();
        UpdaterOptions options = new UpdaterOptions.UpdaterOptionsBuilder().build();

        AbstractForgeVersion forgeVersion = new ModcraftForgeVersionBuilder(ModcraftForgeVersionBuilder.ForgeVersionType.MODCRAFT)
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
