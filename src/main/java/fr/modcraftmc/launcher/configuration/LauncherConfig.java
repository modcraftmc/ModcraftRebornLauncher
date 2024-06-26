package fr.modcraftmc.launcher.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.modcraftmc.launcher.logger.LogManager;
import fr.modcraftmc.libs.errors.ErrorsHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class LauncherConfig {


    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(InstanceProperty.class, ConfigurationSerializer.instancePropertyJsonSerializer)
            .registerTypeAdapter(InstanceProperty.class, ConfigurationSerializer.instancePropertyJsonDeserializer)
            .create();
    private final static Logger logger = LogManager.createLogger("LauncherConfig");
    private static LauncherConfig instance;
    private static File configFile;


    //configs

    private boolean keeplogin = true;
    private boolean keepOpen = true;
    private String refreshToken = "";
    private int ram = 6;
    private InstanceProperty instanceProperty;
    private long latestGamePid;
    private boolean discordActivityEnabled = true;

    public static LauncherConfig load(File file) {

        configFile = file;
        if (!file.exists()) {
            logger.info("Création du fichier de configuration.");

            try {
                file.createNewFile();
            } catch (IOException e) {
                logger.severe("Erreur lors de la création du fichier de configuration.");
                logger.severe(e.getMessage());
                logger.severe("Merci de contacter le support.");
            }
        } else {
            logger.info("Chargement du fichier de configuration.");
        }

        instance = loadFile();

        if (instance == null) {
            instance = loadDefaults();
            instance.save();
        }

        return instance;

    }

    private static LauncherConfig loadDefaults() {
        instance = new LauncherConfig();
        return instance;
    }

    private static LauncherConfig loadFile() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile)));
            return gson.fromJson(reader, LauncherConfig.class);
        } catch (Exception e) {
            ErrorsHandler.handleErrorAndCrashApplication(e);
            return null;
        }
    }

    public boolean isKeeplogin() {
        return this.keeplogin;
    }

    public void setKeeplogin(boolean keeplogin) {
        this.keeplogin = keeplogin;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String accesToken) {
        this.refreshToken = accesToken;
    }

    public boolean isKeepOpen() {
        return keepOpen;
    }

    public void setKeepOpen(boolean keepOpen) {
        this.keepOpen = keepOpen;
    }

    public InstanceProperty getInstanceProperty() {
        return instanceProperty;
    }

    public void setInstanceProperty(InstanceProperty instanceProperty) {
        this.instanceProperty = instanceProperty;
    }

    public int getRam() {
        return ram;
    }

    public void setRam(int ram) {
        this.ram = ram;
    }

    public long latestGamePid() {
        return latestGamePid;
    }

    public void setLatestGamePid(long latestGamePid) {
        this.latestGamePid = latestGamePid;
    }

    public void setDiscordActivityEnabled(boolean discordActivityEnabled) {
        this.discordActivityEnabled = discordActivityEnabled;
    }

    public boolean isDiscordActivityEnabled() {
        return discordActivityEnabled;
    }

    public void save() {
        logger.info("Sauvegarde du fichier du configuration.");
        String jsonConfig = gson.toJson(this);
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8));
            out.write(jsonConfig);
            out.flush();
            out.close();
        } catch (IOException e) {
            ErrorsHandler.handleErrorWithCustomHeader("Impossible de sauvegarder le fichier de configuration. (plus d'espace disque?)", e);
        }
    }
}
