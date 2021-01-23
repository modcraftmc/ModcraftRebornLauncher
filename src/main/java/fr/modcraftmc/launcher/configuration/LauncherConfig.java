package fr.modcraftmc.launcher.configuration;

import com.google.gson.*;
import fr.modcraftmc.launcher.Utils;
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
    private String accesToken = "";
    private int ram;
    private InstanceProperty instanceProperty;

    public boolean isKeeplogin() {
        return keeplogin;
    }

    public String getAccesToken() {
        return accesToken;
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

    public void setAccesToken(String accesToken) {
        this.accesToken = accesToken;
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
        }

        instance.save();
        return instance;

    }

    public void save() {
        logger.info("Sauvegarde du fichier du configuration.");
        String jsonConfig = gsonSer.toJson(this);
        /*
        FileWriter writer;
        try {
            writer = new FileWriter(configFile);
            writer.write(jsonConfig);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            logger.severe("Erreur lors de la sauvegarde du fichier du configuration.");
            logger.severe(e.getMessage());
            logger.severe("Merci de contacter le support.");

        }

         */

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
