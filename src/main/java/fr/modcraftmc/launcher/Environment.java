package fr.modcraftmc.launcher;

public class Environment {

    public enum ENV {
        DEV,
        PROD
    }

    public enum OS {
        WINDOWS,
        LINUX,
        MAC,
        OTHERS
    }

    private ENV env;
    private final OS os;
    private final String prodApiUrl;
    private final String devApiUrl;

    public Environment(OS operatingSystem, String prodApiUrl, String devApiUrl) {
        this.os = operatingSystem;
        this.prodApiUrl = prodApiUrl;
        this.devApiUrl = devApiUrl;
    }

    public ENV getEnv() {
        return env;
    }

    public OS getOS() {
        return os;
    }

    public void setEnv(ENV environment) {
        this.env = environment;
    }

    public String getApiUrl() {
        if (this.env == ENV.DEV) {
            return this.devApiUrl;
        }
        return this.prodApiUrl;
    }

    @Override
    public String toString() {
        return "Environment: " + env + ", OS: " + os + "CurrentApiUrl: " + this.getApiUrl();
    }
}