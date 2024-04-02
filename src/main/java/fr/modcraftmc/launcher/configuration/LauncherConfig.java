package fr.modcraftmc.launcher.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.modcraftmc.launcher.logger.LogManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class LauncherConfig {



    private static LauncherConfig instance;
    private static Gson gsonSer = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(InstanceProperty.class, ConfigurationSerializer.instancePropertyJsonSerializer)
            .create();
    private static Gson gsonDes = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(InstanceProperty.class, ConfigurationSerializer.instancePropertyJsonDeserializer)
            .create();;
    private final static Logger logger = LogManager.createLogger("LauncherConfig");
    private static File configFile;


    //configs

    private boolean keeplogin;
    private boolean keepOpen;
    private String refreshToken = "";
    private int ram = 6;
    private InstanceProperty instanceProperty;
    private long latestGamePid;

    public boolean isKeeplogin() {
        return true;
    }

    public String getRefreshToken() {
        return refreshToken;
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


    public void setKeeplogin(boolean keeplogin) {
        this.keeplogin = keeplogin;
    }

    public void setRefreshToken(String accesToken) {
        this.refreshToken = accesToken;
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

    public void save() {
        logger.info("Sauvegarde du fichier du configuration.");
        String jsonConfig = gsonSer.toJson(this);
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8));
            out.write(jsonConfig);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static LauncherConfig loadDefaults() {
        instance = new LauncherConfig();
        return instance;
    }

    private static LauncherConfig loadFile() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile)));
            return gsonDes.fromJson(reader, LauncherConfig.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
