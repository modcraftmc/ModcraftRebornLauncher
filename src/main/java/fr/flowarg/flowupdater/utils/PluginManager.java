package fr.flowarg.flowupdater.utils;

import fr.flowarg.flowio.FileUtils;
import fr.flowarg.flowlogger.ILogger;
import fr.flowarg.flowupdater.FlowUpdater;
import fr.flowarg.flowupdater.download.DownloadInfos;
import fr.flowarg.flowupdater.download.ICurseFeaturesUser;
import fr.flowarg.flowupdater.download.IProgressCallback;
import fr.flowarg.flowupdater.download.Step;
import fr.flowarg.flowupdater.download.json.CurseFileInfos;
import fr.flowarg.flowupdater.download.json.CurseModPackInfos;
import fr.flowarg.flowupdater.versions.AbstractForgeVersion;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static fr.flowarg.flowio.FileUtils.getFileSizeBytes;
import static fr.flowarg.flowio.FileUtils.getMD5ofFile;

public class PluginManager
{
    private final IProgressCallback progressCallback;
    private final UpdaterOptions options;
    private final ILogger logger;
    private final DownloadInfos downloadInfos;

    private boolean cursePluginLoaded = false;
    private boolean optifinePluginLoaded = false;

    public PluginManager(FlowUpdater updater)
    {
        this.progressCallback = updater.getCallback();
        this.options = updater.getUpdaterOptions();
        this.logger = updater.getLogger();
        this.downloadInfos = updater.getDownloadInfos();
    }

    public void loadPlugins(File dir) throws Exception
    {
        if (this.options.isEnableModsFromCurseForge())
            this.updatePlugin(new File(dir, "FUPlugins/CurseForgePlugin.jar"), "CurseForgePlugin", "CFP");

        if (this.options.isInstallOptifineAsMod())
            this.updatePlugin(new File(dir, "FUPlugins/OptifinePlugin.jar"), "OptifinePlugin", "OP");

        this.logger.debug("Configuring PLA...");
        this.configurePLA(dir);
    }

    public void updatePlugin(File out, String name, String alias) throws Exception
    {
        boolean flag = true;
        if (out.exists())
        {
            final String crc32 = IOUtils.getContent(new URL(String.format("https://flowarg.github.io/minecraft/launcher/%s.info", name))).trim();
            if (FileUtils.getCRC32(out) == Long.parseLong(crc32)) flag = false;
        }

        if (flag)
        {
            this.logger.debug(String.format("Downloading %s...", alias));
            IOUtils.download(this.logger, new URL(String.format("https://flowarg.github.io/minecraft/launcher/%s.jar", name)), out);
        }
    }

    public void loadCurseForgePlugin(File dir, ICurseFeaturesUser curseFeaturesUser)
    {
        final List<Object> allCurseMods = new ArrayList<>(curseFeaturesUser.getCurseMods().size());
        for (CurseFileInfos infos : curseFeaturesUser.getCurseMods())
        {
            if (!this.cursePluginLoaded)
            {
                try
                {
                    Class.forName("fr.flowarg.flowupdater.curseforgeplugin.CurseForgePlugin");
                    this.cursePluginLoaded = true;
                } catch (ClassNotFoundException e)
                {
                    this.cursePluginLoaded = false;
                    this.logger.err("Cannot install mods from CurseForge: CurseAPI is not loaded. Please, enable the 'enableModsFromCurseForge' updater option !");
                    break;
                }
            }

        }
        final CurseModPackInfos modPackInfos = curseFeaturesUser.getModPackInfos();
        if (modPackInfos != null)
        {
            this.progressCallback.step(Step.MOD_PACK);
        }

        curseFeaturesUser.setAllCurseMods(allCurseMods);
    }

    public void loadOptifinePlugin(File dir, AbstractForgeVersion forgeVersion)
    {

    }

    public void configurePLA(File dir)
    {

    }

    public void shutdown()
    {

    }

    public ILogger getLogger()
    {
        return this.logger;
    }

    public boolean isCursePluginLoaded()
    {
        return this.cursePluginLoaded;
    }

    public boolean isOptifinePluginLoaded()
    {
        return this.optifinePluginLoaded;
    }
}
