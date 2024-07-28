package fr.modcraftmc.libs.discord;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;
import fr.modcraftmc.launcher.ModcraftApplication;
import fr.modcraftmc.launcher.logger.LogManager;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.logging.Logger;

public class DiscordManager {

    public Logger LOGGER = LogManager.createLogger("DiscordManager");
    private volatile boolean  isRunning;
    private Core core;
    private Activity activity;
    private boolean isLoaded = false;
    private Runnable onLoaded;

    public void start() {
        if (!ModcraftApplication.launcherConfig.isDiscordActivityEnabled())
            return;

        try {
            File discordLibrary = DownloadNativeLibrary.downloadDiscordLibrary();
            if(discordLibrary == null)
            {
                LOGGER.warning("Error downloading Discord SDK.");
                return;
            }

            Core.init(discordLibrary);

            // Set parameters for the Core
            try(CreateParams params = new CreateParams())
            {
                params.setClientID(637707031804903425L);
                params.setFlags(CreateParams.Flags.NO_REQUIRE_DISCORD);
                this.core = new Core(params);
                this.activity = new Activity();

                activity.setDetails("serveur survie moddé 1.19");
                activity.setState("sur le launcher");
                activity.timestamps().setStart(Instant.now());
                activity.assets().setLargeImage("logo");
                activity.assets().setLargeText("discord.modcraftmc.fr");
                core.activityManager().updateActivity(activity);

                this.isLoaded = true;
                this.isRunning = true;
                this.LOGGER.info("Discord Activity loaded");

                if (onLoaded != null)
                    onLoaded.run();

                while(isRunning) {
                        core.runCallbacks();
                        try {
                            Thread.sleep(12);
                        }
                        catch(InterruptedException ignored) {}
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setOnLoaded(Runnable onLoaded) {
        this.onLoaded = onLoaded;
    }

    public void stop() {
        isRunning = false;
        this.core.close();
    }

    public void setPlayersCount(int online, int max) {
        if (!this.isLoaded || online == 0)
            return;
        this.activity.party().size().setCurrentSize(online);
        activity.party().size().setMaxSize(max);
        core.activityManager().updateActivity(activity);

    }

    public void setState(String state) {
        if (!this.isLoaded)
            return;
        this.activity.setState(state);
        core.activityManager().updateActivity(activity);
    }
}
