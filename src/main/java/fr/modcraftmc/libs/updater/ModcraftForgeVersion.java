package fr.modcraftmc.libs.updater;

import fr.flowarg.flowio.FileUtils;
import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.download.json.*;
import fr.flowarg.flowupdater.utils.ModFileDeleter;
import fr.flowarg.flowupdater.versions.AbstractForgeVersion;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Represent a new Forge version (1.12.2-14.23.5.2851 to 1.19)
 * @author FlowArg
 */
public class ModcraftForgeVersion extends AbstractForgeVersion
{
    private final String[] compatibleVersions = {"1.19.2-43.2.12", "1.19.2", "1.18", "1.17",
            "1.16", "1.15", "1.14",
            "1.13", "1.12.2-14.23.5.285", "1.12.2-14.23.5.286"};

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
        if (!this.isCompatible()) return;

        try (BufferedInputStream stream = new BufferedInputStream(this.installerUrl.openStream()))
        {
            final ModLoaderLauncherEnvironment forgeLauncherEnvironment = this.prepareModLoaderLauncher(dirToInstall, stream);
            final ProcessBuilder processBuilder = new ProcessBuilder(forgeLauncherEnvironment.getCommand());

            processBuilder.redirectOutput(Redirect.INHERIT);
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
        if(this.isCompatible() && super.checkModLoaderEnv(dirToInstall))
        {
            FileUtils.deleteDirectory(dirToInstall.resolve("libraries").resolve("net").resolve("minecraft"));
            FileUtils.deleteDirectory(dirToInstall.resolve("libraries").resolve("net").resolve("minecraftforge"));
            FileUtils.deleteDirectory(dirToInstall.resolve("libraries").resolve("de").resolve("oceanlabs"));
            FileUtils.deleteDirectory(dirToInstall.resolve("libraries").resolve("cpw"));
        }

        return false;
    }

    private boolean isCompatible()
    {
        for (String str : this.compatibleVersions)
        {
            if (this.modLoaderVersion.startsWith(str))
                return true;
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
    public void attachFlowUpdater(@NotNull FlowUpdater flowUpdater) {
        super.attachFlowUpdater(flowUpdater);
        try {
//            this.installerUrl = new URL(
//                    String.format("https://maven.minecraftforge.net/net/minecraftforge/forge/%s/forge-%s-installer.jar", //TODO: use our own forge
//                            this.modLoaderVersion, this.modLoaderVersion));
            this.installerUrl = new URL(
                    "https://modcraftmc.fr/forge-1.19.2-43.2.12-installer.jar"); //TODO: use our own forge);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
