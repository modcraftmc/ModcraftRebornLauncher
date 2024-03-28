package fr.modcraftmc.libs.discord;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;
import fr.modcraftmc.launcher.AsyncExecutor;

import java.time.Instant;

public class DiscordAPI {

    private Core core;
    private Activity activity;
    private boolean isLoaded = false;

    public static void run() {
        AsyncExecutor.runAsync(DiscordAPI::new);
    }

    public DiscordAPI() {
        // Initialize the Core
        Core.initFromClasspath();

        // Set parameters for the Core
        try(CreateParams params = new CreateParams())
        {
            params.setClientID(637707031804903425L);
            params.setFlags(CreateParams.getDefaultFlags());
            // Create the Core
            this.core = new Core(params);
            // Create the Activity
            this.activity = new Activity();

            activity.setDetails("serveur survie moddé 1.19");
            activity.setState("chargement du jeu");

            // Setting a start time causes an "elapsed" field to appear
            activity.timestamps().setStart(Instant.now());
            // Make a "cool" image show up
            activity.assets().setLargeImage("logo");

            // Setting a join secret and a party ID causes an "Ask to Join" button to appear
            activity.party().size().setCurrentSize(1);
            activity.party().size().setMaxSize(100);
            activity.party().setID("server");
            activity.secrets().setJoinSecret("modcraft");
            //core.activityManager().registerCommand("java -jar " + System.getProperty("bootstrapPath"));

            // Finally, update the current activity to our activity
            core.activityManager().updateActivity(activity);

            this.isLoaded = true;
            // Run callbacks forever
            while(true)
            {
                core.runCallbacks();
                try
                {
                    // Sleep a bit to save CPU
                    Thread.sleep(16);
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
