package fr.modcraftmc.launcher;

import fr.modcraftmc.launcher.logger.LogManager;
import javafx.application.Application;

import java.util.logging.Logger;

public class PrimaryLauncher {

    private static Logger LOGGER = LogManager.createLogger("PrimaryLauncher");

    public static void main(String[] args) {
        LOGGER.info("Performing primary check before launch.");
        try {
            Class<?> appClass = Class.forName("javafx.application.Application");
            LOGGER.info("JavaFX found, processing...");
            Application.launch(ModcraftApplication.class, args);

        } catch (ClassNotFoundException e) {
           LOGGER.severe("JavaFX not found, please contact suport.");
           System.exit(1);
        }
    }
}
