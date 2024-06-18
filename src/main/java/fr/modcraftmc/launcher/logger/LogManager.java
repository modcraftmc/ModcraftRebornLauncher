package fr.modcraftmc.launcher.logger;

import fr.modcraftmc.launcher.resources.FilesManager;

import java.io.File;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Logger;

public class LogManager {

    private static FileHandler fileHandler;

    public static void init() {
        try {
            fileHandler = new FileHandler(new File(FilesManager.LOGS_PATH, "launcher.log").getPath());
            fileHandler.setFormatter(new FileLogFormatter());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Logger createLogger(String name) {
        Logger logger = Logger.getLogger(name);
        logger.setUseParentHandlers(false);
        if (fileHandler != null) logger.addHandler(fileHandler);
        ConsoleHandler handler = new ConsoleHandler();
        Formatter formatter = new LogFormatter();
        handler.setFormatter(formatter);
        logger.addHandler(handler);
        return logger;
    }

    public static FileHandler getFileHandler() {
        return fileHandler;
    }
}
