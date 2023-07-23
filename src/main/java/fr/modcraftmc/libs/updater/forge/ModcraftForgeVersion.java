package fr.modcraftmc.libs.updater.forge;

import fr.flowarg.flowio.FileUtils;
import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.download.json.*;
import fr.flowarg.flowupdater.utils.ModFileDeleter;
import fr.flowarg.flowupdater.versions.AbstractForgeVersion;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ModcraftForgeVersion extends AbstractForgeVersion {

    ModcraftForgeVersion(String forgeVersion, List<Mod> mods,
                    List<CurseFileInfo> curseMods, List<ModrinthVersionInfo> modrinthMods, ModFileDeleter fileDeleter,
                    OptiFineInfo optiFine, CurseModPackInfo modPack, ModrinthModPackInfo modrinthModPackInfo)
    {
        super(mods, curseMods, modrinthMods, forgeVersion, fileDeleter, optiFine, modPack, modrinthModPackInfo, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void install(final Path dirToInstall) throws Exception
    {
        super.install(dirToInstall);

        try (BufferedInputStream stream = new BufferedInputStream(this.installerUrl.openStream()))
        {
            final ModLoaderLauncherEnvironment forgeLauncherEnvironment = this.prepareModLoaderLauncher(dirToInstall, stream);
            final ProcessBuilder processBuilder = new ProcessBuilder(forgeLauncherEnvironment.getCommand());

            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            final Process process = processBuilder.start();
            process.waitFor();

            this.logger.info("Successfully installed Forge!");
            FileUtils.deleteDirectory(forgeLauncherEnvironment.getTempDir());
        }
        catch (IOException | InterruptedException e)
        {
            this.logger.printStackTrace(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkModLoaderEnv(@NotNull Path dirToInstall) throws Exception
    {
        if(super.checkModLoaderEnv(dirToInstall))
        {
            FileUtils.deleteDirectory(dirToInstall.resolve("libraries").resolve("net").resolve("minecraft"));
            FileUtils.deleteDirectory(dirToInstall.resolve("libraries").resolve("net").resolve("minecraftforge"));
            FileUtils.deleteDirectory(dirToInstall.resolve("libraries").resolve("de").resolve("oceanlabs"));
            FileUtils.deleteDirectory(dirToInstall.resolve("libraries").resolve("cpw"));
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void cleanInstaller(@NotNull Path tempInstallerDir) throws Exception
    {
        FileUtils.deleteDirectory(tempInstallerDir.resolve("net"));
        FileUtils.deleteDirectory(tempInstallerDir.resolve("com"));
        FileUtils.deleteDirectory(tempInstallerDir.resolve("joptsimple"));
        Files.deleteIfExists(tempInstallerDir.resolve("META-INF").resolve("MANIFEST.MF"));
        Files.deleteIfExists(tempInstallerDir.resolve("lekeystore.jks"));
        Files.deleteIfExists(tempInstallerDir.resolve("big_logo.png"));
        Files.deleteIfExists(tempInstallerDir.resolve("META-INF").resolve("FORGE.DSA"));
        Files.deleteIfExists(tempInstallerDir.resolve("META-INF").resolve("FORGE.SF"));
    }

    @Override
    public void attachFlowUpdater(@NotNull FlowUpdater flowUpdater)
    {
        super.attachFlowUpdater(flowUpdater);
        if (!this.modLoaderVersion.contains("-"))
            this.modLoaderVersion = this.vanilla.getName() + '-' + this.modLoaderVersion;
        else this.modLoaderVersion = this.modLoaderVersion.trim();
        try
        {
            this.installerUrl = new URL(
                    String.format("https://download.modcraftmc.fr/forge-1.19.2-43.2.21-installer.jar",
                            this.modLoaderVersion, this.modLoaderVersion));
        } catch (Exception e)
        {
            this.logger.printStackTrace(e);
        }
    }
}
