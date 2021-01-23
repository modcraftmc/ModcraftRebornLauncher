package fr.modcraftmc.launcher.logger;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Logger;

public class LogManager {

    public static Logger createLogger(String name) {

        Logger logger = Logger.getLogger(name);
        logger.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        Formatter formatter = new LogFormatter();
        handler.setFormatter(formatter);
        logger.addHandler(handler);

        return logger;
    }
}
