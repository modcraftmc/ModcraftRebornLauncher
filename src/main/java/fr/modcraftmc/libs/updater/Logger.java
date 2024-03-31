package fr.modcraftmc.libs.updater;

import fr.flowarg.flowlogger.ILogger;
import fr.modcraftmc.launcher.ModcraftApplication;

import java.nio.file.Path;

public class Logger implements ILogger {
    @Override
    public void err(String s) {
        ModcraftApplication.LOGGER.info(s);
    }

    @Override
    public void info(String s) {
        ModcraftApplication.LOGGER.info(s);
    }

    @Override
    public void warn(String s) {
        ModcraftApplication.LOGGER.info(s);
    }

    @Override
    public void debug(String s) {
        ModcraftApplication.LOGGER.info(s);
    }

    @Override
    public void infoColor(EnumLogColor enumLogColor, String s) {
        ModcraftApplication.LOGGER.info(s);
    }

    @Override
    public void printStackTrace(String s, Throwable throwable) {
        ModcraftApplication.LOGGER.info(s);
    }

    @Override
    public void printStackTrace(Throwable throwable) {
        ModcraftApplication.LOGGER.info(throwable.getMessage());
    }

    @Override
    public Path getLogPath() {
        return null;
    }

    @Override
    public void setLogPath(Path path) {

    }

    @Override
    public String getPrefix() {
        return null;
    }
}
